package uk.ac.standrews.cs.descent.daos;

import uk.metricSpaceFramework.coreConcepts.Metric;
import uk.metricSpaceFramework.datapoints.CartesianPoint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SynthEucsGroundTruthDataAccessObject implements DataAccessObject<CartesianPoint> {
    private static final String COMMA_DELIMITER = ",";
    private static final String SPACE_DELIMITER = " ";
    private List<CartesianPoint> data;
    private List<List<Integer>> data_nn_indices;
    private List<CartesianPoint> queries;
    private List<List<Double>> data_nn_dists;
    private final List<List<Integer>> query_nn_indices;
    private final List<List<Double>> query_nn_dists;

    private final String data_filename = "src/main/java/uk/ac/standrews/cs/descent/data/euc_data_"; // 10000.csv";
    private final String queries_filename = "src/main/java/uk/ac/standrews/cs/descent/data/euc_queries_"; // 10000.csv";

    private final String data_indices_nns_filename = "src/main/java/uk/ac/standrews/cs/descent/data/data_indices_nns_"; // 10000_100.csv";
    private final String data_dists_nns_filename = "src/main/java/uk/ac/standrews/cs/descent/data/data_dists_nns_"; // 10000_100.csv";

    private final String query_indices_nns_filename = "src/main/java/uk/ac/standrews/cs/descent/data/query_indices_nns_"; // 10000_100.csv";
    private final String query_dists_nns_filename = "src/main/java/uk/ac/standrews/cs/descent/data/query_dists_nns_"; // 10000_100.csv";


    public SynthEucsGroundTruthDataAccessObject(int dims, int dataset_size, int queries_size ) throws IOException {
        data = readData(data_filename + dims + "_" +  dataset_size + ".csv" );
        queries = readData(queries_filename + dims + "_" +  dataset_size + ".csv");
        data_nn_indices = readNNIndices(data_indices_nns_filename + dims + "_" + dataset_size + "_100.csv" );
        data_nn_dists = readNNDistances( data_dists_nns_filename + dims + "_" +  dataset_size + "_100.csv");
        query_nn_indices = readNNIndices(query_indices_nns_filename + dims + "_" +  dataset_size + "_" + queries_size + "_100.csv" );
        query_nn_dists = readNNDistances( query_dists_nns_filename + dims + "_" +  dataset_size + "_" + queries_size +  "_100.csv");

    }

    public Metric<CartesianPoint> metric() {
        return new Metric<CartesianPoint>() {
            @Override
            public double distance(CartesianPoint x, CartesianPoint y) {
                double[] ys = y.getPoint();
                double acc = 0;
                int ptr = 0;
                for (double xVal : x.getPoint()) {
                    final double diff = xVal - ys[ptr++];
                    acc += diff * diff;
                }
                return Math.sqrt(acc);
            }

            @Override
            public String getMetricName() {
                return "Euc";
            }

            @Override
            public boolean isBirectional() {
                return false;
            }
        };
    }

    @Override
    public CartesianPoint getData(int index) {
        return data.get(index);
    }

    public void showData(String s, CartesianPoint p) {
        System.out.print(s);
        System.out.println(Arrays.toString(p.getPoint()));
    }

    public int getDataSize() { return data.size(); }

    public List<CartesianPoint> getData() {
        return data;
    }

    public List<CartesianPoint> getQueries() { return queries; }

    public List<List<Integer>> getDataNNIndices() { return data_nn_indices; }

    public List<List<Double>> getDataNNDists() { return data_nn_dists; }

    public List<List<Integer>> getQueryNNIndices() { return query_nn_indices; }

    public List<List<Double>> getQueryNNDists() { return query_nn_dists; }

    private List<CartesianPoint> readData(String filepath ) throws IOException {
        List<CartesianPoint> data = new ArrayList<>();
        try ( BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                CartesianPoint point = new CartesianPoint(toDoubleArray(values));
                data.add(point);
            }
            return data;
        }
    }

    private List<List<Integer>> readNNIndices(String filepath) throws IOException {
        List<List<Integer>> nns = new ArrayList<>();
        try ( BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(SPACE_DELIMITER);
                List<Integer> next_nns = toInts(values);
                nns.add(next_nns);
            }
            return nns;
        }
    }

    private List<List<Double>> readNNDistances(String filepath) throws IOException {
        List<List<Double>> nns = new ArrayList<>();
        try ( BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(SPACE_DELIMITER);
                List<Double> next_nns = toDoubleList(values);
                nns.add(next_nns);
            }
            return nns;
        }
    }

    private List<Double> toDoubleList(String[] values) {
        List<Double> result = new ArrayList<>();
        for( int i = 0; i < values.length; i++ ) {
            result.add( Double.parseDouble(values[i]) );
        }
        return result;
    }

    private double[] toDoubleArray(String[] strings) {
        double[] doubles = new double[strings.length];
        for( int i = 0; i < strings.length; i++ ) {
            doubles[i] = Double.parseDouble(strings[i]);
        }
        return doubles;
    }

    private List<Integer> toInts(String[] strings) {
        List<Integer> nns = new ArrayList<>();
        for( int i = 0; i < strings.length; i++ ) {
            nns.add( Integer.parseInt( strings[i] ) );
        }
        return nns;
    }
}
