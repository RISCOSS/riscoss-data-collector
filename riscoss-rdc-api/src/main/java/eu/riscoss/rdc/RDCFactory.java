package eu.riscoss.rdc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RDCFactory {
	
	private static RDCFactory instance = new RDCFactory();
	
	Map<String,RDC> rdcs = new HashMap<String,RDC>();
	
	public static RDCFactory get() {
		return instance;
	}
	
	private RDCFactory() {}
	
	public void registerRDC( RDC rdc ) {
		this.rdcs.put( rdc.getName(), rdc );
	}
	
	public Collection<RDC> listRDCs() {
		return rdcs.values();
	}

	public RDC getRDC( String name ) {
		return rdcs.get( name );
	}
}
