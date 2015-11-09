package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.collect.Maps;

public class SinglePropertyExtractor implements PropertiesExtractor<NodeStream> {
	private final String propertyName;
	private final WhitespaceMode whitespaceMode;
	
	public SinglePropertyExtractor(final String propertyName) {
		this(propertyName, WhitespaceMode.COLLAPSE_AND_TRIM);
	}
	
	public SinglePropertyExtractor(final String propertyName, final WhitespaceMode whitespaceMode) {
		this.propertyName = propertyName;
		this.whitespaceMode = whitespaceMode;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		if (!nodes.hasNext()) {
			return null;
		}
		
		final Map<String, Object> properties = Maps.newHashMap();
		final String text = whitespaceMode.normalizeWhitespace(nodes.take().getTextContent());
		properties.put(propertyName, text);
		return properties;
	}
}