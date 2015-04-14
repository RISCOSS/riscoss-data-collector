package eu.riscoss.rdc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public class RDCSonar implements RDC {

	private String name;

	public RDCSonar( String name ) {
		this.name = name;
	}

	@Override
	public Map<String, RiskData> getIndicators() {
		return new HashMap<String, RiskData>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Collection<RDCParameter> getParameterList() {
		return new ArrayList<RDCParameter>();
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
