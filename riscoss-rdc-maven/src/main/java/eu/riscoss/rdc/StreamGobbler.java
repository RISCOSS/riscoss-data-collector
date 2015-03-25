package eu.riscoss.rdc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;


public class StreamGobbler extends Thread {
	
	InputStream			is;
	OutputStream		os;
	
	StringBuilder b = new StringBuilder();
	
	boolean done = false;
	
	StreamGobbler( InputStream is, String type ) {
		this( is, type, null );
	}
	
	StreamGobbler( InputStream is, String type, OutputStream redirect ) {
		this.is = is;
		this.os = redirect;
	}
	
	public void run() {
		done = false;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			PrintWriter pw = null;
			if( os != null )
				pw = new PrintWriter( os );
			
			isr = new InputStreamReader( is );
			br = new BufferedReader( isr );
			String line = null;
			while( (line = br.readLine()) != null ) {
				if( pw != null ) {
					pw.println( line );
				}
				else {
					b.append( line );
					b.append( "\n" );
				}
			}
			if( pw != null )
				pw.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			if( isr != null ) try {
				isr.close();
				} catch (IOException e) {}
			
			if( br != null ) try{
				br.close();
			}
			catch( IOException ex ){}
		}
		done = true;
	}
	
	public String asString() {
		while( ! done )
			try {
				Thread.sleep( 10 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		return b.toString();
	}
}