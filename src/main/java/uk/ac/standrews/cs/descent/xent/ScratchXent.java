package uk.ac.standrews.cs.descent.xent;

import uk.ac.standrews.cs.descent.daos.XEntropyDinoDataAccessObject;
import uk.metricSpaceFramework.coreConcepts.Metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A XEChecker program to see how good the NN results are.
 */
public class ScratchXent {

    private static List<List<Integer>> pre_calculated_nns = new ArrayList<>();
    private static List<double[]> data;
    private static Metric<double[]> metric;

    private static double getDist( int index1, int index2 ) {
        double[] point1 = data.get(index1);
        double[] point2 = data.get(index2);
        return metric.distance(point1, point2);
    }

    private static void showDist(int index1, int index2) {
        System.out.println( "from : " + index1 + " to " + index2 + " dist: " + getDist(index1,index2) );
    }


    private static void showBothDists(int index1, int index2) {
        showDist(index1, index2);
        showDist(index2, index1);
    }
    public static void main(String[] args) throws IOException {
        int number_data_points = 1_000_000;
        int dims = 384;
        double temp = 1.0;
        System.out.println("Metric drawn from dino2 " + dims + " " + number_data_points);
        XEntropyDinoDataAccessObject context = new XEntropyDinoDataAccessObject(dims,temp);
        data = context.getData();
        metric = context.metric();

        showBothDists(0,29707);
    }

}
