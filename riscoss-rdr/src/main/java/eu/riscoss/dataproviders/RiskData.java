package eu.riscoss.dataproviders;

import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
		//System.out.println("RD:"+id+" \t"+type+" \t"+value);
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
	
	public String toJSON() {
		JsonObject o = new JsonObject();
		o.addProperty( "id", id );
		o.addProperty( "type", type.name() );
		o.addProperty( "value", value.toString() );
		o.addProperty( "target", target );
		o.addProperty( "date", date.getTime() );
		return o.toString();
	}
	
	private static Date parse( String value ) { 
		if( value == null ) return new Date();
		try {
			Long l = Long.parseLong( value );
			return new Date( l.longValue() );
		} catch (Exception e) {
			return new Date();
		} 
	}
	
	public static RiskData fromJSON( String json ) {
		JsonObject o = (JsonObject)new JsonParser().parse( json );
		String rid = o.get( "id" ).getAsString();
		String type = o.get( "type" ).getAsString();
		String value = o.get( "value" ).getAsString();
		String target = o.get( "target" ).getAsString();
		Date date = parse( o.get( "date" ).getAsString() );
		return new RiskData( rid, target, date, RiskDataType.valueOf( type ), value );
	}
	
}
