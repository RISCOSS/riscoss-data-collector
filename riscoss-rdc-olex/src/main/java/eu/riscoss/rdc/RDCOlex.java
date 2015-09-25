package eu.riscoss.rdc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eu.riscoss.dataproviders.Distribution;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.dataproviders.RiskDataUtils;

public class RDCOlex implements RDC {

	static Map<String, RDCParameter> parameterMap;

	static {
		parameterMap = new HashMap<String, RDCParameter>();

		parameterMap
				.put("licenseFileOlex", new RDCParameter("licenseFileOlex", "", "LicensesOlexCenatic.html", "LicensesOlexCenatic.html"));
		parameterMap.put("targetOlex", new RDCParameter("targetOlex", "OpenLogic OLex page link for the OSS entity",
				"http://olex.openlogic.com/packages/struts", null));
	}

	static String[] names = { "#MIT", "#BSD4", "#BSD3", "#ASL1", "#ASL2", "#Artistic2", "#LGPL2.1", "#LGPL2.1+", "#LGPL3+", "#MPL",
			"#CDDL", "#CPL-EPL", "#EUPL", "#GPL2", "#GPL2+", "#GPL3", "#AGPL3", "#LicenseCount", "licenses", 
			"Target_license", "Equal_licenses", "Number_of_license_types"};

	Map<String, String> parameters = new HashMap<>();
	
	/**
	 * "Distribution" list for all licenses, needed for BN-based models. 
	 */
	private ArrayList<Double> licensesList;
	private ArrayList<Double> equalLicenses;
	
