package eu.riscoss.rdc;

/**
 * @author Mirko Morandini
 */

import java.util.Date;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class IndicatorsMap extends java.util.HashMap<String, RiskData> {
	
	private static final long serialVersionUID = 1346076777763235180L;
	
	private final String targetEntity;
	
	
	public IndicatorsMap(String targetEntity) {
		super();
		this.targetEntity = targetEntity;
	}

	public String getTargetEntity() {
		return targetEntity;
	}
	
	/**
	 * Saves a RiskData with the specified parameters, the actual date and the stored Target.
	 * 
	 * @param indicatorName
	 * @param targetEntity
	 * @param type
	 * @param value
	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
	 */
	public void add( String indicatorName, RiskDataType type, Object value ) {
		if( value == null ) return;
		RiskData rd = new RiskData( indicatorName, targetEntity, new Date(), type, value );
		put( indicatorName, rd );
	}
	
	public void add(String indicatorName, double value) {
		RiskData rd = new RiskData( indicatorName, targetEntity, new Date(), RiskDataType.NUMBER, value );
		put(indicatorName, rd );
	}
	
	@Override
	public String toString(){
		return values().toString();
	}
}


