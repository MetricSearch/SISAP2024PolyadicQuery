package uk.ac.standrews.cs.descent.daos;

import uk.metricSpaceFramework.coreConcepts.Metric;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XEntropyDinoDataAccessObject implements DataAccessObject<double[]> {

    private static final int NUM_TEXT_FILES = 1000;
    private List<double[]> data;
    public final double temp;

    private final String dino_dir_root = "/Volumes/Data/mf_dino2_text/";

    public XEntropyDinoDataAccessObject( int dims, double temp ) throws IOException {
        System.out.println( "Reading in " + NUM_TEXT_FILES + " data files");
        this.temp = temp;
        data = new ArrayList<>();
        readData( dims );
    }

    public Metric<double[]> metric() {
        return new Metric<double[]>() {
            @Override
            // Matlab: CED = @(X,Y) - sum(X .* log(Y),2);
            public double distance(double[] xs, double[] ys) {
                double acc = 0;
                for (int i = 0; i < xs.length; i++) {
                    if (ys[i] == 0 || xs[i] == 0)
                        continue;
                    acc -= xs[i] * Math.log(ys[i]);
                }
                return acc;
            }

            @Override
            public String getMetricName() {
                return "Xent";
            }

            @Override
            public boolean isBirectional() {
                return false;
            }


        };
    }

    private static double[] softmax(double[] vec, double temperature) {
        double[] fs = new double[vec.length];
        double acc = 0;
        for (int i = 0; i < vec.length; i++) {
            double term = Math.exp(vec[i] / temperature);
            acc += term;
            fs[i] = term;
        }
        // norming the data:
        for (int i = 0; i < vec.length; i++) {
            fs[i] = fs[i] / acc;
        }
        return fs;
    }

    @Override
    public double[] getData(int index) {
        return data.get(index);
    }

    @Override
    public List<double[]> getAllData() {
        return data;
    }

    public List<double[]> getData() {
        return data;
    }

    public int getDataSize() { return data.size(); }

    public void showData(String s, double[] dat) {
        System.out.print(s);
        System.out.println(Arrays.toString(dat));
    }

    private void readData( int dimensions ) throws IOException {
        for( int file_num = 0; file_num < NUM_TEXT_FILES; file_num++ ) {
            readDataFileSoftmax( dino_dir_root, file_num, dimensions, data, temp);
            System.out.println("Read file " + file_num);
        }
    }

    public void readDataFileSoftmax(String pathroot, int fileNumber, int dimensions, List<double[]> index, double temp ) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(pathroot + fileNumber + ".txt"));) {
            for (int line = 0; line < NUM_TEXT_FILES; line++) {
                String newLine = br.readLine();
                String[] data = newLine.split("\\s+");
                final double[] dat = new double[dimensions];
                for (int x = 0; x < dimensions; x++) {
                    double f = Double.parseDouble(data[x]);
                    dat[x] = f;
                }
                index.add( softmax(dat, temp) );
            }
        }
    }
}
