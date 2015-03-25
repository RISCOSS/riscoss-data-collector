package eu.riscoss.rdc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public class RDCMarkmail implements RDC {

	private String name;

	public RDCMarkmail( String name ) {
		this.name = name;
	}

	@Override
	public Map<String, RiskData> getIndicators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Collection<RDCParameter> getParameterList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameter( String parName, String parValue ) {
//		defaultProperties.put( parName, parValue );
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return new ArrayList<String>();
	}
	
}
