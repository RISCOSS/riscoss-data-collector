package eu.riscoss.rdc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import eu.riscoss.dataproviders.Distribution;
import eu.riscoss.dataproviders.IndicatorsMap;
import eu.riscoss.dataproviders.RiskData;

public class RDCFossology implements RDC {
	
	Properties defaultProperties = new Properties();
	
//	Map<String,String> 
	
	public RDCFossology() {
		defaultProperties.put( "licenseFile", "./input/LicensesCfg.html" );
		defaultProperties.put( "targetFossology", "http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002" );
		defaultProperties.put( "fossologyScanType", "filelist" );
		defaultProperties.put( "targetFossologyList", "http://fossology.ow2.org/?mod=license-list&upload=38&item=292002&output=dltext" );
	}
	
	public Map<String,RiskData> getIndicators() {
		
		String url = 
				"http://riscossplatform.ow2.org:8080/riscoss-rdr"; //OW2instance RDR
		//				"http://riscoss-platform.devxwiki.com/rdr"; //M24 RDR
		
//		String propertiesFile = "Riscossconfig_" + componentName + ".properties";
		
		//read the default config file
		//		Properties //defaultProperties = RdpConfig.loadDefaults( defaultPropertiesFile );
		//		defaultProperties = new Properties();
		//		defaultProperties.put( "licenseFile", "./input/LicensesCfg.html" );
		
		//read the config from file
//		Properties props = RdpConfig.load( propertiesFile, defaultProperties );
		
		return test( url, defaultProperties );
	}
	
	Map<String,RiskData> test( String riskDataRepositoryURL, Properties properties ) {
		
		Map<String,RiskData> map = new HashMap<>();
		
		try {
			String targetEntity = properties.getProperty("targetEntity");
			
			//			System.out.println();
			//			System.out.println("************************************************");
			//			System.out.printf("Starting the analysis for component %s.\n\n",targetEntity);
			
			IndicatorsMap im = new IndicatorsMap(targetEntity);
			
			
			new FossologyDataProvider().createIndicators( im, properties );
			
			System.out.println("\n**** Resulting indicators ****" + im.entrySet());
			System.out.flush();
			/******************************************************/
			
			/*
			 * At the end, send the result to the Risk Data Repository
			 * Example repository: http://riscoss-platform.devxwiki.com/rdr/xwiki?limit=10000
			 */
			
			//			if (csvresults)
			{
				System.out.println("Results in CSV format retrieved "+new Date());
				for (String key : im.keySet()) {
					RiskData content = im.get(key);
					map.put( key, content );
					//					if (content.getType().equals(RiskDataType.DISTRIBUTION)
					System.out.print("# "+content.getTarget() + "\t" + content.getId() + "\t");
					switch (content.getType()) {
					case DISTRIBUTION:
						for(Double d: ((Distribution)content.getValue()).getValues())
							System.out.print(d+"\t");
						break;
					case NUMBER:
						System.out.print(content.getValue());
						break;
					default:
						break;
					}
					System.out.print("\n");
				}
				System.out.println("CSV End");
				
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
		set.add( new RDCParameter( "targetFossology", "targetFossology", "", getDef( "targetFossology" ) ) );
		set.add( new RDCParameter( "fossologyScanType", "fossologyScanType", "", getDef( "fossologyScanType" ) ) );
		set.add( new RDCParameter( "targetFossologyList", "targetFossologyList", "", getDef( "targetFossologyList" ) ) );
		set.add( new RDCParameter( "url", "url", "", getDef( "url" ) ) );
//		set.add( new RDCParameter( "licenseFile", "licenseFile", "", getDef( "licenseFile" ) ) );
		
		return set;
	}
	
	String getDef( String key ) {
		Object ret = defaultProperties.get( key );
		if( ret == null ) ret = "";
		return ret.toString();
	}
	
	@Override
	public void setParameter( String parName, String parValue ) {
		defaultProperties.put( parName, parValue );
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return new ArrayList<String>();
	}
}
