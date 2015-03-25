package eu.riscoss.rdc;

import java.util.Collection;
import java.util.Map;

import eu.riscoss.dataproviders.RiskData;

public interface RDC {
	
	public String getName();
	
	public Collection<RDCParameter> getParameterList();
	
	public void setParameter( String parName, String parValue );
	
	public Collection<String> getIndicatorNames();
	
	public Map<String,RiskData> getIndicators();
	
}
