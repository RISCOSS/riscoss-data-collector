package eu.riscoss.rdc;

public class RDCOmm_Test {
	public static void main( String[] args ) throws Exception {
		
		RDCFactory.get().registerRDC( new RDCOmm() );
		
		new RDCRunner().run( new String[] {
				"-entity=ASM",
				"-print",
				"-rdc=Omm",
				"-targetOmm=http://www.ow2.org/xwiki/bin/download/ActivitiesDashboard/ASM/OMM4RI.ASM.csv",
				"-rdr=http://riscossplatform.ow2.org/riscoss-rdr/"
		} );
	}
}
