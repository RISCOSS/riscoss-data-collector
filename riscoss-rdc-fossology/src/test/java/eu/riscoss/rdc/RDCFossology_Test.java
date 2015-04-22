package eu.riscoss.rdc;

public class RDCFossology_Test {
	public static void main( String[] args ) throws Exception {
		
		RDCFactory.get().registerRDC( new RDCFossology() );
		
		new RDCRunner().run( new String[] {
				"-entity=x",
				"-print",
				"-rdc=Fossology",
				"-fossologyScanType=overview",
				"-targetFossologyList=http://fossology.ow2.org/?mod=license-list&upload=38&item=292002&output=dltext",
				"-targetFossology=http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002"
		} );
		
//		RDC rdc = new RDCFossology();
//		rdc.setParameter( "targetFossologyList", "http://fossology.ow2.org/?mod=license-list&upload=38&item=292002&output=dltext" );
//		rdc.setParameter( "targetFossology", "http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002" );
//		Map<String,RiskData> map = rdc.getIndicators( "" );
//		for( RiskData rd : map.values() ) {
//			System.out.println( rd.getId() + " = " + rd.getValue() );
//		}
	}
}
