package uk.nhs.ciao.docs.parser;

import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class ParsedDocument {
	private final OriginalDocument originalDocument;
	private final Map<String, Object> properties;
	
	public ParsedDocument(final OriginalDocument originalDocument, final Map<String, Object> properties) {
		this.originalDocument = Preconditions.checkNotNull(originalDocument);
		this.properties = Preconditions.checkNotNull(properties);
	}
	
	public OriginalDocument getOriginalDocument() {
		return originalDocument;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("originalDocument", originalDocument)
				.add("properties", properties)
				.toString();
	}
}
