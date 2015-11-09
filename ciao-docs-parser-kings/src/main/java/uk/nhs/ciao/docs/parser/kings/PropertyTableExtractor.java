package uk.nhs.ciao.docs.parser.kings;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PropertyTableExtractor implements PropertiesExtractor<NodeStream> {
	public enum ValueMode {
		/**
		 * Multiple values are stored in a list
		 */
		MULTIPLE_VALUES,
		
		
		/**
		 * Multiple values are appended as text as a single property value
		 */
		SINGLE_VALUE;
	}
	
	private final ValueMode valueMode;
	private final WhitespaceMode whitespaceMode;
	
	public PropertyTableExtractor() {
		this.valueMode = ValueMode.SINGLE_VALUE;
		this.whitespaceMode = WhitespaceMode.COLLAPSE_AND_TRIM;
	}
	
	public PropertyTableExtractor(final ValueMode valueMode) {
		this.valueMode = valueMode;
		this.whitespaceMode = WhitespaceMode.COLLAPSE_AND_TRIM;
	}
	
	public PropertyTableExtractor(final WhitespaceMode whitespaceMode) {
		this.valueMode = ValueMode.SINGLE_VALUE;
		this.whitespaceMode = whitespaceMode;
	}
	
	public PropertyTableExtractor(final ValueMode valueMode, final WhitespaceMode whitespaceMode) {
		this.valueMode = valueMode;
		this.whitespaceMode = whitespaceMode;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		String name = null;
		String value = null;
		List<String> values = null;
		
		while (nodes.hasNext()) {
			final Node node = nodes.take();
			if (node instanceof Element) {
				final String originalText = Strings.nullToEmpty(node.getTextContent());
				final String trimmedText = originalText.trim();
				if (trimmedText.length() > 1 && trimmedText.endsWith(":")) {
					if (name != null && value != null) {
						properties.put(name, whitespaceMode.normalizeWhitespace(value));
					} else if (name != null && values != null) {
						properties.put(name, values);
					}
					
					name = trimmedText.substring(0, trimmedText.length() - 1);
					value = null;
					values = null;
				} else if (!trimmedText.isEmpty()) {
					if (value == null && values == null) {
						value = originalText;
					} else if (values == null) {
						if (valueMode == ValueMode.MULTIPLE_VALUES || properties.containsKey(name)) {
							values = Lists.newArrayList(whitespaceMode.normalizeWhitespace(value),
									whitespaceMode.normalizeWhitespace(originalText));
							value = null;
						} else {
							// append to existing value
							value = value + " " + originalText;
						}
					} else {
						values.add(whitespaceMode.normalizeWhitespace(originalText));
					}
				}
			}
		}
		
		if (name != null && value != null) {
			properties.put(name, whitespaceMode.normalizeWhitespace(value));
		} else if (name != null && values != null) {
			properties.put(name, values);
		}
		
		return properties;
	}
}