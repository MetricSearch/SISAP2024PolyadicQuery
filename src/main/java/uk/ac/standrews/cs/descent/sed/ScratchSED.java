package uk.ac.standrews.cs.descent.sed;

import uk.ac.standrews.cs.descent.daos.WikipediaGloveDataAccessObject;
import uk.ac.standrews.cs.descent.msed.MsedRep;
import uk.metricSpaceFramework.coreConcepts.Metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A XEChecker program to see how good the NN results are.
 */
public class ScratchSED {

    private static List<List<Integer>> pre_calculated_nns = new ArrayList<>();
    private static List<double[]> data;
    private static Metric<MsedRep> metric;

    private static double getDist( int index1, int index2 ) {
        MsedRep point1 = new MsedRep(data.get(index1));
        MsedRep point2 = new MsedRep(data.get(index2));
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
        System.out.println("Metric drawn from SED on Wikpedia data");
        WikipediaGloveDataAccessObject context = new WikipediaGloveDataAccessObject();
        data = context.getData();
        metric = context.metric();

        showBothDists(1,2);
    }

}
