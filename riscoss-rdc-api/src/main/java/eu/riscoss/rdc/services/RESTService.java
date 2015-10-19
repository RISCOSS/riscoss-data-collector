package eu.riscoss.rdc.services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RESTService {
	
	protected RiscossRESTClient rest;
	
	public RESTService( RiscossRESTClient rest ) {
		this.rest = rest.clone();
	}
	
	protected String encode( String str ) {
		try {
			return URLEncoder.encode( str, "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException( e );
		}
	}
	
	public String getDomain() {
		return encode( rest.get( "domain", "" ) );
	}
	
}
