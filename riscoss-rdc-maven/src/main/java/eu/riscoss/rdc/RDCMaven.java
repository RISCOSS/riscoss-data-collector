package eu.riscoss.rdc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCMaven implements RDC {
	
	static Set<String>				names = new HashSet<>();
	static Map<String,RDCParameter>	parameters = new HashMap<>();
	static Map<String,String>		license_mapping = new HashMap<>();
	
	Map<String,String>				values = new HashMap<>();
	
	private HttpClient client = HttpClientBuilder.create().build();
	
	static {
		names.add( "#mit" );
		names.add( "#bsd4" );
		names.add( "#bsd3" );
		names.add( "#asl1" );
		names.add( "#asl2" );
		names.add( "#artistic2" );
		names.add( "#lgpl2.1" );
		names.add( "#lgpl2.1+" );
		names.add( "#lgpl3+" );
		names.add( "#mpl" );
		names.add( "#cddl" );
		names.add( "#cpl-epl" );
		names.add( "#eupl" );
		names.add( "#gpl2" );
		names.add( "#gpl2+" );
		names.add( "#gpl3" );
		names.add( "#agpl3" );
		
		// <repositoryUrl>/<groupId>/<artifactId>/<version>/<artifactId>-<version>.<type>
		parameters.put( "groupId", new RDCParameter( "groupId", "groupId", "jsoup", "" ) );
		parameters.put( "artifactId", new RDCParameter( "artifactId", "artifactId", "jsoup", "" ) );
		parameters.put( "version", new RDCParameter( "version", "version", "1.8.1", "" ) );
		
		InputStream is = RDCMaven.class.getResourceAsStream( "spdx_license_list.txt" );
		Scanner scanner = new Scanner( is );
		while( scanner.hasNext() ) {
			String line = scanner.next();
			String[] tok = line.split( "[\t]" );
			if( tok.length < 3 ) continue;
			String id = tok[0];
			for( int i = 2; i < tok.length; i++ ) {
				license_mapping.put( tok[i], id );
			}
		}
		scanner.close();
		
		license_mapping.put( "The MIT License", "#mit" );
		license_mapping.put( "Mozilla Public License Version 1.0", "#pml" );
		license_mapping.put( "The Apache Software License, Version 2.0", "#apl2" );
		
//		parameters.put( "type", new RDCParameter( "type", "type", "", "" ) );
	}
	
	@Override
	public String getName() {
		return "Maven";
	}
	
	@Override
	public Collection<RDCParameter> getParameterList() {
		return parameters.values();
	}
	
	@Override
	public void setParameter(String parName, String parValue) {
		values.put( parName, parValue );
	}
	
	String createUrl() {
		// <repositoryUrl>/<groupId>/<artifactId>/<version>/<artifactId>-<version>.<type>
		// http://central.maven.org/maven2/org/jsoup/jsoup/1.8.1/jsoup-1.8.1.pom
		String url = "";
		url += "http://central.maven.org/maven2/org";
		url += "/" + values.get( "groupId" );
		url += "/" + values.get( "artifactId" );
		url += "/" + values.get( "version" );
		url += "/" + values.get( "artifactId" ) + "-" + values.get( "version" );
		url += ".pom"; // + values.get( "type" );
		return url;
	}
	
	@Override
	public Map<String, RiskData> getIndicators( String entity ) {
		
		String json;
		try {
			json = getData();
			return parsePom( json, entity );
		} catch (org.apache.http.ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return new HashMap<String,RiskData>();
	}
	
	Map<String,RiskData> parsePom( String string, String entity ) {
		Map<String,RiskData> values = new HashMap<>();
		
		XmlNode xml = XmlNode.loadString( string );
		XmlNode xlicenses = xml.item( "licenses" );
		for( XmlNode xlicense : xlicenses.getChildren( "license" ) ) {
			XmlNode x = xlicense.item( "name" );
			if( !x.exists() ) continue;
			String license = license_mapping.get( x.getValue() );
			if( license == null ) continue;
			if( "".equals( license ) ) continue;
			RiskData rd = values.get( license );
			if( rd == null ) {
				rd = new RiskData( license, entity, new Date(), RiskDataType.NUMBER, "1" );
			}
			else try {
				String oldval = rd.getValue().toString();
				int n = Integer.parseInt( oldval );
				n++;
				rd.setValue( "" + n );
			}
			catch( Exception ex ) { ex.printStackTrace(); }
			values.put( license, rd );
		}
		
		return values;
	}
	
	@Override
	public Collection<String> getIndicatorNames() {
		return names;
	}
	
	String getData() throws org.apache.http.ParseException, IOException {
		String url = "";
		url += "http://central.maven.org/maven2/org";
		url += "/" + values.get( "groupId" );
		url += "/" + values.get( "artifactId" );
		url += "/" + values.get( "version" );
		url += "/" + values.get( "artifactId" ) + "-" + values.get( "version" );
		url += ".pom";
		HttpGet get = new HttpGet( url );
		
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
