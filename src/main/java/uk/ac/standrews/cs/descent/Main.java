package uk.ac.standrews.cs.descent;

import uk.ac.standrews.cs.descent.daos.DataAccessObject;
import uk.ac.standrews.cs.descent.daos.EucDinoDataAccessObject;
import uk.ac.standrews.cs.descent.daos.XEntropyDinoDataAccessObject;

import java.io.IOException;

public class Main {

     public static <T> void saveIndex(DataAccessObject<T> dao, int num_nns, String index_path, String dists_path) {
        System.out.println( "Started at " + java.time.LocalDateTime.now() );
        long start_time = System.currentTimeMillis();
        System.out.println("Creating Descent structure with nns = " + num_nns);
        Descent<T> desc = new Descent<>(dao, num_nns);
        long end_time = System.currentTimeMillis();
        System.out.println( "Ended at " + java.time.LocalDateTime.now() );
        System.out.println( "total time = " + ( (end_time - start_time) / 1000.0 ) + "s" );
        desc.saveIndex(index_path, dists_path );
    }

    public static void saveCrossEntropy() throws IOException {
        int dims = 384;
        int num_nns = 100;
        double temp = 1.0;
        String cross_entropy_index = "/Volumes/Data/CE_Dino2_NN100_t" + temp + "_indices.txt";
        String cross_entropy_dists = "/Volumes/Data/CE_Dino2_NN100_t" + temp + "_dists.txt";

        System.out.println("Saving Dino2 Cross Entropy NN (" + num_nns + ") table with " + dims);
        XEntropyDinoDataAccessObject dao = new XEntropyDinoDataAccessObject(dims, temp);
        saveIndex(dao, num_nns, cross_entropy_index, cross_entropy_dists);
    }

    public static void saveEuc() throws IOException {
        int dims = 384;
        int num_nns = 100;
        String euc_index = "/Volumes/Data/EUC_Dino2_NN100_indices.txt";
        String euc_dists =  "/Volumes/Data/EUC_Dino2_NN100_dists.txt";

        System.out.println("Saving Dino2 Euc NN (" + num_nns + ") table with" + dims );
        EucDinoDataAccessObject dao = new EucDinoDataAccessObject(dims);
        saveIndex(dao, num_nns, euc_index, euc_dists);
    }

    public static void main(String[] args) throws IOException {
        // saveEuc();
        saveCrossEntropy();
    }
}