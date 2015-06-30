package eu.riscoss.rdc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.riscoss.dataproviders.Distribution;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCSonar implements RDC {

	static Map<String, RDCParameter> parameterMap;

	static {
		parameterMap = new HashMap<>();

		parameterMap.put("Sonar_resourceKey", new RDCParameter(
				"Sonar_resourceKey", "Sonar Resource Key on the Sonar host", "org.xwiki.platform:xwiki-platform", null));
		parameterMap.put("Sonar_host", new RDCParameter("Sonar_host", "Sonar host address",
				"http://sonar.xwiki.org", null));
		parameterMap.put("Sonar_singleMetrics", new RDCParameter(
				"Sonar_singleMetrics", "Sonar metrics as exposed by the sonar API", "ncloc", "ncloc, duplicated_lines_density, line_coverage, tests"));
		parameterMap.put("Sonar_historyMetrics", new RDCParameter(
				"Sonar_historyMetrics", "Sonar metrics as exposed by the sonar API, returns a list gathered from the available history", "ncloc", "ncloc, comment_lines"));
		parameterMap.put("Sonar_by_file_Metrics", new RDCParameter("Sonar_by_file_Metrics",
				"Sonar metrics as exposed by the sonar API, gathered for each file", "ncloc", "ncloc, complexity"));
	}

	Map<String, String> parameters = new HashMap<>();

	class FormattedParameters {
		String[] singleMetrics;
		String[] historyMetrics;
		String[] fileMetrics;
		String host;
		String resourceKey;
		boolean is_valid = false;

		public FormattedParameters(Map<String, String> map) {
			try {
				host = map.get("Sonar_host");
				if (host == null)
					return;
				resourceKey = map.get("Sonar_resourceKey");
				if (resourceKey == null)
					return;
				singleMetrics = map.get("Sonar_singleMetrics").split(
						"\\s*\\,\\s*");
				historyMetrics = map.get("Sonar_historyMetrics").split(
						"\\s*\\,\\s*");
				String fileMetricString = map.get("");
				if (fileMetricString == null)
					fileMetricString = "ncloc, complexity";
				fileMetrics = fileMetricString.split("\\s*\\,\\s*");
				is_valid = true;
			} catch (Exception ex) {
				is_valid = false;
			}
		}

		public boolean isValid() {
			return is_valid;
		}
	}

	public RDCSonar() {
	}

	@Override
	public Map<String, RiskData> getIndicators(String target) {

		Map<String, RiskData> map = new HashMap<String, RiskData>();

		FormattedParameters params = new FormattedParameters(parameters);

		if (!params.isValid())
			return map;

		// retrieve data for single values and for history
		SonarStatistics statistics = getStatistics(params);
		// retrieve data per class
		ArrayList<ClassInfo> classvalueslist = ClassInfoManager
				.getStatisticsPerClass(params.host, params.resourceKey,
						params.fileMetrics);
		// elaborate per-class data to get the needed indicators

		// *****************************
		// calculate specific indicators
		SonarQuality.createIndicators(statistics, params.fileMetrics,
				classvalueslist);
		// *****************************

		if (statistics != null) {
			for (String key : statistics.singleValues.keySet()) {
				RiskData rd = new RiskData("Sonar " + key, target, new Date(),
						RiskDataType.NUMBER, statistics.singleValues.get(key));
				map.put("Sonar " + key, rd);
				// map.put("Sonar " + key, statistics.singleValues.get(key));
			}

			for (String key : statistics.historyValues.keySet()) {
				List<Double> l = statistics.historyValues.get(key);
				Distribution d = new Distribution();
				d.setValues(l);
				RiskData rd = new RiskData("Sonar History " + key, target,
						new Date(), RiskDataType.DISTRIBUTION, d);
				map.put("Sonar History " + key, rd);
			}
			
		}
		for (RiskData rd: map.values()) {
			System.out.println("Data: "+rd.getId()+" "+rd.getValue());
		}
		return map; //new HashMap<String, RiskData>();
	}

	@Override
	public String getName() {
		return "Sonar";
	}

	@Override
	public Collection<RDCParameter> getParameterList() {
		return parameterMap.values();
	}

	@Override
	public void setParameter(String parName, String parValue) {
		parameters.put(parName, parValue);
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return new ArrayList<String>();
	}

	/**
	 * Gets the measures for every metric in the lists singleMetric and
	 * historyMetric
	 * 
	 * @param host
	 * @param resourceKey
	 * @return
	 */
	protected SonarStatistics getStatistics(FormattedParameters params) {

		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date from = null;
		Date to = null;
		try {
			from = df.parse("01/01/2012");
			Date today = new Date();
			to = today;
		} catch (Exception e) {
			e.printStackTrace();
		}

		SonarStatistics stats = new SonarStatistics();

		for (String m : params.singleMetrics) {
			// value stored inside the method. If not found, no value is stored
			RetrieveStats.getStoreCurrentValue(params.host, params.resourceKey,
					m, stats);
			// stats.singleValues.put(m,
			// RetrieveStats.getCurrentValue(host,resourceKey, m));
		}

		for (String m : params.historyMetrics) {
			ArrayList<Double> val;
			if ((val = RetrieveStats.getHistoricValues(params.host,
					params.resourceKey, m, from, to)) != null)
				stats.historyValues.put(m, val);
		}

		return stats;

	}
}
