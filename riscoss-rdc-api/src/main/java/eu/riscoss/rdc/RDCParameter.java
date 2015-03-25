package eu.riscoss.rdc;

public class RDCParameter {
	String name;
	String description;
	String example;
	String defaultValue;
	
	public RDCParameter( String name, String description, String example, String defaultValue ) {
		this.name = name;
		this.description = description;
		this.example = example;
		this.defaultValue = defaultValue;
	}
	
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getExample() {
		return example;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
}
