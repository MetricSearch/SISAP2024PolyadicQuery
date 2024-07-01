package uk.ac.standrews.cs.descent.sed;

import uk.ac.standrews.cs.descent.Descent;
import uk.ac.standrews.cs.descent.daos.WikipediaGloveDataAccessObject;
import uk.ac.standrews.cs.descent.msed.MsedRep;
import uk.metricSpaceFramework.coreConcepts.Metric;
import uk.metricSpaceFramework.util.ConcurrentOrderedList;

import java.io.IOException;
import java.util.List;

public class MainSearchSED {
    public static void main(String[] args) throws IOException {
        System.out.println("Started at " + java.time.LocalDateTime.now());
        long start_time = System.currentTimeMillis();
        int num_nns = 100;
        System.out.println("Metric drawn from Wikipedia/Glove/SED ");
        WikipediaGloveDataAccessObject context = new WikipediaGloveDataAccessObject();
        List<double[]> data = context.getData();
        Metric<MsedRep> metric = context.metric();
        int bear = 3045;
        int tennis = 2140;
        int sheep = 7375;

        String restore_indices_path = "/Volumes/Data/wiki_glove_sed_nn_table_dong/wiki_glove_sed_nns_sm_t10_indices.txt";
        String restore_dists_path = "/Volumes/Data/wiki_glove_sed_nn_table_dong/wiki_glove_sed_nns_sm_t10_dists.txt";

        System.out.println("Restoring Descent structure nns = " + num_nns + " from " + restore_indices_path + " and " + restore_dists_path);

        Descent desc = new Descent(context, restore_indices_path, restore_dists_path, num_nns);

        ConcurrentOrderedList<Integer, Double> results = desc.knnSearch(context.getData(sheep), 200, 200);

        List<Integer> ids = results.getList();
        List<Double> dists = results.getComparators();

        System.out.println("Results:");
        for (int i = 0; i < ids.size(); i++) {
            System.out.println(i + ": " + context.getLabel(ids.get(i)) + ": " + ids.get(i) + " -> " + dists.get(i));
        }

//        long end_time = System.currentTimeMillis();
//        System.out.println( "Ended at " + java.time.LocalDateTime.now() );
//        System.out.println( "total time = " + ( (end_time - start_time) / 1000.0 ) + "s" );
    }

}