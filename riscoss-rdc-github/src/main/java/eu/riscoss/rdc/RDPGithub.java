package eu.riscoss.rdc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import eu.riscoss.dataproviders.RDR;
import eu.riscoss.dataproviders.RiskData;

public class RDPGithub {
	public static void main( String[] args ) {
		if( args.length < 1 ) {
			System.out.println( "Usage: java -jar <github_repository> [RDR]" );
			System.out.println( "E.g.:  java -jar RISCOSS/riscoss-analyser http://riscossplatform.ow2.org:8080/riscoss-rdr" );
			System.exit( 0 );
		}
		RDCGithub rdc = new RDCGithub();
		rdc.setParameter( "repository", args[0] );
		Map<String,RiskData> map = rdc.getIndicators();
		if( args.length > 1 ) {
			try {
				RDR.sendRiskData( args[1], new ArrayList<RiskData>( map.values() ) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			for( String key : map.keySet() ) {
				System.out.println( key + " = " + map.get( key ).getValue() );
			}
		}
	}
}
