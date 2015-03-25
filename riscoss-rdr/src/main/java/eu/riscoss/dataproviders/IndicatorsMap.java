package eu.riscoss.dataproviders;

/**
 * @author Mirko Morandini
 */

import java.util.Date;


public class IndicatorsMap extends java.util.HashMap<String, RiskData> {
	
//	private static IndicatorsMap indicatorsMap=null;
	
	/*
	 * the entity (OSS component) on which the measurement was made
	 */
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
	public RiskData add(String indicatorName, RiskDataType type, Object value) {
		RiskData rd = new RiskData(indicatorName, targetEntity, new Date(), type, value);
		return	put(indicatorName, rd);
	}
	
	
	public RiskData add(String indicatorName, double value) {
		RiskData rd = new RiskData(indicatorName, targetEntity, new Date(), RiskDataType.NUMBER, value);
		return	put(indicatorName, rd);
	}
	
	@Override
	public String toString(){
		return values().toString();
	}
	
//	@Deprecated
//	public RiskData add(String indicatorName, String value) {
//		//TODO: change "Evidence". To what for a String or a Date?  Solution TODO: parse the date and store it as number of seconds.
//		RiskData rd = RiskDataFactory.createRiskData(indicatorName, targetEntity, new Date(), RiskDataType.DATE, value);
//		return	put(indicatorName, rd);
//	}

//	public static Object add(String key, String value) {
//		RiskData rd = RiskDataFactory.createRiskData(indicatorName, targetEntity, new Date(), RiskDataType.NUMBER, value);
//		return	put(indicatorName, rd);
//	}
//
//	public IndicatorsMap() {
//		// TODO Auto-generated constructor stub
//	}
}


