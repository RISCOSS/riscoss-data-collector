package eu.riscoss.rdc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCFossology implements RDC {

	static Map<String,RDCParameter> parameterMap;

	static {
		parameterMap = new HashMap<String,RDCParameter>();

		parameterMap.put( "licenseFile",
				new RDCParameter( "licenseFile", "", "LicensesCfg.html", "LicensesCfg.html") );
		parameterMap.put( "targetFossology",
				new RDCParameter( "targetFossology", "", "http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002", null ) );
		parameterMap.put( "fossologyScanType",
				new RDCParameter( "fossologyScanType", "'overview' or 'filelist'; default: 'overview'", "overview", "overview" ) );
		parameterMap.put( "targetFossologyList",
				new RDCParameter( "targetFossologyList", "", "http://fossology.ow2.org/?mod=license-list&upload=38&item=292002&output=dltext", null ) );
		parameterMap.put( "fossologyFilterExtensions",
				new RDCParameter( "fossologyFilterExtensions", "", "true", "true" ) );
		parameterMap.put( "fossologyAcceptedExtensions",
				new RDCParameter( "fossologyAcceptedExtensions", "", "java,cpp,jj,js,jsp,php,py,jape,aj,jspf,jsb,groovy,rb,gemspec,c,h", "java,cpp,jj,js,jsp,php,py,jape,aj,jspf,jsb,groovy,rb,gemspec,c,h" ) );
	}

	static String[] names = {
		"number-of-different-licenses",
		"percentage-of-files-without-license",
		"files-with-unknown-license",
		"copyleft-licenses",
		"copyleft-licenses-with-linking",
		"percentage-of-files-with-permissive-license",
		"files-with-commercial-license",
		"percentage-of-files-with-public-domain-license",
		"percentage-of-files-with-multiple-license",
		"number-of-files-analysed"
	};



	Map<String,String> parameters = new HashMap<>();

	public RDCFossology() {
	}

	public Map<String,RiskData> getIndicators( String entity ) {

		try {
			return createIndicators( entity );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			return new HashMap<String,RiskData>();
		}

	}

	public String getName() {
		return "Fossology";
	}

	@Override
	public Collection<RDCParameter> getParameterList() {
		return parameterMap.values();
	}

	@Override
	public void setParameter( String parName, String parValue ) {
		parameters.put( parName, parValue );
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return Arrays.asList( names );
	}

	public Map<String,RiskData> createIndicators( String entity ) throws Exception {

		IndicatorsMap map = new IndicatorsMap( entity );

		String scanType = parameters.get( "fossologyScanType" );
		if (scanType == null)
			scanType = "overview"; //default value as it was the only one in prior versions

		String acceptedExtensionsString = parameters.get( "fossologyAcceptedExtensions" );
		if( acceptedExtensionsString == null ) acceptedExtensionsString = "";
		String[] acceptedExtensions = acceptedExtensionsString.split(",");

		if( "true".equalsIgnoreCase( parameters.get( "fossologyFilterExtensions" ) ) ) {
			acceptedExtensions = new String[0]; //empty the extensions list --> behaviour: accept all extensions
		}

		String licenseFile = parameters.get( "licenseFile" );
//		if (licenseFile == null) // licenseFile = "";
//			licenseFile = RDCFossology.class.getResource("LicensesCfg.html").toString();
		HashMap<String, Collection<String>> licensesMap = parseLicensesFile( licenseFile );

		HashMap<String, Integer> licenseBuckets;
		if (scanType.equals("filelist")){
			String targetFossologyTxt = parameters.get( "targetFossologyList" );
			if (targetFossologyTxt == null) {
				throw new Exception(String.format("%s property not speficied", "targetFossologyList" ));
			}
			licenseBuckets = analyseFileList( targetFossologyTxt, licensesMap, acceptedExtensions );
		}
		else { //"overview"
			String targetFossology = parameters.get("targetFossology");
			if (targetFossology == null) {
				throw new Exception(String.format("%s property not speficied", "targetFossology"));
			}
			licenseBuckets = analyseOverviewReport(targetFossology, licensesMap);
		}


		//add all measures to the IndicatorsMap (= Risk Data)
		boolean addAll = false;
		if( addAll )
			for( String licenseBucket : licenseBuckets.keySet() ) {
				RiskData rd = new RiskData(
						"Measure_Fossology." + licenseBucket, entity, new Date(), RiskDataType.NUMBER, licenseBuckets.get(licenseBucket) );
				map.put( "Measure_Fossology." + licenseBucket, rd );
			}

		double total =  licenseBuckets.get("_sum_"); //to make sure that the result of the division is a float //number of files
		Integer licenseCount = licenseBuckets.get("_count_"); //number of licenses found
		Integer numPermissive = licenseBuckets.get("Permissive License");
		Integer numCopyleft = licenseBuckets.get("FSF Copyleft");
		Integer numNoLicense = licenseBuckets.get("No License");
		Integer numUnknown = licenseBuckets.get("_unknown_");
		Integer numLinkingPermitted = licenseBuckets.get("FSF linking permitted");
		Integer numCommercial = licenseBuckets.get("Commercial license");
		Integer numPublicDomain = licenseBuckets.get("Public domain");
		Integer numMultiplyLicensed = licenseBuckets.get("_num_multiply_licensed_files_");

		map.add("number-of-different-licenses", RiskDataType.NUMBER, licenseCount);
		if (total > 0) {
			map.add("percentage-of-files-without-license", RiskDataType.NUMBER, numNoLicense / total);
			map.add("files-with-unknown-license", RiskDataType.NUMBER, numUnknown / total);
			map.add("copyleft-licenses", RiskDataType.NUMBER, numCopyleft / total);
			map.add("copyleft-licenses-with-linking", RiskDataType.NUMBER, numLinkingPermitted / total);
			map.add("percentage-of-files-with-permissive-license", RiskDataType.NUMBER, numPermissive / total);
			map.add("files-with-commercial-license", RiskDataType.NUMBER, numCommercial / total);
			map.add("number-of-files-analysed", total);
			if (numMultiplyLicensed != null)
				map.add("percentage-of-files-with-multiple-license", RiskDataType.NUMBER, numMultiplyLicensed / total);
		}
		return map;
	}

	/**
	 * Parses a Fossology-generated License txt file with list of files and licenses. Example row:
	 * SAT4J 2.3.3/SAT4J 2.3/Sat4J-2.3.3/plugin.properties: EPL-1.0 ,LGPL-2.1+
	 * @param targetFossology path+filename (http or local)
	 * @param licensesMap
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private HashMap<String, Integer> analyseFileList(String targetFossology, HashMap<String, Collection<String>> licensesMap, String[] acceptedExtensions ) throws ClientProtocolException, IOException {

		//LicenseAnalysisReport licenseAnalysisReport; TODO use this one
		BufferedReader br = null;
		HttpEntity entity = null;
//		CloseableHttpResponse response = null;

		int totalFiles = 0;
		int numMultiplyLicensedFiles = 0;
		int numAdditionalLicenseDefinitions = 0;
		String line;
//		int i=0;
		Map<String, Integer> licenseOccurrences = new HashMap<String, Integer>();
//		boolean onlyXLinesDisplayed = false;

		try{
			//open text file with list of files and licenses
			if (targetFossology.toLowerCase().startsWith("http")) {
				HttpClient httpClient = HttpClientBuilder.create().build();;
				HttpGet get = new HttpGet( targetFossology );
				HttpResponse response = httpClient.execute(get);

				entity = response.getEntity();
				if (entity != null) {
					InputStream is = entity.getContent();
					br = new BufferedReader(new InputStreamReader(entity.getContent()));

					//EntityUtils.consume(entity); //release all resources held by the httpEntity
				}
				//response.close();
			} else { //local file

				br = new BufferedReader( new InputStreamReader( new FileInputStream( targetFossology ) ) );
			}

			/* Calculate the occurrences for each license type */

			while ((line = br.readLine()) != null) {
//				System.out.println( line );
				/* Parse only the lines that contains a ':' */
				if( line.contains("Warning: Only the last" ) ) {
//					onlyXLinesDisplayed = true;
					break;
				}

				if (line.contains(":")) {
					String[] parts = line.split(":", 2);
					if (parts.length>1) {

						if (acceptedExtension( parts[0].trim(), acceptedExtensions ) ){
							String licenseString = parts[1].trim();
							String[] licenses = licenseString.split(","); //multiple licenses possible

							for (String license : licenses) {
								if (licenseOccurrences.get(license) == null) {
									licenseOccurrences.put(license, 1);
								} else {
									licenseOccurrences.put(license, licenseOccurrences.get(license) + 1);
								}
							}
							totalFiles++;
							numAdditionalLicenseDefinitions += licenses.length-1;//0 if single license
							numMultiplyLicensedFiles += licenses.length<=1?0:1;  //0 if single license
						}
					}
				}
			}
		}finally {
			if (entity!=null) { //http
				EntityUtils.consume(entity); //release all resources held by the httpEntity
//				response.close();
			}
			br.close(); //also if local
		}
		HashMap<String, Integer> licenseBuckets = new HashMap<String, Integer>();
		//TODO: switch from licenseBuckets to the use of licenseAnalysisReport
		//licenseAnalysisReport.totalFiles = totalFiles;

		licenseBuckets.put("_sum_", totalFiles); //num of files

		licenseBuckets.put("_num_multiply_licensed_files_", numMultiplyLicensedFiles);
		licenseBuckets.put("_num_additional_licenses_", numAdditionalLicenseDefinitions);

		licenseBuckets.put("_count_", licenseOccurrences.keySet().size());
		if (licenseOccurrences.get("No_license_found") != null) {
			/* Removes the NO_LICENSE_FOUND pseudolicense from the number of licenses found. */
			/* UnclassifiedLicense pseudolicense remains still included */
			//licenseAnalysisReport.numberOfLicenses--;
			licenseBuckets.put("_count_", licenseOccurrences.keySet().size()-1);
		}

		//initializes with 0 to avoid missing types
		licenseBuckets.put("_unknown_", 0);
		for (String licensetype : licensesMap.keySet()) {
			licenseBuckets.put(licensetype, 0);
		}

		/* Find license types and sum their occurrences*/
		for (String license : licenseOccurrences.keySet()) {
			String licenseType = getLicenseTypeForLicense(licensesMap, license); //UNKNOWN_LICENSE_TYPE _unknown_ if none matches

			if (licenseBuckets.get(licenseType) == null) {
				licenseBuckets.put(licenseType, licenseOccurrences.get(license));
			} else {
				licenseBuckets.put(licenseType,
						licenseBuckets.get(licenseType) + licenseOccurrences.get(license));
			}
		}

		return licenseBuckets;

	}

	/**
	 * Analyses a fossology html file
	 * @param target
	 * @param licensesMap
	 * @return
	 * @throws IOException
	 */
	private HashMap<String, Integer> analyseOverviewReport(String target, HashMap<String, Collection<String>> licensesMap) throws IOException {
		//private static HashMap<String, Integer> analyseFossologyReport(String target, String licenseFile) throws IOException {
		//        List<String> result = new ArrayList<String>();
		Document document;

		if (target.startsWith("http")) {
			document = Jsoup.connect(target).get();
		} else {
			File file = new File(target);
			document = Jsoup.parse(file, "UTF-8", "http://localhost");
		}

		Element table = document.select("table[id=lichistogram]").first();
		Elements rows = table.select("tr");

		List<LicenseEntry> llist= new ArrayList<LicenseEntry>(); //list of licenses in the fossology file

		//for each license, parses the name (0) and the number of occurrences (2) and saves it as a LicenseEntry
		for (Element element : rows) {
			Elements col= element.select("td");

			if (col.size()!=0) {
				int c=Integer.parseInt(col.get(0).ownText());//num of occurrences
				String lic=col.get(2).text();
				llist.add(new LicenseEntry(c,lic));
			}
		}

		//get license type buckets

		HashMap<String, Integer> licenseBuckets = new HashMap<String, Integer>();
		int total=0;


		Set<String> licenseTypes = licensesMap.keySet();
		//initialize with 0 to avoid missing types
		for (String licensetype : licenseTypes) {
			licenseBuckets.put(licensetype, 0);
		}

		boolean matched = false;
		int numUnknown = 0;
		for (LicenseEntry le : llist) {
			for (String licenseType : licenseTypes) {//cycles on license types from config file
				if (le.matchesOneOf(licensesMap.get(licenseType), licenseType)) {
					Integer currentcount=licenseBuckets.get(le.licensetype);
					if (currentcount==null) //for safety, but should be initialised
						currentcount=0;
					licenseBuckets.put(le.licensetype, currentcount+le.count);
					matched = true;
				}
			}
			total+=le.count;
			if (matched==false) { //unknown
				numUnknown+=le.count;
				System.err.println("Unknown license: " +le.getName());
			}
		}

		licenseBuckets.put("_unknown_", numUnknown);
		licenseBuckets.put("_sum_", total);
		licenseBuckets.put("_count_", llist.size());

		return licenseBuckets;
	}

	private String getLicenseTypeForLicense(HashMap<String, Collection<String>> licensesMap, String license) {
		for (String l : licensesMap.keySet()) {
			//if (license.toLowerCase().contains(l.toLowerCase())) {
			//attention: order matters in the file! (e.g. to parse GPL/LGPL correctly)
			if (matchesOneOf(licensesMap.get(l), license))
				return l;
		}
		return "_unknown_";
	}

	public boolean matchesOneOf(Collection<String> si, String license){
		for (String string : si) {
			if (license.startsWith(string)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Parses a LicensesCfg file
	 * @param target
	 * @return HashMap: License Types, each with a Collection of Licenses
	 * @throws IOException
	 */
	protected static HashMap<String, Collection<String>> parseLicensesFile (String target) throws IOException {
		HashMap<String, Collection<String>> result = new HashMap<String, Collection<String>>();
		Document document;
		if (target.startsWith("http")) {
			document = Jsoup.connect(target).get();
		} else {
			if( target.startsWith( "file:" ) )
				target = target.substring( 5 );

			//File file = new File(target);

			InputStream in =
					RDCFossology.class.getResourceAsStream( "res/"+target );
			//System.out.println("Fossology config file used: "+file.getPath());
			//System.out.println("Fossology IS file used: "+in.toString());

			document = Jsoup.parse(in, "UTF-8", "http://localhost");

		}

		Elements licensesLinks = document.getElementsByAttribute("id");

		for (Element element : licensesLinks) {
			String licenseName = element.child(0).text();
			if (element.children().size() >1) {
				String s = element.child(1).text();
				Collection<String> licensesList = Arrays.asList(s.split("\\s*\\|\\s*"));

				result.put(licenseName, licensesList);
			}
		}

		return result;
	}

	private boolean acceptedExtension( String filePathString, String[] acceptedExtensions ) {

		if( "".equals( acceptedExtensions ) )
			return true; //default: all extensions
		String[] filePath = filePathString.trim().split("/");
		String fileNameString = filePath[filePath.length-1];

		int dot = fileNameString.lastIndexOf('.');
		String extension = (dot == -1) ? "" : fileNameString.substring(dot+1).toLowerCase();  //empty string if '.' is the last char

		for( String accext: acceptedExtensions ) {
			if( accext.trim().equalsIgnoreCase( extension ) ) {
				return true;
			}
		}

		final String[] knownNonCode = {"txt", "xml", "xslt", "xsd", "xsl", "xul", "xed", "xmi", "wsdl", "owl", "html", "xhtml", "htm","properties", "prefs", "test", "pom", "project", "dtd", "css", "scss","ttf", "diff",
				"license", "ico",  "png", "gif", "jpg", "pspimage", "psd", "doc", "sh", "bat", "ods", "odp", "rdf", "manifest", "cat", "zip", "vm", "mf", "old", "bak", "ini",
				"cfg", "conf", "config", "def", "inf", "lst", "sql", "json", "wsdl", "class", "classpath", "type", "less", "md5", "sha1", ""}; //vm: velocity

		return false;
	}
}
