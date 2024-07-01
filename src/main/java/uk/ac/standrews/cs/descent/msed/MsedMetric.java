package uk.ac.standrews.cs.descent.msed;

import uk.metricSpaceFramework.coreConcepts.Metric;

public class MsedMetric implements Metric<MsedRep> {
    @Override
    public double distance(MsedRep u, MsedRep v) {
        int nSum = u.getN() + v.getN();
        double[] v1 = vector_mean(u.getVecSum(), v.getVecSum(), nSum);
        double comp = MsedRep.complexity(v1);

        double prod = u.getCompProduct() * v.getCompProduct();
        double root = Math.pow(prod, 1.0 / nSum);
        return ((comp / root - 1)) / (nSum - 1);
    }

    @Override
    public String getMetricName() {
        return "MSED";
    }

    @Override
    public boolean isBirectional() {
        return true;
    }

    private double[] vector_mean(double[] v, double[] w, int nSum) {
        double[] res = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            res[i] = (v[i] + w[i]) / nSum;
        }
        return res;
    }
}
