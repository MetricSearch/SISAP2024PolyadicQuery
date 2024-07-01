package uk.metricSpaceFramework.coreConcepts;

public class CountedMetric<T> implements Metric<T> {

    long count;
    Metric<T> m;

    public CountedMetric(Metric<T> m) {
        this.count = 0;
        this.m = m;
    }

    @Override
    public double distance(T x, T y) {
        this.count++;
        return this.m.distance(x, y);
    }

    public Metric<T> extractMetric() { return m; }

    @Override
    public String getMetricName() {
        return m.getMetricName();
    }

    @Override
    public boolean isBirectional() {
        return m.isBirectional();
    }

    public long reset() {
        long res = this.count;
        this.count = 0;
        return res;
    }

    public long getCount() {
        return count;
    }
}

