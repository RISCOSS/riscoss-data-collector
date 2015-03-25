package eu.riscoss.rdc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

public class TextFile implements Iterable<String> {
	
	File file;
	
	static class LineIterator implements Iterator<String> {
		
		RandomAccessFile input;
		String string;
		
		LineIterator( File file ) {
			try {
				input = new RandomAccessFile( file, "r" );
				string = input.readLine();
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
		
		@Override
		public boolean hasNext() {
			return string != null;
		}
		
		@Override
		public String next() {
			String ret = string;
			try {
				string = input.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		@Override
		public void remove() {}
		
	}
	
	public TextFile( File file ) {
		this.file = file;
	}

	@Override
	public Iterator<String> iterator() {
		return new LineIterator( file );
	}
	
	public String asString() {
		StringBuilder b = new StringBuilder();
		for( String line : this ) {
			b.append( line );
		}
		return b.toString();
	}
}
