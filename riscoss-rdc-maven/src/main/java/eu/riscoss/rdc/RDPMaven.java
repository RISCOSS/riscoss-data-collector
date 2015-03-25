package eu.riscoss.rdc;

public class RDPMaven {
	public static void main( String[] args ) {
		RDCMaven rdc = new RDCMaven();
		try {
			rdc.setParameter( "groupId", "jsoup" );
			rdc.setParameter( "artifactId", "jsoup" );
			rdc.setParameter( "version", "1.8.1" );
			rdc.setParameter( "type", "pom" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		rdc.getIndicators();
	}
}
