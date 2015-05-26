package uk.nhs.ciao.docs.parser;

import java.util.Map;

public class ParsedDocument {
	private final String originalDocumentLocation;
	private final Map<String, Object> properties;
	
	public ParsedDocument(final String originalDocumentLocation, final Map<String, Object> properties) {
		this.originalDocumentLocation = originalDocumentLocation;
		this.properties = properties;
	}
	
	public String getOriginalDocumentLocation() {
		return originalDocumentLocation;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
}
