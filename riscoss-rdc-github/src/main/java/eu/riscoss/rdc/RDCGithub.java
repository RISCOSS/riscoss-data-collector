package eu.riscoss.rdc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCGithub implements RDC {
	
	static final String GITHUB_PREFIX = "github:repository-";
	
	static Map<String,String>		names = new HashMap<>(); //names published in the rdr 
	static Map<String,String>		keys = new HashMap<>();
	static Map<String,RDCParameter>	parameters = new HashMap<>();
	
	private HttpClient client = HttpClientBuilder.create().build();
	
	Map<String,String> values = new HashMap<>();
	
	static {
		//github-specific indicators (not hardcoded, depends on availability!)
		keys.put( "forks_count", "number" );
		keys.put( "open_issues_count", "number" );
		keys.put( "stargazers_count", "number" );
		keys.put( "created_at", "date" );
		keys.put( "subscribers_count", "number" );
		keys.put( "open_issues", "number" );
		keys.put( "watchers_count", "number" );
		keys.put( "size", "number" );
		keys.put( "has_wiki", "boolean" );
		keys.put( "updated_at", "date" );
		//keys.put( "license", "object" ); //OBJECT NOT IMPLEMENTED! --> boolean "has_license"
		//	github:repository-ci_link?		//to check!  
		//  github:repository-closed_issues? //not available
		
		
	}
	
	static {
		//github-specific indicators (not hardcoded, depends on availability!)
		for (Entry<String, String> entry : keys.entrySet()) {
			names.put( GITHUB_PREFIX + entry.getKey(), entry.getValue() );
		}
		
		
		names.put( GITHUB_PREFIX + "contributors", "number" );
		names.put( GITHUB_PREFIX + "contributions_sum", "number" );
		names.put( GITHUB_PREFIX + "ci_link", "boolean" );
		
		//hardcoded indicators
		names.put( GITHUB_PREFIX + "has_license", "boolean" ); //OBJECT NOT IMPLEMENTED!
		//general indicators (hardcoded)
		names.put( "size", "number" );
		
		parameters.put( "repository", new RDCParameter( "repository", "Repository name", "RISCOSS/riscoss-analyser", null ) );
	}
	
	@Override
	public String getName() {
		return "Github";
	}
	
	@Override
	public Collection<RDCParameter> getParameterList() {
		return parameters.values();
	}
	
	@Override
	public void setParameter(String parName, String parValue) {
		values.put( parName, parValue );
	}
	
	@Override
	public Map<String, RiskData> getIndicators( String entity ) {
		try {
			String json = getData();
			Map<String,RiskData> values = new HashMap<>();
			parseJson( json.substring(json.indexOf( "{" ) ), entity , values);
			json = getDataContributors();
			parseJsonContributors(json, entity , values);
			json = getDataContent();
			parseJsonContent(json, entity , values);
			return values;
		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new RuntimeException( e );
		}
	}
	
	Map<String,RiskData> parseJsonContent( String json, String entity, Map<String, RiskData> values ) {
		int hasTravis = 0;
		
		try {
			JSONAware jv = (JSONAware) new JSONParser().parse( json );
			if( jv instanceof JSONArray ) {
				JSONArray ja = (JSONArray)jv;

				for (Object o : ja){
					JSONObject jo = (JSONObject)o;
					//System.out.println(jo.get("contributions"));
					String filename = jo.get("name").toString();
					if (filename.equals(".travis.yml")) {
						hasTravis = 1;
						break;
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}	

		RiskData rd = new RiskData( GITHUB_PREFIX + "ci_link", entity, new Date(), RiskDataType.NUMBER, hasTravis );
		values.put( rd.getId(), rd );
		
		return values;
	}
	
	
	
	Map<String,RiskData> parseJsonContributors( String json, String entity, Map<String, RiskData> values ) {
		int contributions = 0;
		int contributors = 0;
		try {
			JSONAware jv = (JSONAware) new JSONParser().parse( json );
			if( jv instanceof JSONArray ) {
				JSONArray ja = (JSONArray)jv;
				
				contributors = ja.size();
				
				for (Object o : ja){
					JSONObject jo = (JSONObject)o;
					//System.out.println(jo.get("contributions"));
					contributions += Integer.parseInt(jo.get("contributions").toString());
				}
			}	
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		RiskData rd = new RiskData( GITHUB_PREFIX + "contributors", entity, new Date(), RiskDataType.NUMBER, contributors );
		values.put( rd.getId(), rd );
		rd = new RiskData( GITHUB_PREFIX + "contributions_sum", entity, new Date(), RiskDataType.NUMBER, contributions );
		values.put( rd.getId(), rd );

		return values;
	}
	
	Map<String,RiskData> parseJson( String json, String entity, Map<String, RiskData> values ) {
		try {
			JSONAware jv = (JSONAware) new JSONParser().parse( json );
			if( jv instanceof JSONObject ) {
				JSONObject jo = (JSONObject)jv;
				for( Object key : jo.keySet() ) {
					//System.out.println(key+" \t"+jo.get(key) );
					if( keys.keySet().contains( key.toString() ) && (jo.get( key )!=null)) {
						String value = jo.get( key ).toString();

						if( "number".equals( keys.get( key.toString() ) ) ) {
							try {
								double d = Double.parseDouble( value );
								RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, d );
								values.put( rd.getId(), rd );

								//hard-coded size value
								if (key.toString().equals("size")){
									rd = new RiskData( "size", entity, new Date(), RiskDataType.NUMBER, d );
									values.put( rd.getId(), rd );
								}

							} 
							catch( Exception ex ) {
								ex.printStackTrace();
							}


						}
						else if( "boolean".equals( keys.get( key.toString() ) ) ) {
							try {
								boolean b = Boolean.parseBoolean( value );
								RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, (b ? 1 : 0) );
								values.put( rd.getId(), rd );
							}
							catch( Exception ex ) {
								ex.printStackTrace();
							}
						}
						else if( "date".equals( keys.get( key.toString() ) ) ) {
							try {
								value = value.replaceAll( "T", " " );
								SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd H:m:s" );
								Date date = formatter.parse( value );
								RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, date.getTime() );
								values.put( rd.getId(), rd );
							}
							catch( Exception ex ) {
								ex.printStackTrace();
							}
						}
						//object currently not implemented in the RDR
						//implementation: hardcoded, as boolean, adding "has_" (see below)
						//							else if( "object".equals( keys.get( key.toString() ) ) ) {
						//									RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, 1 );
						//									values.put( rd.getId(), rd );
						//								
						//							}

					}
					if (key.toString().equals("license")){
						RiskData rd;
						if (jo.get( key ) == null ) 
							rd = new RiskData( GITHUB_PREFIX + "has_license", entity, new Date(), RiskDataType.NUMBER, 0 );
						else
							rd = new RiskData( GITHUB_PREFIX + "has_license", entity, new Date(), RiskDataType.NUMBER, 1 );

						values.put( rd.getId(), rd );
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return values;
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return names.keySet();
	}

	String getData() throws org.apache.http.ParseException, IOException {
		String repository = values.get( "repository" );
		HttpGet get = new HttpGet( "https://api.github.com/repos/" + repository );
		get.setHeader("Accept", "application/vnd.github.drax-preview+json");

		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String ret = EntityUtils.toString(entity);
			return ret;
		} else {
			// something has gone wrong...
			return response.getStatusLine().toString();
		}
	}

	String getDataContributors() throws org.apache.http.ParseException, IOException {
		String repository = values.get( "repository" );
		HttpGet get = new HttpGet( "https://api.github.com/repos/" + repository + "/contributors");
		
		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String ret = EntityUtils.toString(entity);
			return ret;
		} else {
			// something has gone wrong...
			return response.getStatusLine().toString();
		}
	}
	
	String getDataContent() throws org.apache.http.ParseException, IOException {
		String repository = values.get( "repository" );
		HttpGet get = new HttpGet( "https://api.github.com/repos/" + repository + "/contents");
		
		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String ret = EntityUtils.toString(entity);
			return ret;
		} else {
			// something has gone wrong...
			return response.getStatusLine().toString();
		}
	}
}
