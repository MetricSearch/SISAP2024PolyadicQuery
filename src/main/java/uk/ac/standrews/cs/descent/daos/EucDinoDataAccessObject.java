package uk.ac.standrews.cs.descent.daos;

import uk.metricSpaceFramework.coreConcepts.Metric;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EucDinoDataAccessObject implements DataAccessObject<double[]> {

    private static final int NUM_TEXT_FILES = 1000;
    private List<double[]> data;
    public final double TEMP = 5;
    private final String dino_dir_root = "/Volumes/Data/mf_dino2_text/";

    public EucDinoDataAccessObject(int dims ) throws IOException {
        System.out.println( "Reading in " + NUM_TEXT_FILES + " data files");
        data = new ArrayList<>();
        readData( dims );
    }

    public Metric<double[]> metric() {
        return new Metric<double[]>() {
            @Override
            public double distance(double[] xs, double[] ys) {
                double acc = 0;
                int ptr = 0;
                for (double xVal : xs) {
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
                return true;
            }
        };
    }

    @Override
    public double[] getData(int index) {
        return data.get(index);
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
            readDataFile( dino_dir_root, file_num, dimensions, data );
            System.out.println("Read file " + file_num);
        }
    }

    public void readDataFile(String pathroot, int fileNumber, int dimensions, List<double[]> index ) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(pathroot + fileNumber + ".txt"));) {
            for (int line = 0; line < NUM_TEXT_FILES; line++) {
                String newLine = br.readLine();
                String[] data = newLine.split("\\s+");
                final double[] dat = new double[dimensions];
                for (int x = 0; x < dimensions; x++) {
                    double f = Double.parseDouble(data[x]);
                    dat[x] = f;
                }
                index.add( dat );
            }
        }
    }
}
