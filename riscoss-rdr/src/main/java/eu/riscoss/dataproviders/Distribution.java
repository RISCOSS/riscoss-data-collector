package eu.riscoss.dataproviders;

import java.util.ArrayList;
import java.util.List;

public class Distribution
{
    private List<Double> values;

    public Distribution(Double... values)
    {
        this.values = new ArrayList<Double>();
        for (Double d : values) {
            this.values.add(d);
        }
    }
    
    public Distribution(List<Double> values)
    {
    	this.values = values;
    }

    public List<Double> getValues()
    {
        return values;
    }

    public void setValues(List<Double> values)
    {
        this.values = values;
    }
    
    @Override
    public String toString() {
//    	String ret = "(";
//    	for (Double d : values) {
//    		ret=(ret.equals("(")?ret.concat(""+d):ret.concat(", "+d));
//		}
//    	return ret+")";
    	
    	//attention: ToString is used Client-side for writing data into the RDR! Now it needs EXACTLY this format.
    	String str = "";
		String sep = "";
		for( double val : values ) {
			str += sep + val;
			sep = ";";
		}
		return str;
    }
    
	/**
	 * Computes the average.
	 * @param l
	 * @return 0 if the list is empty!
	 */
	public Double getAverage(){
		Double sumc = 0.0;
		for (Double num : values)
			sumc += num;
		return  sumc / values.size();
	}
}
