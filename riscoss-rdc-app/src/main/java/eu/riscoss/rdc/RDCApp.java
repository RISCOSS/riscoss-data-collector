package eu.riscoss.rdc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import eu.riscoss.dataproviders.RiskData;

public class RDCApp {
	public static void main( String[] args ) {
		new RDCApp().run( args );
	}
	
	public void run(String[] args) {
		Map<String,String> m = parseCmdLine( args );
		
		RDCFactory.get().registerRDC( new RDCFossology() );
		RDCFactory.get().registerRDC( new RDCGithub() );
		RDCFactory.get().registerRDC( new RDCMaven() );
		RDCFactory.get().registerRDC( new RDCMarkmail( "Markmail" ) );
		RDCFactory.get().registerRDC( new RDCJira( "Jira" ) );
		RDCFactory.get().registerRDC( new RDCSonar( "Sonar" ) );
		
		RDC rdc = null;
		if( m.get( "-rdc" ) == null ) {
			if( m.get( "-i" ) == null ) {
				printUsage();
				System.exit( 0 );
			}
			else {
				String rdcname = input( "Enter RDC name: " );
				rdc = RDCFactory.get().getRDC( rdcname );
			}
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
			String val = m.get( "-" + p.getName() );
			if( val == null ) {
				val = p.getDefaultValue();
			}
			if( val == null ) {
				if( m.get( "-i" ) == null ) {
					System.out.println( "Missing parameter: -" + p.getName() );
					System.exit( 0 );
				}
				val = input( "Enter value for '" + p.getName() + "'" );
				try {
					rdc.setParameter( p.getName(), val );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		Map<String,RiskData> ret = rdc.getIndicators();
		
		if( m.get( "-i" ) != null ) {
			for( String key : ret.keySet() ) {
				System.out.println( key + " = " + ret.get( key ) );
			}
		}
	}
	
	Map<String,String> parseCmdLine( String[] args ) {
		Map<String,String> map = new HashMap<>();
		for( String arg : args ) {
			if( arg.startsWith( "-" ) ) {
				if( arg.indexOf( "=" ) != -1 ) {
					String[] parts = arg.split( "[=]" );
					map.put( parts[0], parts[1] );
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
		System.out.println( "Usage: java -jar <rdp.jar> [-i]" );
		System.out.println( "-i interactive mode" );
	}
}
