package data;

public enum RmxConstant {
	ENV_PROPERTY_NAME("env");
	
	
	private String value;
	
	private RmxConstant(String value) {
		this.value = value;
	}
	

	public String getValue() {
		return value;
	}
}
