package eu.riscoss.rdc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.riscoss.dataproviders.RDR;
import eu.riscoss.dataproviders.RiskData;


public class RDCRunner {
	
	public void run(String[] args) throws Exception {
		
		Map<String,String> m = parseCmdLine( args );
		
		for( String key : m.keySet() ) {
			System.out.println( key + " = " + m.get( key ) );
		}
		
		if( m.get( "-info" ) != null ) {
			JSONArray outArray = new JSONArray();
			for( RDC rdc : RDCFactory.get().listRDCs() ) {
				JSONObject jrdc = new JSONObject();
				try {
					jrdc.put( "name", rdc.getName() );
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				JSONArray jargs = new JSONArray();
				for( RDCParameter par : rdc.getParameterList() ) {
					JSONObject jpar = new JSONObject();
					try {
						jpar.put( "name",  par.getName() );
						jpar.put( "description",  par.getDescription() );
						jpar.put( "defaultValue",  par.getDefaultValue() );
						jpar.put( "example",  par.getExample() );
					} catch (JSONException e) {
						e.printStackTrace();
					}
					jargs.put( jpar );
				}
				try {
					jrdc.put( "parameters", jargs );
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				JSONArray jinds = new JSONArray();
				for( String name : rdc.getIndicatorNames() ) {
					jinds.put( name );
				}
				try {
					jrdc.put( "indicators", jinds );
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				outArray.put( jrdc );
			}
			System.out.println("-----BEGIN CONFIGURATION DATA-----");
			System.out.println( outArray.toString() );
			System.out.println("-----END CONFIGURATION DATA-----");
			return;
		}
		
		JSONObject input = null;;
		if( args.length > 0 && "--stdin-conf".equals(args[ args.length - 1] ) ) {
			String stdin = IOUtils.toString(System.in, "UTF-8");
			input = new JSONObject(stdin);
		} else {
			input = new JSONObject();
		}

		String entity = m.get( "-entity" );
		if( entity == null ) { entity = input.optString("targetEntity"); }
		if( entity == null ) {
			printUsage();
			return;
		}
		
		RDC rdc = null;
		if( m.get( "-rdc" ) == null ) {
			printUsage();
			System.exit( 0 );
		}
		else {
			rdc = RDCFactory.get().getRDC( m.get( "-rdc" ) );
			if( rdc == null ) {
				System.out.println( "Unknown RDC name: " + m.get( "-rdc" ) );
				System.exit( 0 );
			}
		}
		
		Collection<RDCParameter> pars = rdc.getParameterList();
		for( RDCParameter p : pars ) {
			String val = null;
			try {
				val = input.getString( p.getName() );
			} catch( Exception ex ) {
				val = m.get( "-" + p.getName() );
			}
			if( val == null ) {
				val = p.getDefaultValue();
			}
			if( val == null ) {
				System.out.println( "Missing parameter: -" + p.getName() );
				System.exit( 0 );
			}
			try {
				rdc.setParameter( p.getName(), val );
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
		
		Map<String,RiskData> ret = rdc.getIndicators( entity );
		
		if( m.get( "-print" ) != null ) {
			JSONArray outArray = new JSONArray();
			String value = "";
			for( String key : ret.keySet() ) try {
				JSONObject outObj = new JSONObject();
				RiskData rd = ret.get( key );
				outObj.put("id", rd.getId() );
				outObj.put("target", entity );
				value = rd.getValue().toString();
				outObj.put( "value", value );
				outObj.put( "type", rd.getType() );
				outArray.put(outObj);
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			System.out.println("-----BEGIN RISK DATA-----");
			System.out.println(outArray.toString());
			System.out.println("-----END RISK DATA-----");
		}
		
		if( m.get( "-rdr" ) != null ) {
			String url = m.get( "-rdr" );
			try {
				RDR.sendRiskData( url, new ArrayList<RiskData>( ret.values() ) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	Map<String,String> parseCmdLine( String[] args ) {
		Map<String,String> map = new HashMap<String,String>();
		for( String arg : args ) {
			if( arg.startsWith( "-" ) ) {
				int n = arg.indexOf( "=" );
				if( n != -1 ) {
					String p1 = arg.substring( 0, n );
					String p2 = arg.substring( n+1 );
					map.put( p1, p2 );
				}
				else {
					map.put( arg, "true" );
				}
			}
		}
		return map;
	}
	
	String input( String msg ) {
		System.out.print( msg );
		Scanner in = new Scanner(System.in);
		String str = in.next();
//		in.close();
		return str;
	}
	
	void printUsage() {
		System.out.println( "Usage: java -jar <rdp.jar> [-info | -entity=<entity> [-print] [-rdr=<rdrUrl>] [-i] [-rdc=rdcName] [-<par_namr>=<par_value> [...] ] ]" );
		System.out.println( "-info Prints the list of availabe RDCs, the indicators they provide and the parameters they require" );
		System.out.println( "-entity Entity id" );
		System.out.println( "-print Print on stdout" );
		System.out.println( "-rdr Url of RDR repository" );
//		System.out.println( "-i interactive mode (asks for missing values - still work in progress)" );
		System.out.println( "--stdin-conf Read parameters from stdin" );
	}
	
	protected static String formatType( String value ) {
		String type = "NUMBER";
		try {
			Double.valueOf(value);

		} catch (NumberFormatException e) {
			type = "STRING";
		}

		return type;
	}
}
