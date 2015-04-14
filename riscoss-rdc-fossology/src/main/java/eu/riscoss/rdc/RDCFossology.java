package eu.riscoss.rdc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import eu.riscoss.datacollector.common.IndicatorsMap;

import eu.riscoss.dataproviders.RiskData;

public class RDCFossology implements RDC {
	
	Properties defaultProperties = new Properties();
	Properties properties = null;
	
	
//	Map<String,String> 
	
	public RDCFossology() {
		defaultProperties.put( "licenseFile", "./LicensesCfg.html" );
//		defaultProperties.put( "targetFossology", "http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002" );
		defaultProperties.put( "fossologyScanType", "filelist" );
//		defaultProperties.put( "targetFossologyList", "http://fossology.ow2.org/?mod=license-list&upload=38&item=292002&output=dltext" );
		defaultProperties.put( "targetEntity", "ND" );//not defined
		defaultProperties.put( "fossologyFilterExtensions","true");
		defaultProperties.put( "fossologyAcceptedExtensions","java,cpp,jj,js,jsp,php,py,jape,aj,jspf,jsb,groovy");

		properties = new Properties(defaultProperties);
	}
	
	public Map<String,RiskData> getIndicators() {

		//read the default config file
		//Properties defaultProperties = RdpConfig.loadDefaults(defaultPropertiesFile);
		//read the config from file
		//properties = RdpConfig.load(args[3], defaultProperties);
		IndicatorsMap im = new IndicatorsMap(properties.getProperty("targetEntity"));
				
		Map<String,RiskData> map = new HashMap<>();
		
		try {
			
			new FossologyDataProvider().createIndicators( im, properties );
			
			System.out.println("\n**** Resulting indicators ****" + im.entrySet());
			System.out.flush();
			/******************************************************/
			
			for (String key : im.keySet()) {
				map.put(key, im.get(key));
			}
		}catch (Exception e1) {
			//			System.err.println("Error in parsing command line arguments. Exiting.");
			e1.printStackTrace();
			//			System.exit(1);
		}
		
		return map;
	}
	
	public String getName() {
		return "Fossology";
	}
	
	@Override
	public Collection<RDCParameter> getParameterList() {
		
		Set<RDCParameter> set = new HashSet<>();
		
//		set.add( new RDCParameter( "targetEntity", "targetEntity", "", "" ) );
		set.add( new RDCParameter( "targetFossology", "Fossology http overview site target URL", "", getDef( "targetFossology" ) ) );
		set.add( new RDCParameter( "fossologyScanType", "overview or filelist", "", getDef( "fossologyScanType" ) ) );
		set.add( new RDCParameter( "targetFossologyList", "Fossology results txt filelist", "", getDef( "targetFossologyList" ) ) );
//		set.add( new RDCParameter( "url", "url", "", getDef( "url" ) ) );
		set.add( new RDCParameter( "licenseFile", "License groups configuration file" , "", getDef( "licenseFile" ) ) );
		set.add( new RDCParameter( "fossologyFilterExtensions", "Filter extensions? true (default) / false" , "", getDef( "fossologyFilterExtensions" ) ) );
		set.add( new RDCParameter( "fossologyAcceptedExtensions", "comma-separated list of file extensions" , "java, cpp, jj, js, jsp, php, py", getDef( "fossologyAcceptedExtensions" ) ) );	
		
		return set;
	}
	
	private String getDef( String key ) {
		Object ret = defaultProperties.get( key );
		if( ret == null ) ret = "";
		return ret.toString();
	}
	
	@Override
	public void setParameter( String parName, String parValue ) {
		properties.put( parName, parValue );
	}

	@Override
	public Collection<String> getIndicatorNames() {
		
		String[] n = {"number-of-different-licenses", 
				"percentage-of-files-without-license",
				"files-with-unknown-license",
				"copyleft-licenses",
				"copyleft-licenses-with-linking",
				"percentage-of-files-with-permissive-license",
				"files-with-commercial-license",
				"percentage-of-files-with-public-domain-license",
				"percentage-of-files-with-multiple-license"
		};
		
		return Arrays.asList(n);
	}
	
	public static void main(String[] args) {
		RDC rdc = new RDCFossology();
		rdc.setParameter("targetFossology","http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002");
		rdc.setParameter("fossologyScanType", "filelist");
		rdc.setParameter("targetFossologyList", "http://fossology.ow2.org/?mod=license-list&upload=38&item=292002&output=dltext");
		rdc.getIndicators();
	}
}
