package eu.riscoss.rdc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class RDCTravisCI {
	
	static String test_url = "https://s3.amazonaws.com/archive.travis-ci.org/jobs/53921972/log.txt";
	
	public static void main( String[] args ) {
		System.out.println( download( test_url ) );
	}
	
	public RDCTravisCI() {
	}
	
	static String download( String url ) {
		try {
			byte[] ba1 = new byte[1024];
			int baLength;
			OutputStream fos1 = new ByteArrayOutputStream();
			
			URL url1 = new URL( url );
			InputStream is1 = url1.openStream();
			while ((baLength = is1.read(ba1)) != -1) {
				fos1.write(ba1, 0, baLength);
			}
			
			fos1.flush();
			fos1.close();
			is1.close();
			
			return fos1.toString();
		} catch (Exception npe) {
			return "";
		}
	}
}
