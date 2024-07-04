package uk.ac.standrews.cs.descent.bugattis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bugattis {

    public static int dimensions = 384;

    public static List<double[]> getSMBugattis() throws IOException {
        List<double[]> smaxed = new ArrayList<>();
        List<double[]> bugs = getBugattis();
        for( double[] dino_rep : bugs ) {
            smaxed.add( softmax( dino_rep, 10 ) );
        }
        return smaxed;
    }

    private static List<double[]> getBugattis() throws IOException {
        List<double[]> res = new ArrayList<>();
        String next_line;
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("/Volumes/Data/bugatti_not_in_mf/embeddings/embeddings.txt"))) {
            while ((next_line = br.readLine()) != null) {
                String[] data = next_line.split("\\s+");
                final double[] dat = new double[dimensions];
                for (int x = 0; x < dimensions; x++) {
                    double f = Double.parseDouble(data[x]);
                    dat[x] = f;
                }
                res.add(dat);
            }
        }
        return res;
    }

    private static double[] softmax(double[] vec, double temperature) {
        double[] fs = new double[vec.length];
        double acc = 0;
        for (int i = 0; i < vec.length; i++) {
            double term = Math.exp(vec[i] / temperature);
            acc += term;
            fs[i] = (double) term;
        }
        for (int i = 0; i < vec.length; i++) {
            fs[i] = fs[i] / acc;
        }
        return fs;
    }

}
