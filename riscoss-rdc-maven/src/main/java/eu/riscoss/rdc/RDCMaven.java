package eu.riscoss.rdc;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCMaven implements RDC {
	
	static Set<String>				names = new HashSet<>();
	static Map<String,RDCParameter>	parameters = new HashMap<>();
	static Map<String,String>		values = new HashMap<>();
	
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
	public Map<String, RiskData> getIndicators() {
		
		String cmdline = "curl " + createUrl();
		Executable e = new Executable( cmdline ).exec();
		try {
			// to avoid a bug in StreamGobbler
			Thread.sleep( 100 );
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String result = e.getOutput();
//		String result = new Executable( cmdline ).exec().getOutput();
		return parsePom( result, values.get( "artifactId" ) );
	}
	
	Map<String,RiskData> parsePom( String string, String repository ) {
		Map<String,RiskData> values = new HashMap<>();
		
		XmlNode xml = XmlNode.loadString( string );
		XmlNode x = xml.item( "licenses" ).item( "license" ).item( "name" );
		if( x.exists() ) {
			values.put( "license", new RiskData( "license", repository, new Date(), RiskDataType.NUMBER, x.getValue() ) );
		}
		
		return values;
	}
	
	@Override
	public Collection<String> getIndicatorNames() {
		return names;
	}
}
