package uk.nhs.ciao.docs.parser.extractor;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.xml.NodeStream;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Dynamically extracts properties for a stream of nodes.
 * <p>
 * Property are detected using the specified {@link PropertyNameDetector} (the default searches for
 * <code>':'</code> suffix).
 * <p>
 * Values for that property are extracted from the stream until another name is detected. Multiple
 * properties are joined using the specified {@link ValueMode}.
 */
public class PropertyTableExtractor implements PropertiesExtractor<NodeStream> {
	private final ValueMode valueMode;
	private final WhitespaceMode whitespaceMode;
	private final PropertyNameDetector propertyNameDetector;
	
	public PropertyTableExtractor() {
		this(ValueMode.SINGLE_VALUE, WhitespaceMode.COLLAPSE_AND_TRIM);
	}
	
	public PropertyTableExtractor(final ValueMode valueMode) {
		this(valueMode, WhitespaceMode.COLLAPSE_AND_TRIM);
	}
	
	public PropertyTableExtractor(final WhitespaceMode whitespaceMode) {
		this(ValueMode.SINGLE_VALUE, whitespaceMode);
	}
	
	public PropertyTableExtractor(final ValueMode valueMode, final WhitespaceMode whitespaceMode) {
		this(valueMode, whitespaceMode, PropertyNameDetector.ENDS_WITH_COLON);
	}
	
	public PropertyTableExtractor(final ValueMode valueMode, final WhitespaceMode whitespaceMode,
			final PropertyNameDetector propertyNameDetector) {
		this.valueMode = Preconditions.checkNotNull(valueMode);
		this.whitespaceMode = Preconditions.checkNotNull(whitespaceMode);
		this.propertyNameDetector = Preconditions.checkNotNull(propertyNameDetector);
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
				if (propertyNameDetector.isPropertyName(trimmedText)) {
					if (name != null && value != null) {
						properties.put(name, whitespaceMode.normalizeWhitespace(value));
					} else if (name != null && values != null) {
						properties.put(name, values);
					}
					
					name = propertyNameDetector.getPropertyName(trimmedText);
					name = whitespaceMode.normalizeWhitespace(name);
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
						} else if (valueMode == ValueMode.SINGLE_VALUE) {
							// append to existing value
							value = value + " " + originalText;
						} // else NOOP
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