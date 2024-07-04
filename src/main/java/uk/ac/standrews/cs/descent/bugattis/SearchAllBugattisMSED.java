package uk.ac.standrews.cs.descent.bugattis;

import uk.ac.standrews.cs.descent.Descent;
import uk.ac.standrews.cs.descent.daos.SEDDinoSMDataAccessObject;
import uk.ac.standrews.cs.descent.msed.MsedRep;
import uk.metricSpaceFramework.util.ConcurrentOrderedList;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchAllBugattisMSED {

    private static String matlab_nn_path = "/Volumes/Data/mf_dino2_sed_nn_table_dong/mf_dino2_sed_nns_sm_t10.mat";
    // see Python code (make_sed_nns.py) for constructing this file.
    private static final int DATA_SIZE = 1_000_000;
    private static final int NEIGHBOURS = 100;
    public static void main(String[] args) throws IOException {
        System.out.println("Started at " + java.time.LocalDateTime.now());

        int check_this_number = 20;
        int num_results_required = 100;
        int swarm_size = 100;
        int num_nns = 20; // was 100
        int dims = 384;
        double temp = 10.0;
        System.out.println("Metric drawn from dino2 " + dims);
        SEDDinoSMDataAccessObject context = new SEDDinoSMDataAccessObject(dims, temp);

        System.out.println("Restoring Descent structure with temp = " + temp + " and nns = " + num_nns + " from " + matlab_nn_path);

        ConcurrentOrderedList<Integer, Double>[] nns = getMatlabNNTable(matlab_nn_path);

        List<double[]> bugattis = Bugattis.getSMBugattis(); // sm bugattis

        Descent desc = new Descent(context, num_nns);

        List<Integer> known_bugatti_ids = KnownBugattis.known_bugatti_ids;

        // to warm up the code...
        MsedRep bug_msed = new MsedRep(bugattis.get(1));
        ConcurrentOrderedList<Integer, Double> results = desc.knnSearch(bug_msed, swarm_size, num_results_required);

        doExperiment( desc, known_bugatti_ids,"MSED(1)", new SingleBugattiMSedFactory(bugattis), bugattis, swarm_size,num_results_required, check_this_number);
        for( int i = 2; i < bugattis.size(); i++ ) {
            doExperiment(desc, known_bugatti_ids, "MSED(" + i + ")", new MulipleBugattiMSedFactory(bugattis, i), bugattis, swarm_size, num_results_required, check_this_number);
        }
    }

    public static void doExperiment(Descent desc, List<Integer> known_bugatti_ids, String label, BugattiMSedFactory factory, List<double[]> bugattis, int swarm_size, int num_results_required, int check_this_number) {
        long end_time = 0;
        MsedRep bug_msed;
        int counter = 0;
        List<Integer> times = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        int sum = 0;
        for( int i = 0; i < factory.getSize(); i++ ) {
            bug_msed = factory.makeRep(i);
            List<Integer> perm = factory.getPerm(i);
            counter = counter +1;
            long search_start_time = System.currentTimeMillis();
            ConcurrentOrderedList results = desc.knnSearch(bug_msed, swarm_size, num_results_required);
            end_time = System.currentTimeMillis();
            List<Integer> ids = results.getList();
            List<Double> dists = results.getComparators();

            int count = 0;
            for (int j = 0; j < check_this_number; j++) {
                if( known_bugatti_ids.contains(ids.get(j) ) ) {
                    count++;
                }
            }
            show_result( perm, count );

            times.add( (int) ( end_time - search_start_time ) );
            counts.add( count );
        }
        showStats( label, times, counts );
    }

    private static void show_result(List<Integer> perm, int count) {
        for( int i = 0; i < perm.size(); i++ ) {
            System.out.print( perm.get(i) + " " );
        }
        System.out.println( ": " + count );
    }

    private static void showStats( String label, List<Integer> times, List<Integer> counts) {
        double mean_time = times.stream().mapToDouble(Double::valueOf).average().orElse(0);
        double mean_count = counts.stream().mapToDouble(Double::valueOf).average().orElse(0);

        double time_stddev = stdev( mean_time,times );
        double count_stddev = stdev( mean_count,counts );

        System.out.println( label + " queries = " + counts.size() +
                        " mean found = " + mean_count +
                        " stdev found " +  count_stddev +
                " mean time = " + mean_time +
                " stdev time = " + time_stddev );
    }

    private static double stdev(double meanTime, List<Integer> times) {
        double acc = 0d;

        for (int i = 0; i < times.size(); i++) {
            acc += Math.pow(times.get(i) - meanTime, 2);
        }

        return Math.sqrt(acc / (times.size() - 1));
    }

    private static ConcurrentOrderedList<Integer, Double>[] getMatlabNNTable(String matlabNnPath) throws IOException {
        Mat5File data = Mat5.readFromFile(matlabNnPath);

        Matrix indicies = data.getMatrix("indexes");
        Matrix distances = data.getMatrix("distances");

        ConcurrentOrderedList<Integer, Double>[] neighbours = new ConcurrentOrderedList[DATA_SIZE];

        for (int i = 0; i < DATA_SIZE; i++) {
            neighbours[i] = new ConcurrentOrderedList<>(NEIGHBOURS);
            for (int neighbour_idx = 0; neighbour_idx < NEIGHBOURS; neighbour_idx++) {
                int idx = indicies.getInt(i, neighbour_idx);
                double dist = distances.getDouble(i, neighbour_idx);

                neighbours[i].add(idx, dist);
            }
        }

        return neighbours;
    }
}
