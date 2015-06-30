package eu.riscoss.rdc;

import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public class RDCSonar_Test {
	public static void main( String[] args ) throws Exception {
		
		
		RDCFactory.get().registerRDC( new RDCSonar() );
		
		new RDCRunner().run( new String[] {
				"-entity=x",
				"-print",
				"-rdc=Sonar",
				"-Sonar_historyMetrics=ncloc, comment_lines",
				"-Sonar_by_file_Metrics=nclock, complexity",
				"-Sonar_singleMetrics=ncloc, duplicated_lines_density, line_coverage, tests",
				"-Sonar_resourceKey=org.xwiki.platform:xwiki-platform",
				"-Sonar_host=http://sonar.xwiki.org"
		} );
		
//		RDC rdc = new RDCSonar();
//		rdc.setParameter( "Sonar_historyMetrics", "ncloc, duplicated_lines_density, line_coverage, tests" );
//		rdc.setParameter( "Sonar_by_file_Metrics", "nclock, complexity" );
//		rdc.setParameter( "Sonar_singleMetrics", "ncloc, duplicated_lines_density, line_coverage, tests" );
//		rdc.setParameter( "Sonar_resourceKey", "org.xwiki.platform:xwiki-platform" );
//		rdc.setParameter( "Sonar_host", "http://sonar.xwiki.org" );
//		rdc.getIndicators( "" );
//		Map<String,RiskData> map = rdc.getIndicators( "" );
//		for( RiskData rd : map.values() ) {
//			System.out.println( rd.getId() + " = " + rd.getValue() );
//		}
	}
}
