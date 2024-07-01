package uk.ac.standrews.cs.descent;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;

import java.io.IOException;
import java.util.ArrayList;

public class MatLabData {

    private static final int DATA_POINTS_PER_FILE = 10_000;

    static ArrayList<double[]> loadMat(String path) throws IOException {
            Mat5File data = Mat5.readFromFile(path);
            Matrix features;

            try {
                features = data.getMatrix("probVecs");
            } catch (IllegalArgumentException e) {
                features = data.getMatrix("features");
            }


            ArrayList<double[]> listData = new ArrayList<>();
            int dimensions = features.getNumCols();

            for (int row = 0; row < DATA_POINTS_PER_FILE; row++) {
                double[] point = new double[features.getNumCols()];
                for (int col = 0; col < dimensions; col++) {
                    point[col] = features.getDouble(row, col);
                }
                listData.add(point);
            }

            return listData;
        }
}
