package eu.riscoss.rdc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCOmm implements RDC {

	static Map<String, RDCParameter> parameterMap;

	static {
		parameterMap = new HashMap<String, RDCParameter>();

		parameterMap
				.put("targetOmm",
						new RDCParameter(
								"targetOmm", "OW2 OMM evaluation csv file",
								"http://www.ow2.org/xwiki/bin/download/ActivitiesDashboard/ASM/OMM4RI.ASM.csv",
								null));
	}

	static String[] names = { "omm_PDOC", "omm_STD", "omm_QTP", "omm_LCS", "omm_ENV", "omm_DFCT", 
		"omm_MST", "omm_CM", "omm_PP", "omm_REQM", "omm_RDMP", "omm_STK" };


	Map<String, String> parameters = new HashMap<>();

	public RDCOmm() {
	}

	public Map<String, RiskData> getIndicators(String entity) {

		try {
			return createIndicators(entity);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new HashMap<String, RiskData>();
		}
	}

	public String getName() {
		return "Omm";
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
		return Arrays.asList(names);
	}

	public Map<String, RiskData> createIndicators(String oss_entity)
			throws Exception {

		IndicatorsMap map = new IndicatorsMap(oss_entity);
		BufferedReader br = null;
		HttpEntity entity = null;

		String targetOmm = parameters.get("targetOmm");

		if (targetOmm == null) {
			throw new Exception(String.format("%s property not speficied",
					"targetOmm"));
		}

		try {
			if (targetOmm.toLowerCase().startsWith("http")) {
				HttpClient httpClient = HttpClientBuilder.create().build();
				HttpGet get = new HttpGet(targetOmm);
				HttpResponse response = httpClient.execute(get);

				entity = response.getEntity();
				if (entity != null) {
					//InputStream is = entity.getContent();
					br = new BufferedReader(new InputStreamReader(
							entity.getContent()));

					// EntityUtils.consume(entity); //release all resources held by the httpEntity
				}
				// response.close();
			} else { // local file
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(targetOmm)));
			}

			String line;
			while ((line = br.readLine()) != null) {
				
//				System.out.println("line " + line);
				if (line.contains(",")) {
					String[] parts = line.split(",", 2);
					
					if (parts.length > 1) {
						String ommString = parts[0].trim();
						int ommValue;
						try {
							ommValue = Integer.parseInt(parts[1].trim());
						} catch (NumberFormatException e1) {
							System.err
									.println("Wrong number format for OMM String "
											+ ommString);
							continue;
						}
						RiskData rd = new RiskData(ommString, oss_entity,
								new Date(), RiskDataType.NUMBER, ommValue);
						map.put(ommString, rd);
					}

				}
			}
		} finally {
			if (oss_entity != null) { // http
				EntityUtils.consume(entity); // release all resources held by
												// the httpEntity
				// response.close();
			}
			br.close(); // also if local
		}

		return map;
	}

}
