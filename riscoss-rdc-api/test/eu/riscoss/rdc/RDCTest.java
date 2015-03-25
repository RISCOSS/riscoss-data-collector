package eu.riscoss.rdc;

import java.util.HashMap;
import java.util.Map;

public class RDCTest {
	public static void main( String[] args ) {
		RDC rdc = RDCFactory.get().getRDC( "Fossology" );
		rdc.getIndicators( "xwiki" );
		Map<String,String> parameters = new HashMap<>();
		
		parameters.put( "targetEntity", "XWiki" );
		parameters.put( "targetFossology", "http://fossology.ow2.org/?mod=nomoslicense&upload=38&item=292002" );
		parameters.put( "fossologyScanType", "filelist" );
		parameters.put( "targetFossologyList", "http://fossology.ow2.org/?mod=license-list&upload=38&item=292002&output=dltext" );
		
		try {
			for( String key : parameters.keySet() ) {
				rdc.setParameter( key, parameters.get( key ) );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
