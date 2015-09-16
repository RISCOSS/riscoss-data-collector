package eu.riscoss.rdc;

public class RDCOlex_Test {
	public static void main( String[] args ) throws Exception {
		
		RDCFactory.get().registerRDC( new RDCOlex() );
		
		new RDCRunner().run( new String[] {
				"-entity=Struts",
			"-print",
				"-rdc=Olex",
				"-targetOlex=http://olex.openlogic.com/packages/opensimmpls",
				"-rdr=http://riscossplatform.ow2.org/riscoss-rdr/"
		} );
		
	}
}
