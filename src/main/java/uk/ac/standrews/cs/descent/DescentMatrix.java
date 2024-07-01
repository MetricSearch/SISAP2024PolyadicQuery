package uk.ac.standrews.cs.descent;

import org.ejml.data.BMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import uk.metricSpaceFramework.coreConcepts.Metric;

import java.util.List;
import java.util.Random;
public class DescentMatrix {

    private List<double[]> data;
    private Metric<double[]> metric;
    private int num_nns;
    private final Random random = new Random(67812345);
    private DMatrixSparseCSC neighbour_dists;   // matrix of neighbour distances
    private BMatrixRMaj neighbours; // matrix of bools - is a a neighbour of b
    private BMatrixRMaj new_neighbours; // matrix of bools - is a a new neighbour of b

    public DescentMatrix(List<double[]> data, Metric<double[]> metric, int num_nns) {
        this.num_nns = num_nns;
        this.metric = metric;
        this.data = data;
        //this.memo_distances = new ConcurrentHashMap<>();
        this.neighbour_dists = new DMatrixSparseCSC(data.size(), data.size());
        this.neighbours = new BMatrixRMaj(data.size(), data.size());
        this.new_neighbours = new BMatrixRMaj(data.size(), data.size());
        randomlyInitialiseNeighbours();
        System.out.println("Initial assignment Ave dist = " + getDists());
    }

    private void randomlyInitialiseNeighbours() {
        for (int row_index = 0; row_index < data.size(); row_index++) {
            for (int i = 0; i < num_nns; i++) {
                int next_rand;
                // make sure we are not assigning a point to itself and it is not a duplicate
                do {
                    next_rand = random.nextInt(data.size());
                }
                while (next_rand == row_index || neighbours.get(row_index, next_rand)); // keep going if iteself or set already
                neighbours.set(row_index, next_rand, true);
                new_neighbours.set(row_index, next_rand, true);
                neighbour_dists.set(row_index, next_rand, metric.distance(data.get(row_index), data.get(next_rand)));
            }
        }
    }

    private double getDists() {
        double sum = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.size(); j++) {
                if (neighbours.get(i, j)) {
                    sum = sum + neighbour_dists.get(i, j);
                }
            }
        }
        return sum / (data.size() * num_nns);
    }
}
