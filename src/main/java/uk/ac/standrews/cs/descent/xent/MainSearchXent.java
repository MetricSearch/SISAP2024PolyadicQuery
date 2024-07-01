package uk.ac.standrews.cs.descent.xent;

import uk.ac.standrews.cs.descent.Descent;
import uk.ac.standrews.cs.descent.daos.XEntropyDinoDataAccessObject;
import uk.metricSpaceFramework.coreConcepts.Metric;
import uk.metricSpaceFramework.util.ConcurrentOrderedList;

import java.io.IOException;
import java.util.List;

public class MainSearchXent {

    public static void main(String[] args) throws IOException {
        System.out.println( "Started at " + java.time.LocalDateTime.now() );
        long start_time = System.currentTimeMillis();
        int dims = 384;
        int num_nns = 100;
        double temp = 100.0;
        System.out.println("Metric drawn from dino2 " + dims );
        XEntropyDinoDataAccessObject context = new XEntropyDinoDataAccessObject(dims,temp);
        List<double[]> data = context.getData();
        Metric<double[]> metric = context.metric();
        String restore_indices_path = "/Volumes/Data/CE_Dino2_NN100_t"+ temp + "_indices.txt";
        String restore_dists_path = "/Volumes/Data/CE_Dino2_NN100_t" + temp + "_dists.txt";

        System.out.println("Restoring Descent structure with temp = " + temp + " and nns = " + num_nns + " from " + restore_indices_path + " and " + restore_dists_path );

        Descent desc = new Descent(context, restore_indices_path, restore_dists_path, num_nns);

        ConcurrentOrderedList<Integer, Double> results = desc.knnSearch(data.get(0), 200, 200);

        List<Integer> ids = results.getList();
        List<Double> dists = results.getComparators();

        System.out.println( "Results:" );
        for( int i = 0; i < ids.size(); i++ ) {
            System.out.println( i + ": " + ids.get(i) + " -> " + dists.get(i) );
        }


//        long end_time = System.currentTimeMillis();
//        System.out.println( "Ended at " + java.time.LocalDateTime.now() );
//        System.out.println( "total time = " + ( (end_time - start_time) / 1000.0 ) + "s" );
    }

}