	public RDCOlex() {
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
		return "Olex";
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

	public Map<String, RiskData> createIndicators(String entity) throws Exception {

		IndicatorsMap map = new IndicatorsMap(entity);

		String licenseFile = parameters.get("licenseFileOlex");
		// if (licenseFile == null) // licenseFile = "";
		// licenseFile =
		// RDCFossology.class.getResource("LicensesCfg.html").toString();
		HashMap<String, Collection<String>> licensesMap = parseLicensesFile(licenseFile);

		HashMap<String, Integer> licenseBuckets;

		String targetOlex = parameters.get("targetOlex");
		if (targetOlex == null) {
			throw new Exception(String.format("%s property not speficied", "targetOlex"));
		}
		licenseBuckets = analyseOverviewReport(targetOlex, licensesMap);

		// add all measures to the IndicatorsMap (= Risk Data)
		boolean addAll = true;
		if (addAll)
			for (String licenseBucket : licenseBuckets.keySet()) {
				System.out.println("LB: " + licenseBucket + " \t" + licenseBuckets.get(licenseBucket));
				RiskData rd = new RiskData(licenseBucket, entity, new Date(), RiskDataType.NUMBER, licenseBuckets.get(licenseBucket));
				map.put(licenseBucket, rd);
			}

		
		//new indicators needed for KPA BN (bn_cenatic)
		
		map.put("licenses", new RiskData("licenses", entity, new Date(), RiskDataType.DISTRIBUTION, new Distribution(licensesList)));	
		System.out.println("LB: licenses\t" + licensesList);
		
		int nL = licenseBuckets.get("#LicenseCount");
		//1st slot "One", 2nd slot: many
		Distribution d = new Distribution(nL<=1?1.0:0.0, nL>1?1.0:0.0); 
		map.put("Target_license", new RiskData("Target_license", entity, new Date(), RiskDataType.DISTRIBUTION, d));
		
		d = new Distribution( equalLicenses );
		map.put("Equal_licenses", new RiskData("Equal_licenses", entity, new Date(), RiskDataType.DISTRIBUTION, d));
		
		//additional for BNs
		d = new Distribution(nL<=1?1d:0d, nL==2?1d:0d, nL==3?1d:0d, nL==4?1d:0d, nL>4?1d:0d); //levels defined ad hoc for now!
		//RiskDataUtils.getNumberDistribution(new ArrayList<Double>() , levels)...
		map.put("Number_of_license_types", new RiskData("Number_of_license_types", entity, new Date(), RiskDataType.DISTRIBUTION, d));
				
		//map.put("Type_of_linking",.... can onlz be decided when the component is in use --> user input!

		return map;
	}

	/**
	 * Analyses a fossology html file
	 * 
	 * @param target
	 * @param licensesMap
	 *            parsed license buckets file (html)
	 * @return
	 * @throws IOException
	 */
	private HashMap<String, Integer> analyseOverviewReport(String target, HashMap<String, Collection<String>> licensesMap)
			throws IOException {
		// private static HashMap<String, Integer> analyseFossologyReport(String
		// target, String licenseFile) throws IOException {
		// List<String> result = new ArrayList<String>();
		Document document;

		if (target.startsWith("http")) {
			document = Jsoup.connect(target).get();
		} else {
			File file = new File(target);
			document = Jsoup.parse(file, "UTF-8", "http://localhost");
		}

		List<LicenseEntryOlex> llist = new ArrayList<LicenseEntryOlex>(); // list of licenses in the fossology file
		// Element entry =
		// document.select(":containsOwn(pkg_license_data)").first();
		Element entry = document.select("tr#pkg_license_data > td").first();

		Elements licEntries = entry.select("a");
		for (Element licEntry : licEntries) {
			// using the link to the license to have a stable representation
			// without spaces
			String licStr = licEntry.attr("href");
			if (licStr.startsWith("/licenses/")) {
				String license = licStr.substring(10); // delete leading
														// /licenses/
				System.out.println("License detected: " + license);// for now for single
																	// license only!!
				llist.add(new LicenseEntryOlex(1, license)); // 1: number of occurrences
			}
		}
		// get license type buckets
		LinkedHashMap<String, Integer> licenseBuckets = new LinkedHashMap<String, Integer>();
		// int total=0;

		Set<String> licenseTypes = licensesMap.keySet();
		// initialize with 0 to avoid missing types
		for (String licensetype : licenseTypes) {
			licenseBuckets.put(licensetype, 0);
		}
		
		

		boolean matched = false;
		int numUnknown = 0;
		for (LicenseEntryOlex le : llist) {
			for (String licenseType : licenseTypes) {// cycles on license types
														// from config file
				if (le.matchesOneOf(licensesMap.get(licenseType), licenseType)) {
					Integer currentcount = licenseBuckets.get(le.licensetype);
					if (currentcount == null) // for safety, but should be
												// initialised
						currentcount = 0;
					licenseBuckets.put(le.licensetype, currentcount + le.count);
					matched = true;
					// System.out.println("Matched: "+licensesMap.get(licenseType)+"  "+licenseType+"  "+le);
				}
			}
			// total+=le.count;
			if (matched == false) { // unknown
				numUnknown += le.count;
				System.err.println("Unknown license: " + le.getName());
			}
		}
		
		// Create a "Distribution" list for all licenses, needed for BN-based models. 
		// Sort order as defined in LicensesOlexCenatic.html
		// at this moment, only the licenses, in correct order, are in the licenseBuckets map!
		licensesList = new ArrayList<Double>();
		equalLicenses = new ArrayList<Double>();
		double[] equalLic = new double[5];
		int equalLicCount = 0;
		for (String key : licenseBuckets.keySet()) {
			Integer value = licenseBuckets.get(key);
			licensesList.add(value.doubleValue()/llist.size());
			
			//equal licenses: 
			//s1: # none equal, s2: # 2 equal, s3: # 3 equal, s4: # 4 equal, s5: # >4 equal
			if (value>0) {
				if (value <= 4){
					equalLic[value-1]++;
				} else { //if value > 4
					equalLic[4]++;
				}
				equalLicCount++;
			}
		}
		
		for (double d : equalLic) {
			equalLicenses.add(d/equalLicCount); //attention: order is important!
		}
		
		licenseBuckets.put("#UnknownLicensesCount", numUnknown);
		
		licenseBuckets.put("#LicenseCount", llist.size());
		
		return licenseBuckets;
	}

	//
	// private String getLicenseTypeForLicense(HashMap<String,
	// Collection<String>> licensesMap, String license) {
	// for (String l : licensesMap.keySet()) {
	// //if (license.toLowerCase().contains(l.toLowerCase())) {
	// //attention: order matters in the file! (e.g. to parse GPL/LGPL
	// correctly)
	// if (matchesOneOf(licensesMap.get(l), license))
	// return l;
	// }
	// return "_unknown_";
	// }

	// private boolean matchesOneOf(Collection<String> si, String license){
	// for (String string : si) {
	// if (license.startsWith(string)){
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * Parses a LicensesCfg file
	 * 
	 * @param target
	 * @return HashMap: License Types, each with a Collection of Licenses
	 * @throws IOException
	 */
	protected static HashMap<String, Collection<String>> parseLicensesFile(String target) throws IOException {
		HashMap<String, Collection<String>> result = new LinkedHashMap<String, Collection<String>>();
		Document document;
		if (target.startsWith("http")) {
			document = Jsoup.connect(target).get();
		} else {
			if (target.startsWith("file:"))
				target = target.substring(5);

			// File file = new File(target);
			InputStream in = RDCOlex.class.getResourceAsStream("res/" + target);
			// System.out.println("Fossology config file used: "+file.getPath());
			// System.out.println("Fossology IS file used: "+in.toString());

			document = Jsoup.parse(in, "UTF-8", "http://localhost");

		}

		Elements licensesLinks = document.getElementsByAttribute("id");

		for (Element element : licensesLinks) {
			String licenseName = element.attr("id");// element.child(0).text();
			// System.out.println(element.text());
			String s = element.text(); // with or without <p> tags
			Collection<String> licensesList = Arrays.asList(s.split("\\s*\\|\\s*"));
			result.put(licenseName, licensesList);

		}
		return result;
	}

}
