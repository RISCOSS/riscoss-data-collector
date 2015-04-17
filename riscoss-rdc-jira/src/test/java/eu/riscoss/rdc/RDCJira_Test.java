package eu.riscoss.rdc;

import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public class RDCJira_Test {
	public static void main( String[] args ) throws Exception {
		
		RDCFactory.get().registerRDC( new RDCJira() );
		
		new RDCRunner().run( new String[] {
				"-info",
				"-rdc=Jira",
		} );
		
		new RDCRunner().run( new String[] {
				"-entity=x",
				"-print",
				"-rdc=Jira",
				"-JIRA_URL=https://jira.ow2.org",
				"-JIRA_AnonymousAuthentication=true",
				"-JIRA_Username=username",
				"-JIRA_Password=password",
				"-JIRA_Project=EZB"
		} );
		
//		RDC rdc = new RDCJira();
//		rdc.setParameter( "JIRA_URL", "https://jira.ow2.org" );
//		rdc.setParameter( "JIRA_AnonymousAuthentication", "true" );
//		rdc.setParameter( "JIRA_Username", "username" );
//		rdc.setParameter( "JIRA_Password", "password" );
//		rdc.setParameter( "JIRA_Project", "EZB" );
//		rdc.getIndicators( "" );
//		Map<String,RiskData> map = rdc.getIndicators( "" );
//		for( RiskData rd : map.values() ) {
//			System.out.println( rd.getId() + " = " + rd.getValue() );
//		}
	}
}
