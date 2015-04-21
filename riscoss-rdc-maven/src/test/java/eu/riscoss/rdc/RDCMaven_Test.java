package eu.riscoss.rdc;


public class RDCMaven_Test {
	public static void main( String[] args ) throws Exception {
		
		RDCFactory.get().registerRDC( new RDCMaven() );
		
		new RDCRunner().run( new String[] {
				"-info"
		} );
		
		new RDCRunner().run( new String[] {
				"-entity=x",
				"-print",
				"-rdc=Maven",
				"-groupId=jsoup",
				"-artifactId=jsoup",
				"-version=1.8.1"
		} );
	}
}
