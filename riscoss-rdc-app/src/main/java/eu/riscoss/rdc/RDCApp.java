package eu.riscoss.rdc;

import java.util.Set;

import org.reflections.Reflections;


public class RDCApp {
	public static void main( String[] args ) throws Exception {
		
		Reflections reflections = new Reflections( RDCRunner.class.getPackage().getName() );
		
		Set<Class<? extends RDC>> subTypes = reflections.getSubTypesOf(RDC.class);
		
		for( Class<? extends RDC> cls : subTypes ) {
			try {
				RDC rdc = (RDC)cls.newInstance();
				RDCFactory.get().registerRDC( rdc );
			}
			catch( Exception ex ) {}
		}
		
		new RDCRunner().run( args );
	}
}
