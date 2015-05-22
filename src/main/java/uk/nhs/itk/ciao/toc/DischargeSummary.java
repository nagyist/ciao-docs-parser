package uk.nhs.itk.ciao.toc;

import java.util.Map;

public class DischargeSummary {
	private final String location;
	private final Map<String, Object> properties;
	
	public DischargeSummary(final String location, final Map<String, Object> properties) {
		this.location = location;
		this.properties = properties;
	}
	
	public String getLocation() {
		return location;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
}
