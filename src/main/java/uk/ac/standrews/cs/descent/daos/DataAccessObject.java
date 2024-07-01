package uk.ac.standrews.cs.descent.daos;

import uk.metricSpaceFramework.coreConcepts.Metric;

import java.util.List;

public interface DataAccessObject<metric_type> {
    public Metric<metric_type> metric();
    public metric_type getData(int index );
    public List<metric_type> getAllData();
    public int getDataSize();
    public void showData(String s, metric_type dat);
}
