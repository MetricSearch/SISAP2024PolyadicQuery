package uk.ac.standrews.cs.descent.daos;

import uk.metricSpaceFramework.coreConcepts.Metric;

public interface DataAccessObject<metric_type> {
    public Metric<metric_type> metric();
    public metric_type getData(int index );
    public int getDataSize();
    public void showData(String s, metric_type dat);
}
