package eu.riscoss.rdc;

import java.util.List;
import java.util.TreeMap;

public class SonarStatistics {
	public TreeMap<String, Double> singleValues = new TreeMap<String, Double>();
	public TreeMap<String, List<Double>> historyValues = new TreeMap<String, List<Double>>();
}