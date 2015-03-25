package eu.riscoss.dataproviders;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RdpConfig {

	/**
	 * creates a sample properties file, saves it on the default path and returns it.
	 * @param propertiesFile
	 * @return sample properties
	 */
	public static Properties loadWrite(String propertiesFile) {
		Properties p =  createSample();
		save(propertiesFile,p,"Riscoss tool data provider properties for XWiki");
		return p;
	}
	
	/**
	 * Initialisation here, will be passed by a config file
	 * @return
	 */
	public static Properties createSample() {

		// create and load default properties
		Properties config = new Properties();
		//		FileInputStream in = new FileInputStream("defaultProperties");
		//		defaultProps.load(in);
		//		in.close();
		
		//general properties
		
		config.setProperty("licenseFile", "./input/LicensesCfg.html");

		//manual setting of properties for XWiki
		config.setProperty("targetEntity", "xwiki");
		
		config.setProperty("targetFossology", "http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002"); //"./input/Bonita_Fossology.html";
		config.setProperty("targetMaven","./input/dependencies.html");
		config.setProperty("targetMarkmail","http://xwiki.markmail.org");
		
		config.setProperty("GitRepositoryURI","http://***");
		
		config.setProperty("JIRA_URL", "http://jira.xwiki.org");

		config.setProperty("JIRA_AnonymousAuthentication", "true"); // anonymous authentication: true if the authentication is anonymous, false otherwise.

		config.setProperty("JIRA_Username", "username"); // username to login into jira

		config.setProperty("JIRA_Password", "password"); // password to login into jira

		config.setProperty("JIRA_InitialDate", "2014/06/15"); // initial date to perform analysis (yyyy/mm/dd)
		
		config.setProperty("Sonar_resourceKey", "org.ow2.bonita:bonita-server");
		
		config.setProperty("Sonar_host","http://nemo.sonarqube.org");
		
		config.setProperty("Sonar_singleMetrics", "ncloc, complexity");
		
		config.setProperty("Sonar_historyMetrics","ncloc, comment_lines");
		
		config.setProperty("Indicators_XML", "./input/Indicators.xml");
		
		return config;
	}
	
	/**
	 * loads a configuration file defining properties
	 * @param propertiesFile
	 * @return
	 */
	public static Properties load(String propertiesFile, Properties defaultProperties) {
			
		Properties props;
		if (defaultProperties == null)
			props = new Properties();
		else
			props = new Properties(defaultProperties);
		
		try {
			InputStream in = RdpConfig.class.getResourceAsStream(propertiesFile);
//			FileInputStream in = new FileInputStream(propertiesFile);
			props.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//example access:
		//String licenseFile = properties.getProperty("licenseFile");
		return props;
	}
	
	/**
	 * loads a default configuration file, used each time (general) properties are missing in the actual properties file
	 * @param propertiesFile
	 * @return
	 */
	public static Properties loadDefaults(String propertiesFile) {
			
		Properties props = new Properties();
		try {
			InputStream in = RdpConfig.class.getResourceAsStream( propertiesFile );
//			FileInputStream in = new FileInputStream(propertiesFile);
			props.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//example access:
		//String licenseFile = properties.getProperty("licenseFile");
		return props;
	}
	
	/**
	 * saves a configuration file to disk
	 * @param propertiesFile the file name
	 * @param config the java properties
	 * @param comment a comment put in the file header
	 */
	public static void save(String propertiesFile, Properties config, String comment) {
		
		FileOutputStream output = null;
		try {
	 
			output = new FileOutputStream(propertiesFile);
			// save properties to project root folder
			config.store(output, comment);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
