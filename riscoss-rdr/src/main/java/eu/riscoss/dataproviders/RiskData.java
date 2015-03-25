package eu.riscoss.dataproviders;

import java.util.Date;

public class RiskData
{
	private String id;
	
	private String target;
	
	private Date date;
	
	private RiskDataType type;
	
	private Object value;
	
	
	public RiskData( String id, String target, Date date, RiskDataType type, Object value ) {
        setId( id );
        setTarget( target );
        setDate( date );
        setType (type );
        setValue( value );
	}
	
	
	public String getId()
	{
		return id;
	}
	
	public String getTarget()
	{
		return target;
	}
	
	public Date getDate()
	{
		return date;
	}
	
	public RiskDataType getType()
	{
		return type;
	}
	
	public Object getValue()
	{
		return value;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setTarget(String target)
	{
		this.target = target;
	}
	
	public void setDate(Date date)
	{
		this.date = date;
	}
	
	public void setType(RiskDataType type)
	{
		this.type = type;
	}
	
	public void setValue(Object value)
	{
		this.value = value;
	}
}
