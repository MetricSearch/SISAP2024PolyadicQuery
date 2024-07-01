package uk.metricSpaceFramework.util;

import java.util.List;

public class Util {

	private static final String TAB = "\t";

	public static double square(double x) {
		return x * x;
	}

	public static double mean(List<Double> list) {
		if (list.size() == 0) {
			throw new RuntimeException("Empty list");
		}
		double result = 0;
		for (double d : list) {
			result = result + d;
		}
		return ((double) result) / list.size();
	}

	public static double max(List<Double> list) {
		if (list.size() == 0) {
			throw new RuntimeException("Empty list");
		}
		double max_so_far = Double.MIN_VALUE;
		for (double d : list) {
			// note - Richard - Math.max is faster than this... prob doesn't matter here but
			// interesting to note!
			max_so_far = Math.max(d,max_so_far);
		}
		return max_so_far;
	}

	public static double min(List<Double> list) {
		if (list.size() == 0) {
			throw new RuntimeException("Empty list");
		}
		double min_so_far = Double.MAX_VALUE;
		for (double d : list) {
			min_so_far = Math.min(d,min_so_far);
		}
		return min_so_far;
	}

	public static double max(double[] list) {
		if (list.length == 0) {
			throw new RuntimeException("Empty list");
		}
		double max_so_far = Double.MIN_VALUE;
		for (double d : list) {
			max_so_far = Math.max(d,max_so_far);
		}
		return max_so_far;
	}

	public static double min(double[] list) {
		if (list.length == 0) {
			throw new RuntimeException("Empty list");
		}
		double min_so_far = Double.MAX_VALUE;
		for (double d : list) {
			min_so_far = Math.min(d,min_so_far);
		}
		return min_so_far;
	}

	public static double sum(List<Double> list) {
		if (list.size() == 0) {
			throw new RuntimeException("Empty list");
		}
		double result = 0;
		for (double d : list) {
			result = result + d;
		}
		return result;
	}

	public static double stddev(List<Double> list, double mean) {
		if (list.size() == 0) {
			throw new RuntimeException("Empty list");
		}
		double sd = 0;
		for (double d : list) {
			sd += Math.pow(d - mean, 2);
		}
		return Math.sqrt(sd / list.size());
	}

	public static double iDIMChavez(double mean, double std_dev) {
		return square(mean) / (2 * square(std_dev));
	}

	public static double iDIMChavez(List<Double> dists) throws Exception {
		if (dists.size() > 0) {
			double mean = (double) Util.mean(dists);
			double std_dev = Util.stddev(dists, mean);
			return iDIMChavez(mean, std_dev);
		} else {
			throw new Exception("zero length list");
		}
	}

	/**
	 * MLE IDIM Estimator by Amsaleg et al. in paper "Estimating Local Intrinsic
	 * Dimensionality". 2015.
	 *
	 * @param dists - a list of distances.
	 * @return the MLE IDIM of the list
	 * @throws Exception if a zero length list is supplied.
	 */
	public static double LIDimAmsaleg(List<Double> dists) throws Exception {
		int count = dists.size();
		if (count > 0) {
			double mean = mean(dists);
			double sum = sum(dists);
			double max = max(dists);
			double weight = max;
			double one_over_n = (1.0d / (double) count);

			// System.out.println("count = " + count + " s =" + sum + " w = " + weight + " tot = " + weight * count);
			double sigma = 0;

			for (double d : dists) {
				sigma += Math.log(d / weight);
			}
			return -1.0d / (one_over_n * sigma);
		} else {
			throw new Exception("zero length list");
		}
	}

	/**
	 * MLE IDIM Estimator by Levina and Bickel in paper "Maximum Likelihood
	 * Estimation of Intrinsic Dimension".
	 *
	 * @param dists - a list of distances.
	 * @return the MLE IDIM of the list
	 * @throws Exception if a zero length list is supplied.
	 */
	public static double LIDimLevinaBickel(List<Double> dists) throws Exception {
		int count = dists.size();
		double one_over_k_minus_one = 1.0d / ((double) count - 1.0d);

		if (count > 0) {
			double max = max(dists);

			double sigma = 0; // sum

			for (int i = 0; i < count - 1; i++) {
				double d = dists.get(i);
				sigma += Math.log(max / d);
			}
			return 1.0d / (one_over_k_minus_one * sigma);
		} else {
			throw new Exception("zero length list");
		}
	}

	public static double LIDimLevinaBickel(double[] dists) throws Exception {
		int count = dists.length;
		double one_over_k_minus_one = 1.0d / ((double) count - 1.0d);

		if (count > 0) {
			double max = max(dists);

			double sigma = 0; // sum

			for (int i = 0; i < count - 1; i++) {
				double d = dists[i];
				sigma += Math.log(max / d);
			}
			return 1.0d / (one_over_k_minus_one * sigma);
		} else {
			throw new Exception("zero length list");
		}
	}

}