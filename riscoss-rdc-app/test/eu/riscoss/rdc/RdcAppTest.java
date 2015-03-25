package eu.riscoss.rdc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public class RdcAppTest {
	public static void main( String[] args ) {
		new RdcAppTest().run( args );
	}
	
	public void run(String[] args) {
		
		RDCFactory.get().registerRDC( new RDCFossology( "Fossology" ) );
		RDCFactory.get().registerRDC( new RDCMaven( "Maven" ) );
		RDCFactory.get().registerRDC( new RDCMarkmail( "Markmail" ) );
		RDCFactory.get().registerRDC( new RDCJira( "Jira" ) );
		RDCFactory.get().registerRDC( new RDCSonar( "Sonar" ) );
		
		System.out.println( "Available Risk Data Collectors:" );
		for( RDC rdc : RDCFactory.get().listRDCs() ) {
			System.out.println( "- " + rdc.getName() );
		}
		RDC fossology = RDCFactory.get().getRDC( "Fossology" );
		System.out.println( "Creating Fossology instance: OK" );
		System.out.println( "Required parameters:" );
		for( RDCParameter par : fossology.getParameterList() ) {
			System.out.println( "> " + par.getName() + " [description=" + par.getDescription()  + ", defaultValue=" + par.getDefaultValue() + ", example=" + par.getExample() + "]" );
		}
		
		Map<String,String> map = new HashMap<String,String>();
		for( RDCParameter par : fossology.getParameterList() ) {
			System.out.println( "SET " + par.getName() + " = ''" );
			map.put( par.getName(), "" );
		}
		System.out.println( "Running Fossology RDC" );
		runRDC( "Fossology", "XWiki", map );
	}
	
	
	protected void runRDC( String rdcName, String te, Map<String,String> args ) {
		Collection<RDCParameter> map = RDCFactory.get().getRDC( rdcName ).getParameterList();
		RDC rdc = RDCFactory.get().getRDC( rdcName );
		if( rdc == null ) return;
		for( RDCParameter par : map ) {
			try {
				rdc.setParameter( par.getName(), args.get( par.getName() ) );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Task task = new Task() {
			@Override
			public void run() {
				String rdcName = get( "rdc-name", null );
				if( rdcName == null ) return;
				String te = get( "entity-name", null );
				if( te == null ) return;
				Map<String,RiskData> map = RDCFactory.get().getRDC( rdcName ).getIndicators( te );
				for( String key : map.keySet() ) {
					String s = map.get( key ).getValue().toString();
					System.out.println( "SET " + key + " = " + s );
				}
				//				getPage().setAttribute( "a:rdc", "text", "RDC" );
			} };
		task.put( "rdc-name", rdcName );
		task.put( "entity-name", te );
		new Thread( task ).start();
			
//		retrieveIndicators( rdcName );
	}
}
