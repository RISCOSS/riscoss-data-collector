//package eu.riscoss.rdc;
//
///**
// * @author Mirko Morandini, Fabio Mancinelli
// */
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import eu.riscoss.datacollector.common.IndicatorsMap;
//import eu.riscoss.dataproviders.RdpConfig;
//import eu.riscoss.datacollector.DataProvider;
//
//public class FossologyDataProvider extends DataProvider {
//
////	public static void main(String[] args) {
////		run(new FossologyDataProvider(), args);
////	}
//
//	private static final boolean VERBOSE = false;  //output of all licenses and filterings
//	
//    /* Properties needed by the data collector */
//    private static final String TARGET_FOSSOLOGY_PROPERTY = "targetFossology";
//    private static final String FOSSOLOGY_SCANTYPE_PROPERTY ="fossologyScanType";
//    private static final String LICENSE_FILE_PROPERTY = "licenseFile";
//	private static final String TARGET_FOSSOLOGY_TXT_PROPERTY = "targetFossologyList";
//	private static final String FOSSOLOGY_FILTER_EXTENSIONS = "fossologyFilterExtensions"; //true (default) / false
//	private static final String FOSSOLOGY_ACCEPTED_EXTENSIONS = "fossologyAcceptedExtensions"; //comma-separated list of file extensions, e.g. java, cpp, jj, js, jsp, php, py
//
//    /* String used to check when a file doens't have a license associated */
//    private static final String NO_LICENSE_FOUND = "No_license_found";
//
//    /* These are the license types defined in licenseType.properties */
//    private static final String PERMISSIVE_LICENSE_TYPE = "permissive";
//    private static final String COPYLEFT_LICENSE_TYPE = "copyleft";
//    private static final String COPYLEFT_WITH_LINKING_LICENSE_TYPE = "copyleft-with-linking";
//    private static final String COMMERCIAL_LICENSE_TYPE = "commercial";
//    private static final String UNKNOWN_LICENSE_TYPE = "_unknown_";
//    
//    String[] acceptedExtensions = new String[0];
//    
//	/**
//	 * 
//	 * @param targetFosslolgy
//	 * @param licenseFile
//	 * @throws Exception 
//	 */	
//	public void createIndicators(IndicatorsMap im, Properties properties) throws Exception {
//		//private static void createIndicatorsFromFossologyMeasures(String targetFosslolgy, String licenseFile) throws IOException {
//		
//		final String licenseFile = properties.getProperty(LICENSE_FILE_PROPERTY);
//			if (licenseFile == null) {
//				throw new Exception(String.format("%s property not speficied", LICENSE_FILE_PROPERTY));
//			}
//		
//		String scanType = properties.getProperty(FOSSOLOGY_SCANTYPE_PROPERTY); //"overview" or "filelist"
//		if (scanType == null) 
//			scanType = "overview"; //default value as it was the only one in prior versions
//		
//		this.acceptedExtensions = properties.getProperty(FOSSOLOGY_ACCEPTED_EXTENSIONS, "").split(",");
//				
//		if (properties.getProperty(FOSSOLOGY_FILTER_EXTENSIONS, "true")=="false"){
//			this.acceptedExtensions = new String[0]; //empty the extensions list --> behaviour: accept all extensions 
//		}	
//		System.out.println(this.acceptedExtensions);
//			
//		
//		HashMap<String, Collection<String>> licensesMap = parseLicensesFile(licenseFile);
//		
//		HashMap<String, Integer> licenseBuckets;
//		if (scanType.equals("filelist")){
//			String targetFossologyTxt = properties.getProperty(TARGET_FOSSOLOGY_TXT_PROPERTY);
//		        if (targetFossologyTxt == null) {
//		            throw new Exception(String.format("%s property not speficied", TARGET_FOSSOLOGY_TXT_PROPERTY));
//		        }
//			licenseBuckets = analyseFileList(targetFossologyTxt, licensesMap);
//		}
//		else { //"overview"
//			String targetFossology = properties.getProperty(TARGET_FOSSOLOGY_PROPERTY);
//	        if (targetFossology == null) {
//	            throw new Exception(String.format("%s property not speficied", TARGET_FOSSOLOGY_PROPERTY));
//	        }
//	        licenseBuckets = analyseOverviewReport(targetFossology, licensesMap);
//		}
//
//		
//		//add all measures to the IndicatorsMap (= Risk Data)
//		boolean addAll = false;
//		if( addAll )
//			for( String licenseBucket : licenseBuckets.keySet() ) {
//				im.add( "Measure_Fossology." + licenseBucket, licenseBuckets.get( licenseBucket ) );
//			}
//	
//		float total =  licenseBuckets.get("_sum_"); //to make sure that the result of the division is a float //number of files
//		Integer licenseCount =  licenseBuckets.get("_count_"); //number of licenses found
//		Integer numPermissive = licenseBuckets.get("Permissive License");
//		Integer numCopyleft =  licenseBuckets.get("FSF Copyleft");
//		Integer numNoLicense =  licenseBuckets.get("No License");
//		Integer numUnknown =  licenseBuckets.get("_unknown_");
//		Integer numLinkingPermitted =  licenseBuckets.get("FSF linking permitted");
//		Integer numCommercial =  licenseBuckets.get("Commercial license");
//		Integer numPublicDomain = licenseBuckets.get("Public domain");
//		Integer numMultiplyLicensed = licenseBuckets.get("_num_multiply_licensed_files_");
//		
//		im.add("number-of-different-licenses", licenseCount); //Number of (different?) component licenses
//		im.add("percentage-of-files-without-license", numNoLicense/total); //% of files without license (Fossology)
//		im.add("files-with-unknown-license", numUnknown/total); //% of files with unclear/unknown license (Fossology)
//		im.add("copyleft-licenses", numCopyleft/total); //% of licenses: viral (Fossology)
//		im.add("copyleft-licenses-with-linking", numLinkingPermitted/total); //% of licenses: library viral (Fossology)
//		im.add("percentage-of-files-with-permissive-license", numPermissive/total); //% of licenses: without constraints (Fossology)
//		im.add("files-with-commercial-license",numCommercial/total); //% of licenses: commercial (Fossology)
////		im.add("percentage-of-files-with-public-domain-license",numPublicDomain/total);
//		im.add("percentage-of-files-with-multiple-license", numMultiplyLicensed/total);
//		//TODO
////		im.add("files-with-ads-required-liceses",0);
//		
//		//    	i93b" label="Amount of OSS code integrated"
//		//    	i93c" label="Technique used for integrating code (static/dynamic linking, copy)"
//		//    	i93d" label="Type of licenses in core components"
//		//    	i93h" label="Amount of component code imported/linked from other OSS projects"
//		//    	i120" label="Percentage of US code"
//	
//		//System.out.println(IndicatorsMap.get().toString());
//	}
//
//	/**
//	 * Parses a LicensesCfg file
//	 * @param target
//	 * @return HashMap: License Types, each with a Collection of Licenses
//	 * @throws IOException
//	 */
//	protected static HashMap<String, Collection<String>> parseLicensesFile (String target) throws IOException {
//		HashMap<String, Collection<String>> result = new HashMap<String, Collection<String>>();
//		Document document;
//		if (target.startsWith("http")) {
//			document = Jsoup.connect(target).get();
//		} else {
//			File file = new File(target);
//			System.out.println("Fossology config file used: "+file.getCanonicalPath());
//			
////				file = new File( RdpConfig.class.getResource( file.toString() ).toURI().getPath() );
//				document = Jsoup.parse(file, "UTF-8", "http://localhost");
//			
//		}
//
//		//    	 System.out.println(document.outerHtml());
//
//		Elements licensesLinks = document.getElementsByAttribute("id");
//
//		for (Element element : licensesLinks) {
//			String licenseName = element.child(0).text();
//			if (element.children().size() >1) {
//				String s = element.child(1).text();
//				Collection<String> licensesList = Arrays.asList(s.split("\\s*\\|\\s*")); //("\\s*\\|\\s*"));
//
////xDebug				System.out.println("Analysed license type: "+licenseName+": "+licensesList);
//				result.put(licenseName, licensesList);
//			}
//		}
//	
//		return result;
//	}
//
//	/**
//	 * Analyses a fossology html file
//	 * @param target
//	 * @param licensesMap
//	 * @return
//	 * @throws IOException
//	 */
//	private HashMap<String, Integer> analyseOverviewReport(String target, HashMap<String, Collection<String>> licensesMap) throws IOException {
//		//private static HashMap<String, Integer> analyseFossologyReport(String target, String licenseFile) throws IOException {
//		//        List<String> result = new ArrayList<String>();
//		Document document;
//
//		if (target.startsWith("http")) {
//			document = Jsoup.connect(target).get();
//		} else {
//			File file = new File(target);
//			document = Jsoup.parse(file, "UTF-8", "http://localhost");
//		}
//
//		Element table = document.select("table[id=lichistogram]").first();
//		Elements rows = table.select("tr");
//
//		List<LicenseEntry> llist= new ArrayList<LicenseEntry>(); //list of licenses in the fossology file
//
//		//for each license, parses the name (0) and the number of occurrences (2) and saves it as a LicenseEntry
//		for (Element element : rows) {
//			Elements col= element.select("td");
//
//			if (col.size()!=0) {
//				int c=Integer.parseInt(col.get(0).ownText());//num of occurrences
//				String lic=col.get(2).text();
//				llist.add(new LicenseEntry(c,lic));
//				//mlist.put(lic, c);
//			}
//			//        	System.out.println(col.get(1).ownText());
//			//        	Element count=col.get(0);
//		}
//
//		//get license type buckets
//
//		HashMap<String, Integer> licenseBuckets = new HashMap<String, Integer>();
//		int total=0;
//
//		
//		Set<String> licenseTypes = licensesMap.keySet();
//		//initialize with 0 to avoid missing types
//		for (String licensetype : licenseTypes) {
//			licenseBuckets.put(licensetype, 0);
//		}
//
//		boolean matched = false;
//		int numUnknown = 0;
//		for (LicenseEntry le : llist) {
//			for (String licenseType : licenseTypes) {//cycles on license types from config file
//				if (le.matchesOneOf(licensesMap.get(licenseType), licenseType)) {
//					Integer currentcount=licenseBuckets.get(le.licensetype);
//					if (currentcount==null) //for safety, but should be initialised
//						currentcount=0;
//					licenseBuckets.put(le.licensetype, currentcount+le.count);
//					matched = true;
//				}
//			}
//			total+=le.count;
//			if (matched==false) { //unknown
//				numUnknown+=le.count;
//				System.out.println("Unknown license: " +le.getName());
//			}
//		}
//
//		licenseBuckets.put("_unknown_", numUnknown);
//		licenseBuckets.put("_sum_", total);
//		licenseBuckets.put("_count_", llist.size());
//		
//		System.out.println("\nLicense Buckets Fossology from HTML overview scanning:");
//		System.out.println(licenseBuckets);
//
//		//        for (String license : result) {
//		//            System.out.format("%s\n", license);
//		//        }
//		return licenseBuckets;
//	}
//
//	public boolean matchesOneOf(Collection<String> si, String license){
//		for (String string : si) {
////			if (name.contains(string))
//			if (license.startsWith(string)){
//				return true;
//			}
//		}
//		return false;
//	}
//	
//    private String getLicenseTypeForLicense(HashMap<String, Collection<String>> licensesMap, String license) { 	
//    	for (String l : licensesMap.keySet()) {
//    		//if (license.toLowerCase().contains(l.toLowerCase())) {
//    		//attention: order matters in the file! (e.g. to parse GPL/LGPL correctly)
//    		if (matchesOneOf(licensesMap.get(l), license)) 
//    			return l;
//    	}
//    	//DEBUG - add missing licenses to licenses file!
//    	System.err.println("WARNING: getLicenseTypeForLicense() #Unknown license: " +license+". Try to add it to LicensesCfg.");
//    	return UNKNOWN_LICENSE_TYPE;
//    } 
//           
//    /**
//	 * Parses a Fossology-generated License txt file with list of files and licenses. Example row: 
//	 * SAT4J 2.3.3/SAT4J 2.3/Sat4J-2.3.3/plugin.properties: EPL-1.0 ,LGPL-2.1+
//	 * @param targetFossology path+filename (http or local)
//	 * @param licensesMap 
//	 * @return
//     * @throws IOException 
//     * @throws ClientProtocolException 
//	 */
//    private HashMap<String, Integer> analyseFileList(String targetFossology, HashMap<String, Collection<String>> licensesMap) throws ClientProtocolException, IOException {
//    	
//        //LicenseAnalysisReport licenseAnalysisReport; TODO use this one
//        BufferedReader br = null;
//        HttpEntity entity = null;
//        CloseableHttpResponse response = null;
//        
//        int totalFiles = 0;
//    	int numMultiplyLicensedFiles = 0;
//    	int numAdditionalLicenseDefinitions = 0;
//    	String line;
//    	int i=0;
//    	Map<String, Integer> licenseOccurrences = new HashMap<String, Integer>();
//    	boolean onlyXLinesDisplayed = false; 
//    	
//        try{
//        	//open text file with list of files and licenses
//        	if (targetFossology.toLowerCase().startsWith("http")) {
//        		CloseableHttpClient httpClient = HttpClients.createDefault();
//        		HttpGet get = new HttpGet(targetFossology);
//        		response = httpClient.execute(get);
//
//        		entity = response.getEntity();
//        		if (entity != null) {
//        			InputStream is = entity.getContent();
//        			br = new BufferedReader(new InputStreamReader(entity.getContent()));
//
//        			//EntityUtils.consume(entity); //release all resources held by the httpEntity
//        		}   
//        		//response.close();
//        	} else { //local file
//
//        		br = new BufferedReader(new InputStreamReader(new FileInputStream(targetFossology)));
//        	}
//
//        	/* Calculate the occurrences for each license type */
//        	
//        	while ((line = br.readLine()) != null) {
//        		//DEBUG 
//        		//System.out.println(i++ +" "+line);
//        		/* Parse only the lines that contains a ':' */
//        		if (line.contains("Warning: Only the last")){
//        			System.out.println();
//        			System.err.println("WARNING: "+ line);
//        			System.out.println();
//        			onlyXLinesDisplayed = true;
//        			break;
//        		}
//        		
//        		if (line.contains(":")) {
//        			String[] parts = line.split(":", 2);
//        			if (parts.length>1) {
//
//        				if (acceptedExtension(parts[0].trim())){
//        					String licenseString = parts[1].trim();
//        					String[] licenses = licenseString.split(","); //multiple licenses possible
//
//        					for (String license : licenses) {
//        						if (licenseOccurrences.get(license) == null) {
//        							licenseOccurrences.put(license, 1);
//        						} else {
//        							licenseOccurrences.put(license, licenseOccurrences.get(license) + 1);
//        						}
//        					}
//        					if(VERBOSE){
//        						for (String l : licenses) {
//									System.out.print(l+"  ");
//								}
//        						System.out.println();
//        					}
//        					totalFiles++;
//        					numAdditionalLicenseDefinitions += licenses.length-1;//0 if single license
//        					numMultiplyLicensedFiles += licenses.length<=1?0:1;  //0 if single license
//        				}
//        			}
//        		}
//        	}
//        }finally {
//        	if (entity!=null) { //http
//        		EntityUtils.consume(entity); //release all resources held by the httpEntity
//        		response.close();
//        	}
//        	br.close(); //also if local
//        }
//		HashMap<String, Integer> licenseBuckets = new HashMap<String, Integer>();
//		//TODO: switch from licenseBuckets to the use of licenseAnalysisReport
//		//licenseAnalysisReport.totalFiles = totalFiles;
//		
//		licenseBuckets.put("_sum_", totalFiles); //num of files
//		
//		licenseBuckets.put("_num_multiply_licensed_files_", numMultiplyLicensedFiles);
//		licenseBuckets.put("_num_additional_licenses_", numAdditionalLicenseDefinitions);
//        
//        //licenseAnalysisReport.numberOfLicenses = licenseOccurrences.keySet().size();
//        licenseBuckets.put("_count_", licenseOccurrences.keySet().size()); 
//        if (licenseOccurrences.get(NO_LICENSE_FOUND) != null) { 
//            /* Removes the NO_LICENSE_FOUND pseudolicense from the number of licenses found. */
//        	/* UnclassifiedLicense pseudolicense remains still included */
//        	//licenseAnalysisReport.numberOfLicenses--;
//        	licenseBuckets.put("_count_", licenseOccurrences.keySet().size()-1);
//        }
//    
//		//initializes with 0 to avoid missing types
//        licenseBuckets.put("_unknown_", 0);
//		for (String licensetype : licensesMap.keySet()) {
//			licenseBuckets.put(licensetype, 0);
//		}
//		
//	    /* Find license types and sum their occurrences*/
//        for (String license : licenseOccurrences.keySet()) {
//            //if (!license.equals(NO_LICENSE_FOUND)) { //MM: commented... also this pseudolicense is used for bucketing 
//                String licenseType = getLicenseTypeForLicense(licensesMap, license); //UNKNOWN_LICENSE_TYPE _unknown_ if none matches
//
//                if (licenseBuckets.get(licenseType) == null) {
//                	licenseBuckets.put(licenseType, licenseOccurrences.get(license));
//                } else {
//                	licenseBuckets.put(licenseType,
//                			licenseBuckets.get(licenseType) + licenseOccurrences.get(license));
//                }
//            //}
//        }
//    	
//		System.out.println("\nLicense Buckets Fossology from TXT filelist scanning:");
//		System.out.println(licenseBuckets);
//
//		//        for (String license : result) {
//		//            System.out.format("%s\n", license);
//		//        }
//		return licenseBuckets;
//        
//    }
//
//	private boolean acceptedExtension(String filePathString) {
//		
//		//acceptedExtensions = {"java", "cpp", "jj", "js", "jsp", "php", "py",....}; //use this.acceptedExtensions
//		if (acceptedExtensions.equals(""))
//			return true; //default: all extensions
//		String[] filePath = filePathString.trim().split("/");
//		String fileNameString = filePath[filePath.length-1];
//		
//		int dot = fileNameString.lastIndexOf('.');
//		String extension = (dot == -1) ? "" : fileNameString.substring(dot+1).toLowerCase();  //empty string if '.' is the last char
//		
//		if(VERBOSE){
//			System.out.print("."+extension);
//		}
//		for (String accext: this.acceptedExtensions){
//			if (accext.trim().equalsIgnoreCase(extension)){
//				if(VERBOSE){
//					System.out.println();
//				}
//				return true;
//			}
//		}
//		if(VERBOSE)
//			System.out.println(" - filtered.");
//		
//		final String[] knownNonCode = {"txt", "xml", "xslt", "xsd", "xsl", "xul", "xed", "xmi", "wsdl", "owl", "html", "xhtml", "htm","properties", "prefs", "test", "pom", "project", "dtd", "css", "scss","ttf", "diff", 
//				"license", "ico",  "png", "gif", "jpg", "pspimage", "psd", "doc", "sh", "bat", "ods", "odp", "rdf", "manifest", "cat", "zip", "vm", "mf", "old", "bak", "ini", 
//				"cfg", "conf", "config", "def", "inf", "lst", "sql", "json", "wsdl", "class", "classpath", "type", "less", "md5", "sha1", ""}; //vm: velocity
//		if (!Arrays.asList(knownNonCode).contains(extension))
//			System.err.println("INFO: unknown extension: "+ extension);
//		
//		return false;
//	}
//	
//
//}
//
//
