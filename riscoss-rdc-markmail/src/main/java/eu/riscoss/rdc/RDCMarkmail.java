package eu.riscoss.rdc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public class RDCMarkmail implements RDC {

	public RDCMarkmail() {
	}

	@Override
	public Map<String, RiskData> getIndicators( String entity ) {
		return new HashMap<String, RiskData>();
	}

	@Override
	public String getName() {
		return "Markmail";
	}

	@Override
	public Collection<RDCParameter> getParameterList() {
		return new ArrayList<RDCParameter>();
	}

	@Override
	public void setParameter( String parName, String parValue ) {
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return new ArrayList<String>();
	}
	
}
