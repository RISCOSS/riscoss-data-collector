/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Mirko Morandini
**/


package eu.riscoss.rdc;

import java.util.Map;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.riscoss.rdc.services.RDRService;
import eu.riscoss.rdc.services.RiscossRESTClient;
//import eu.riscoss.agent.usecases.GithubTest_Corporate;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.rdc.RDCParameter;

public class CorporateRDCRunnerApp {

	private static Properties common_properties = new Properties();
	private static Properties rdc_properties = new Properties();

	/**
	 * Application for running RDCs and store data in a remote corporate RISCOSS platform 
	 * @param args
	 */
	public static void main( String[] args ) {	

		common_properties.setProperty("rdc", "RDCGithub" );  //RDC rdc = new RDCGithub();
		
		//where to send the resulting JSon
		common_properties.setProperty("platform_addr", "http://riscoss.fbk.eu/riscoss-webapp-0.3.0-BCN");// "http://127.0.0.1:8888");
		common_properties.setProperty("platform_username", "admin");
		common_properties.setProperty("platform_pwd", "admin");

		//this domain needs to be present in the platform
		common_properties.setProperty("domain", "FBK");
		common_properties.setProperty("entity", "E1");  //e.g. xwiki, riscoss,..
		
		//define the properties needed for the RDC
		//here: github proberties
		rdc_properties.setProperty("repository", "RISCOSS/riscoss-data-collector");
		rdc_properties.setProperty("userpwd", "");  //form user:pwd
		
		try {
			new CorporateRDCRunnerApp().run( );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}	
	}
	
	
	public void run() throws Exception {

		RiscossRESTClient rest = new RiscossRESTClient( common_properties.getProperty("platform_addr") );
		
		String rdcToBeLoaded = "eu.riscoss.rdc."+common_properties.getProperty("rdc");
        Class rdcClass = ClassLoader.getSystemClassLoader().loadClass(rdcToBeLoaded);
        RDC rdc = (RDC) rdcClass.newInstance();
		
        //////////////////////
		rest.login( common_properties.getProperty("platform_username"), common_properties.getProperty("platform_pwd") );
		rest.set( "domain", common_properties.getProperty("domain") ); //rest.domain( domain );
		//////////////////////
		String json = runRDC(rdc, common_properties.getProperty("entity"));
		System.out.println("json: "+json);
		new RDRService(rest).store(json);
	}
	

	public String runRDC(RDC rdc, String entityName) {

		JsonArray array = new JsonArray();

		for (RDCParameter rdcparam : rdc.getParameterList()) {
			rdc.setParameter(rdcparam.getName(), (String)rdc_properties.getProperty(rdcparam.getName()));
		}
		
		try {
			Map<String, RiskData> values = rdc.getIndicators(entityName);
			if (values == null)
				throw new Exception("The RDC '" + rdc.getName() + "' returned an empty map for the entity '" + entityName + "'");
			
			for (String key : values.keySet()) {
				RiskData rd = values.get(key);
				
				JsonObject o = new JsonObject();
				o.addProperty("id", rd.getId());
				o.addProperty("target", rd.getTarget());
				o.addProperty("value", rd.getValue().toString());
				o.addProperty("type", rd.getType().toString());//??
				o.addProperty("date", rd.getDate().getTime());
				//datatype???
				//origin???
				array.add(o);
			}
						
		} catch (Exception ex) {
			ex.printStackTrace();
			String msg = "Some data were not gathered and/or stored in the RDR";
			JsonObject json = new JsonObject();
			json.addProperty("msg", msg);
			
			json.addProperty("result", "error");
			json.addProperty("error-message", ex.getMessage());
			array.add(json);  //TODO
		}

		return array.toString();
	}
	
}
