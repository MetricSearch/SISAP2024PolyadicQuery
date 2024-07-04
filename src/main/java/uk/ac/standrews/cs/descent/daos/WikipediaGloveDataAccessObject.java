package uk.ac.standrews.cs.descent.daos;

import uk.ac.standrews.cs.descent.msed.MsedMetric;
import uk.ac.standrews.cs.descent.msed.MsedRep;
import uk.metricSpaceFramework.coreConcepts.Metric;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WikipediaGloveDataAccessObject implements DataAccessObject<MsedRep> {

    private List<double[]> data;
    private List<String> labels;
    private final String wikipedia_glove_dir_root = "data/";
    private final String wikipedia_glove_header_file = wikipedia_glove_dir_root + "rowheaders.txt";
    private final String wikipedia_glove_file = wikipedia_glove_dir_root + "wiki_glove_sm_t10.txt";
    private final int dims = 100; // always 100 says Ben
    private final int num_entries = 400_000; // num wikipedia glove entries
    public WikipediaGloveDataAccessObject() throws IOException {
        System.out.println( "Reading in data files from " + wikipedia_glove_dir_root );
        readData();
        readLabels();
    }

    public Metric<MsedRep> metric() {
        return new MsedMetric();
    }

    @Override
    public MsedRep getData(int index) {
        return new MsedRep( data.get(index) );
    }

    public List<double[]> getData() {
        return data;
    }

    public int getDataSize() { return data.size(); }

    public void showData(String s, MsedRep dat) {
        System.out.print(s + " MSEDREP sum = ");
        System.out.println(Arrays.toString(dat.getVecSum()));
    }

    private void readData() throws IOException {
        data = new ArrayList<double[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(wikipedia_glove_file));) {
            for (int line = 0; line < num_entries; line++) {
                String newLine = br.readLine();
                String[] split_line = newLine.split(" ");
                final double[] dat = new double[dims];
                for (int x = 0; x < dims; x++) {
                    double f = Double.parseDouble(split_line[x]);
                    dat[x] = f;
                }
                data.add( dat );
            }
        }
    }

    private void readLabels() throws IOException {
        labels = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(wikipedia_glove_header_file));) {
            for (int line = 0; line < num_entries; line++) {
                String newLine = br.readLine();
                labels.add( newLine );
            }
        }
    }

    public String getLabel(int i) {
        return labels.get(i);
    }
}
