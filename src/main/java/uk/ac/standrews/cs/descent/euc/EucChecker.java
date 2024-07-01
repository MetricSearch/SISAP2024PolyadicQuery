package uk.ac.standrews.cs.descent.euc;

import uk.ac.standrews.cs.descent.daos.EucDinoDataAccessObject;
import uk.metricSpaceFramework.coreConcepts.Metric;
import uk.metricSpaceFramework.util.ConcurrentOrderedList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A XEChecker program to see how good the NN results are.
 */
public class EucChecker {

    public static final String NNfile = "/Volumes/Data/EUC_Dino2_NN100_indices.txt";

    private static List<List<Integer>> pre_calculated_nns = new ArrayList<>();
    private static List<double[]> data;
    private static Metric<double[]> metric;

    private static List<Integer> getNNFromFile(int i) {
        return pre_calculated_nns.get(i);
    }

    private static void readInPreCalculatedNNs() throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(NNfile))) {
            String next_line;
            while( ( next_line = r.readLine() ) != null ) {
                List<Integer> next_line_of_nns = new ArrayList<>();
                String[] string_ids = next_line.split(" ");
                for( String next_string : string_ids ) {
                    next_line_of_nns.add( Integer.parseInt(next_string) );
                }
                pre_calculated_nns.add(next_line_of_nns);
            }
        }
    }

    private static List<Integer> bruteForceIndices( int relative_to_this_index, int num_nns ) {
        double[] ref_point = data.get(relative_to_this_index);
        ConcurrentOrderedList<Integer,Double> neighbours = new ConcurrentOrderedList<>(num_nns);
        for( int i = 0; i < data.size(); i++ ) {
            if( i != relative_to_this_index ) {
                double dist = metric.distance(ref_point,data.get(i));
                neighbours.add( i,dist );
            }
        }
        return neighbours.getList();
    }

    private static void show(int reference_index, String key, List<Integer> neighbours) {
        System.out.println( key + " : " );
        for( int i : neighbours ) {
            System.out.print(i + " " + " -> " + metric.distance(data.get(reference_index),data.get(i)) + " " );
        }
        System.out.println();
    }

    private static void compareResults() {
        // just check the first few
        for( int i = 0; i < 1; i++ ) {      // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            List<Integer> bf = bruteForceIndices(i, 100);
            List<Integer> calc = getNNFromFile(i);
            show(i, "brute force",bf);
            show(i, "descent",calc);
        }
    }

    public static void main(String[] args) throws IOException {
        readInPreCalculatedNNs();
        int number_data_points = 1_000_000;
        int dims = 384;
        int num_nns = 100;
        System.out.println("Metric drawn from dino2 " + dims + " " + number_data_points+ " Using EUC");
        EucDinoDataAccessObject context = new EucDinoDataAccessObject(dims);
        data = context.getData();
        metric = context.metric();

        compareResults();
    }
}
