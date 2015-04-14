package eu.riscoss.datacollector;

/**
 * @author Mirko Morandini
 */

import java.io.IOException;
import java.util.Properties;

import eu.riscoss.datacollector.common.IndicatorsMap;

public interface AbstractDataProvider {
	
	
//	abstract HashMap<String, Integer> analyseReport(String target, String configFile) throws IOException;
	
	abstract void createIndicators(IndicatorsMap im, Properties properties) throws IOException, Exception;

}