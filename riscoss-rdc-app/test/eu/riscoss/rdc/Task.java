package eu.riscoss.rdc;

import java.util.HashMap;
import java.util.Map;

abstract class Task implements Runnable {
	
	Map<String,Object> map = new HashMap<String,Object>();
	
	public Task() {}
	
	public void put( String key, Object value ) {
		map.put( key, value );
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get( String key, T def ) {
		try {
			return (T)map.get( key );
		}
		catch( Exception ex ) {
			return def;
		}
	}
}
