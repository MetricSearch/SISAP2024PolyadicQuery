package uk.ac.standrews.cs.descent.euc;

import uk.ac.standrews.cs.descent.daos.SynthEucsGroundTruthDataAccessObject;
import uk.metricSpaceFramework.datapoints.CartesianPoint;

import java.io.IOException;
import java.util.List;

public class ShowEucGT {

    public ShowEucGT(SynthEucsGroundTruthDataAccessObject context) {
        List<CartesianPoint> data = context.getData();
        List<List<Double>> dists = context.getDataNNDists();
        List<List<Integer>> indices = context.getDataNNIndices();
        int num_nns = dists.get(0).size();
        double aver_dists = getDists(data, dists, num_nns);
        System.out.println( "Average dists to " + num_nns + " :  " + aver_dists );
    }

    private double getDists(List<CartesianPoint> data,  List<List<Double>> dists, int num_nns ) {
        double sum = 0;
        for (int i = 0; i < data.size(); i++) {
            for( Double dist : dists.get(i) ) {
                sum = sum + dist;
            }
        }
        return sum / ( data.size() * num_nns );
    }

    public static void main(String[] args) throws IOException {
        int number_data_points = 100_000;
        int dims = 20;
        int number_query_points = 1000;
        int num_nns = 100;
        System.out.println( "Metric drawn from euc" + dims + " " + number_data_points);
        SynthEucsGroundTruthDataAccessObject context = new SynthEucsGroundTruthDataAccessObject(dims, number_data_points, number_query_points);
        new ShowEucGT(context);
    }
}
