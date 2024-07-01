package uk.ac.standrews.cs.descent.msed;

import java.io.Serializable;
import java.util.List;

public class MsedRep implements Serializable {
    private int n;
    private double[] vecSum;
    private double compProduct;

    public MsedRep(List<double[]> query) {
        this.n = query.size();
        this.vecSum = vector_sum(query);
        this.compProduct = 1;
        for( double[] fv : query){
            compProduct *= complexity(fv);
        }
    }

    public MsedRep(double[] v) {
        this.n = 1;
        this.vecSum = v;
        this.compProduct = complexity(v);
    }

    public static double[] vector_sum(List<double[]> query) {
        double[] res = new double[query.get(0).length];
        for (double[] v : query) {
            for (int i = 0; i < v.length; i++) {
                res[i] = res[i] + v[i];
            }
        }
        return res;
    }

    public static double complexity(double[] v) {
        double acc = 0;
        for (double f : v) {
            acc -= f != 0? f * Math.log(f):0;
        }
        return Math.exp(acc);
    }

    public int getN() {
        return this.n;
    }

    public double[] getVecSum() {
        return this.vecSum;
    }

    public double getCompProduct() {
        return this.compProduct;
    }
}
