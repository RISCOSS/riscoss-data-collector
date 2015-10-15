package eu.riscoss.rdc;

import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public class RDCGithub_Test {
	public static void main( String[] args ) throws Exception {
		
		RDCFactory.get().registerRDC( new RDCGithub() );
		
		new RDCRunner().run( new String[] {
				"-entity=x",
				"-print",
				"-rdc=Github",
				"-repository=RISCOSS/riscoss-analyser"
				//"-repository=RISCOSS/riscoss-data-collector"  // Mirkk/GOBRepo"xwiki/xwiki-platform
				//"-repository=xwiki/xwiki-platform"

		} );
		
//		RDC rdc = new RDCGithub();
//		rdc.setParameter( "repository", "RISCOSS/riscoss-analyser" );
//		Map<String,RiskData> map = rdc.getIndicators( "" );
//		for( RiskData rd : map.values() ) {
//			System.out.println( rd.getId() + " = " + rd.getValue() );
//		}
	}
}
