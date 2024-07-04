package uk.ac.standrews.cs.descent;

import uk.ac.standrews.cs.descent.daos.DataAccessObject;
import uk.ac.standrews.cs.utilities.Pair;
import uk.metricSpaceFramework.coreConcepts.Metric;
import uk.metricSpaceFramework.util.ConcurrentOrderedList;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class Descent<metric_type> {

    //private final Map<Friends,Double> memo_distances;
    private final Set<Integer>[] new_neighbours;
    private final ConcurrentOrderedList<Integer,Double>[] neighbours;   // array of lists of neighbours (index into data) and distances
    private final Set<Integer>[] rev_neighbours; // indices into data
    private final Metric<metric_type> metric;
    private final int num_nns;
    private final Random random = new Random(67812345);
    private static final int BIG_NUMBER = 5000;
    private final DataAccessObject<metric_type> dao;

    public Descent(DataAccessObject<metric_type> dao, int num_nns) {
        this.dao = dao;
        this.num_nns = num_nns;
        this.metric = dao.metric();
        this.new_neighbours = new TreeSet[dao.getDataSize()];
        this.neighbours = new ConcurrentOrderedList[dao.getDataSize()];
        this.rev_neighbours = new TreeSet[dao.getDataSize()];
        randomlyInitialiseNeighbours();
        initialseNewNeighbours();
        initialiseReverseNeighbours();
        System.out.println( "Initial assignment Ave dist = " + getDists()  );
        improveAssignments();
    }

    public Descent(DataAccessObject<metric_type> dao, String saveIndexPath, String saveDistsPath, int num_nns ) {
        this.dao = dao;
        this.num_nns = num_nns;
        this.metric = dao.metric();
        int data_size = dao.getDataSize();
        this.neighbours = new ConcurrentOrderedList[data_size];
        this.new_neighbours = null; // not used by search
        this.rev_neighbours = null; // not used by search
        restoreState(saveIndexPath, saveDistsPath, data_size, num_nns);
    }

    public void debug() {
        ConcurrentOrderedList<Integer,Double> neebs = neighbours[0];
        List<Double> dists = neebs.getComparators();
        List<Integer> indices = neebs.getList();
        dao.showData("data zero: ", dao.getData(0));
        for( int nn_index = 0; nn_index < indices.size(); nn_index++ ) {
            int i = indices.get(nn_index);

            double d = dists.get(nn_index);
            double recalc = metric.distance(dao.getData(0),dao.getData(i));
            if( d != recalc ) {
                System.out.println( "Error at index " + i + " distances don\'t match map: " + d + " recalc: " + recalc) ;
            }
            System.out.println(i + " " + d );
            dao.showData("dat: " + i, dao.getData(i));
        }
    }

    private void initialseNewNeighbours() {
        for (int row_index = 0; row_index < neighbours.length; row_index++) {
            new_neighbours[row_index] = new TreeSet<>();
            List<Integer> copy_row = neighbours[row_index].getList();
            new_neighbours[row_index].addAll( copy_row );
        }
    }

    private void resetNewNeighbours() {
        IntStream.range(0, neighbours.length).parallel().forEach( i -> new_neighbours[i] = new TreeSet<>() );
    }

    private void randomlyInitialiseNeighbours() {
        for (int row_index = 0; row_index < neighbours.length; row_index++) {
            neighbours[row_index] = new ConcurrentOrderedList<>(num_nns);
        }
        for (int row_index = 0; row_index < neighbours.length; row_index++) {
            for (int i = 0; i < num_nns; i++) {
                int next_rand;
                // make sure we are not assigning a point to itself and it is not a duplicate
                do {
                    next_rand = random.nextInt(dao.getDataSize());
                }
                while ( next_rand == row_index || neighbours[row_index].contains(next_rand) );
                neighbours[row_index].add( next_rand, metric.distance(dao.getData(row_index),dao.getData(next_rand)));
            }
        }
    }

    private void initialiseReverseNeighbours() {
        for (int row_index = 0; row_index < rev_neighbours.length; row_index++) {
            rev_neighbours[row_index] = new TreeSet<>();
        }
    }

    private void improveAssignments() {
        int iter = 0;
        int updates_applied = 0;
        do {
            assignReverseNeighbours();
            ConcurrentHashMap<Integer,ConcurrentOrderedList<Integer,Double>> updates_to_apply = new ConcurrentHashMap<>();
            // initialise updates_to_apply before we go parallel
            for( int i = 0; i < neighbours.length; i++ ) {
                updates_to_apply.put(i,new ConcurrentOrderedList<>(num_nns));
            }
            final int iteration = iter;

            int to_apply = IntStream.range(0, neighbours.length).parallel().map( i -> improveAssignment(i, updates_to_apply ) ).sum();

            System.out.println( "Updates_to_apply: " + to_apply );
            updates_applied = applyUpdates(updates_to_apply);
            System.out.println( "Iteration " + iter++ + " Ave dist = " + getDists() + " updates applied " + updates_applied );
            // debug();
        }
        while( updates_applied > 100 );  // debug - iter < 1 ); // updates_applied > 100 );
    }

    public boolean checkIntegrity() {
        for (int i = 0; i < neighbours.length; i++) {
            Set<Integer> incoming = rev_neighbours[i];                  // nodes pointing at i
            ConcurrentOrderedList<Integer, Double> outgoing = neighbours[i];       // nodes which i is pointing at

            boolean found_b = false;
            for (int a_index : incoming) {
                if (neighbours[a_index].contains(i)) {
                    found_b = true;
                }
            }
            int count = 0;
            for (int c_index : outgoing.getList()) {
                Set<Integer> revs = rev_neighbours[c_index];
                if (revs.contains(i)) {
                    count++;
                }
            }
            if( ! found_b || count != num_nns ) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param b_index          - index into the neighbours array.
     *                         We are considering a -> b -> c and have been passed the index into b.
     *                         Does all pairs between a and c and replaces the num_nns neighbours in a and c if closer.
     * @param updates_to_apply
     */
    private int improveAssignment(int b_index, ConcurrentHashMap<Integer, ConcurrentOrderedList<Integer, Double>> updates_to_apply ) {

        Set<Integer> incoming = rev_neighbours[b_index];                           // nodes pointing at b <<<<< subset
        ConcurrentOrderedList<Integer,Double> outgoing = neighbours[b_index];      // nodes which b is pointing at
        int count = 0;

        for (int id_refers_to_b : incoming) {                                       // for all the nodes that are pointing at b
            if ( isNewNeighbour( id_refers_to_b,b_index ) ) {                       // if that node newly points at b
                for (int id_neighbour_of_b : outgoing.getList()) {                  // for all the nodes b points at
                    if ( isNewNeighbour(b_index, id_neighbour_of_b)) {              // if b newly points that node
                        if (id_refers_to_b != id_neighbour_of_b ) {                 // ensure we are not adding node to itself
                            double dist = metric.distance(dao.getData(id_refers_to_b), dao.getData(id_neighbour_of_b));
                            // Note there is a race here but it is safe - might do some extra work in the worst case.
                            if ( updates_to_apply.get(id_refers_to_b).getThreshold() == null || updates_to_apply.get(id_refers_to_b).getThreshold() > dist ) {
                                updates_to_apply.get(id_refers_to_b).add(id_neighbour_of_b, dist);
                                count += 1;
                            }
                            if( metric.isBirectional() ) {
                                if (updates_to_apply.get(id_refers_to_b).getThreshold() == null || updates_to_apply.get(id_refers_to_b).getThreshold() > dist) {
                                    updates_to_apply.get(id_neighbour_of_b).add(id_refers_to_b, dist);
                                    count += 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    private boolean isNewNeighbour( int a, int b ) {
        return new_neighbours[a].contains(b);
    }

    private double getDists() {
        double sum = 0;
        for (int i = 0; i < neighbours.length; i++) {
            ConcurrentOrderedList<Integer,Double> dists = neighbours[i];
            for( Double dist : dists.getComparators() ) {
                sum = sum + dist;
            }
        }
        return sum / ( neighbours.length * num_nns );
    }

    public int applyUpdates(ConcurrentHashMap<Integer, ConcurrentOrderedList<Integer, Double>> updates) {
        resetNewNeighbours();
        return IntStream.range(0, neighbours.length).parallel().map( i -> updateI(i,updates) ).sum();
    }

    private int updateI(int i, ConcurrentHashMap<Integer, ConcurrentOrderedList<Integer, Double>> updates) {
        ConcurrentOrderedList<Integer, Double> ol = updates.get(i);
        if( ol != null ) {
            int count = 0;

            List<Integer> indices_of_updates = ol.getList();            // the ids of nodes referenced that we are adding
            List<Double> distances_to_updates = ol.getComparators();    // the distances at which they are being added

            for (int j = 0; j < indices_of_updates.size(); j++) {
                double distance_to_node_being_added = distances_to_updates.get(j);
                if( neighbours[i].size() < num_nns || distance_to_node_being_added < neighbours[i].getThreshold() ) { // onlu update the list if the new distance is closer than those already in the list
                    int id_of_node_being_added = indices_of_updates.get(j);
                    if (!neighbours[i].contains(id_of_node_being_added)) {  // if already in list must be at same distance.
                        neighbours[i].add(id_of_node_being_added, distance_to_node_being_added);
                        new_neighbours[i].add(id_of_node_being_added);
                        count = count + 1;
                    }
                }
            }
            return count;
        }
        return 0;
    }

    private void assignReverseNeighbours() {
        initialiseReverseNeighbours(); // reset the neighbours
        // not parallel races in updates of lists
        for( int i = 0; i < neighbours.length; i++ ) {                       // for all the data points:
            List<Integer> neighbours_of_i = neighbours[i].getList();        // list of neighbours of nodes at index i
            for (int j = 0; j < neighbours_of_i.size(); j++) {              // for each neighbour in the list
                int jth_neighbour_index = neighbours_of_i.get(j);           // id of the jth neighbour of node i
                if (!rev_neighbours[jth_neighbour_index].contains(i)) {     // if i is not in the rev_neighbours already
                    if( rev_neighbours[jth_neighbour_index].size() < num_nns * 1.5 ) {          // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        rev_neighbours[jth_neighbour_index].add(i);             // add it to the reverse neighbours
                    }
                }
            }
        }
    }

    public ConcurrentOrderedList<Integer, Double> knnSearch(metric_type query, int swarm_size, int nn_to_return) {
        int entry_point_index = getEntryPoint();
        return knnSearchInternal( query,entry_point_index,swarm_size,nn_to_return );
    }

    public ConcurrentOrderedList<Integer, Double> exactKnnSearch(metric_type query, int swarm_size, int nn_to_return) {
        ConcurrentOrderedList<Integer, Double> results = new ConcurrentOrderedList<>(nn_to_return);

        for (int i = 0; i < dao.getDataSize(); i++) {
            metric_type p = dao.getData(i);
            double dist = metric.distance(query, p);
            if (results.getThreshold() == null || results.getThreshold() > dist) {
                results.add(i, dist);
            }
        }

        return results;
    }

    /**
     * @param query - the query to perform
     * @param entry_point_index - a starting index into the NN table
     * @param swarm_size - the amount of backtracking required
     * @param nn_to_return - the number of neighbours required
     * @return nn nodes closest to query, this list is ordered with the closest first.
     */
    private ConcurrentOrderedList<Integer, Double> knnSearchInternal(metric_type query, int entry_point_index, int swarm_size, int nn_to_return ) {
//        System.out.println( ">>>>>>> search " + " Distance from entry_point: " +  + metric.distance( query, dao.getData(entry_point_index) ) );
        List<Integer> visitedSet_v = makeListFrom(entry_point_index);
        ConcurrentOrderedList<Integer, Double> candidates = makeOrderedListFrom(query,entry_point_index,BIG_NUMBER);
        ConcurrentOrderedList<Integer, Double> nearest_neighbours = makeOrderedListFrom(query,entry_point_index,swarm_size);
        while( candidates.size() > 0 ) {
            Pair<Integer, Double> nearest_candidate_pair = candidates.remove(0); // get the nearest element to q (sort order for C)
            Pair<Integer, Double> furthest_known_nn_pair = nearest_neighbours.get(nearest_neighbours.size()-1); // get the furthest from q in W
            if( nearest_candidate_pair.Y() > furthest_known_nn_pair.Y() ) {
                break; // end of process
            }
            List<Integer> neighbours_of_nearest_candidate = neighbours[nearest_candidate_pair.X()].getList();
            for( Integer next_neighbour_of_nearest_candidate : neighbours_of_nearest_candidate)  {                                     // update candidates and nns
                if( ! visitedSet_v.contains(next_neighbour_of_nearest_candidate) ){
                    visitedSet_v.add(next_neighbour_of_nearest_candidate);
                    furthest_known_nn_pair = nearest_neighbours.get(nearest_neighbours.size() - 1); // furthest from query in nearest_neighbours
                    double distance_q_next_neighbour = metric.distance( query, dao.getData(next_neighbour_of_nearest_candidate) );
                    double distance_q_furthest_known = metric.distance( query, dao.getData(furthest_known_nn_pair.X()) );
                    if( distance_q_next_neighbour < distance_q_furthest_known || nearest_neighbours.size() < swarm_size ) {
//                        Diagnostic.traceln( "Adding candidate " + e.getId() + " at dist: " + distance_e_q );
                        candidates.add(next_neighbour_of_nearest_candidate,distance_q_next_neighbour);
                        nearest_neighbours.add(next_neighbour_of_nearest_candidate,distance_q_next_neighbour);
                    }
                }
            }
        }

        return nearest_neighbours;
    }


    private List<Integer> makeListFrom(int entryPointIndex) {
        List<Integer> new_list = new ArrayList<>();
        new_list.add(entryPointIndex);
        return new_list;
    }

    private ConcurrentOrderedList<Integer, Double> makeOrderedListFrom(metric_type query, int point_index, int list_size) {
        ConcurrentOrderedList<Integer, Double> ol = new ConcurrentOrderedList<>(list_size);
        double dist = metric.distance(query, dao.getData(point_index));
        ol.add(point_index,dist);
        return ol;
    }

    private int getEntryPoint() {
        return dao.getDataSize() / 2;   // this could be more sophisticated! TODO PROBLEM <<<<<<<<<<<
    }

    public void saveIndex(String saveIndexPath, String saveDistsPath) {
        try (BufferedWriter index_writer = new BufferedWriter(new FileWriter(saveIndexPath));
             BufferedWriter dists_writer = new BufferedWriter(new FileWriter(saveDistsPath))) {
            for (int i = 0; i < neighbours.length; i++) {
                ConcurrentOrderedList<Integer,Double> neebs = neighbours[i];
                List<Double> dists = neebs.getComparators();
                List<Integer> indices = neebs.getList();
                StringBuffer indices_sb = new StringBuffer();
                StringBuffer dists_sb = new StringBuffer();
                for( int nn_index = 0; nn_index < indices.size(); nn_index++ ) {
                    indices_sb.append(indices.get(nn_index));
                    indices_sb.append(" ");
                    dists_sb.append(dists.get(nn_index));
                    dists_sb.append(" ");
                }
                indices_sb.setLength(indices_sb.length() - 1);
                dists_sb.setLength(dists_sb.length() - 1);
                indices_sb.append("\n");
                dists_sb.append("\n");
                index_writer.write(indices_sb.toString());
                dists_writer.write(dists_sb.toString());
            }
            index_writer.flush();
            dists_writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void restoreState(String saveIndexPath, String saveDistsPath, int data_size, int num_nns) {
        // TODO Note for search we could do without the distances and calculate as we go - slower but saves space.
        try (BufferedReader index_reader = new BufferedReader(new FileReader(saveIndexPath));
             BufferedReader dists_reader = new BufferedReader(new FileReader(saveDistsPath)); ){

            for (int i = 0; i < data_size; i++) {
                neighbours[i] = new ConcurrentOrderedList(num_nns);
                String next_index_line = index_reader.readLine();
                String next_dists_line = dists_reader.readLine();
                String[] indices = next_index_line.split(" ");
                String[] dists = next_dists_line.split(" ");
                for( int nn_index = 0; nn_index < Math.min(indices.length,dists.length); nn_index++ ) {
                    int next_index = Integer.parseInt( indices[nn_index] );
                    double next_dist = Double.parseDouble( dists[nn_index] );
                    neighbours[i].add( next_index,next_dist );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
