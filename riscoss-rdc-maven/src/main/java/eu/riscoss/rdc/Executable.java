package eu.riscoss.rdc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class Executable implements Runnable {
	public interface Listener {
		void processTerminated( Executable e );
	}
	
	StreamGobbler	outGobbler	= null;
	StreamGobbler	errGobbler	= null;
	
	String			commandline;
	File			outputFile;
	
	Listener		listener;
	
	boolean			wait		= true;
	
	long			upperTime = -1;
	
	ArrayList<String> args = new ArrayList<String>();
	
	
	public Executable( File command )
	{
		this.commandline = command.getAbsolutePath();
	}
	
	public Executable( String commandline )
	{
		this.commandline = commandline;
	}
	
	public void setLocking( boolean b )
	{
		this.wait = b;
	}
	
	public void redirectOutput( File file )
	{
		this.outputFile = file;
	}
	
	public File getOutputFile()
	{
		return outputFile;
	}
	
	@Override
	public void run() {
		_run();
	}
	
	public void run( Executable.Listener listener )
	{
		this.listener = listener;
		
		_run();
	}
	
	static class ProcessKiller implements Runnable
	{
		long time = 0;
		Process process;
		
		ProcessKiller( Process p, long time )
		{
			this.process = p;
			this.time = time;
		}
		
		@Override
		public void run()
		{
			try
			{
				Thread.sleep( time );
			}
			catch( InterruptedException e )
			{
				return;
			}
			
			try
			{
				process.exitValue();
			}
			catch( IllegalThreadStateException ex )
			{
				process.destroy();
				System.out.print( "(killed)" );
			}
		}
	}
	
	private void _run()
	{
		try
		{
			Process p = Runtime.getRuntime().exec( mkCmdLine() );
			
			outGobbler = outputFile != null ? new StreamGobbler( 
					p.getInputStream(), "OUTPUT", new FileOutputStream( outputFile.getAbsoluteFile() ) ) : 
					new StreamGobbler( p.getInputStream(), "OUTPUT" );
			
			errGobbler = new StreamGobbler( p.getErrorStream(), "ERROR" );
			
			outGobbler.start();
			errGobbler.start();
			
			Thread killerThread = null;
			
			if( upperTime > 0 )
			{
				ProcessKiller killer = new ProcessKiller( p, upperTime );
				
				killerThread = new Thread( killer );
				
				killerThread.start();
			}
			
			if( wait == true ) {
				// int exitVal =
				p.waitFor();
			}
			
			if( killerThread != null ) {
				killerThread.interrupt();
			}
			
			if( this.listener != null )
			{
				this.listener.processTerminated( this );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Executable exec() {
		System.out.println( "> " + this.commandline );
		_run();
		return this;
	}
	
	public InputStream getOutputStream() throws FileNotFoundException
	{
		if( outputFile != null )
		{
			return new FileInputStream( outputFile );
		} else
		{
			return new ByteArrayInputStream( outGobbler.b.toString().getBytes() );
		}
	}
	
	public void addArgument( String arg )
	{
		args.add( arg );
	}
	
	String mkCmdLine()
	{
		StringBuilder b = new StringBuilder();
		
		b.append( commandline );
		
		for( String arg : args )
			b.append( " " + arg );
		
		return b.toString();
	}
	
	public void clearArguments()
	{
		args.clear();
	}

	public void setTimeout( long millis )
	{
		this.upperTime = millis;
	}
	
	public String getOutput() {
		return outGobbler.asString();
	}
}
