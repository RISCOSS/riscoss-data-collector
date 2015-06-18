package eu.riscoss.rdc;

public class RDCOlex_Test {
	public static void main( String[] args ) throws Exception {
		
		RDCFactory.get().registerRDC( new RDCOlex() );
		
		new RDCRunner().run( new String[] {
				"-entity=Struts",
//				"-print",
				"-rdc=Olex",
				"-targetOlex=http://olex.openlogic.com/packages/opensimmpls",
				"-rdr=http://riscossplatform.ow2.org/riscoss-rdr/"
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
