package uk.nhs.ciao.docs.parser.extractor;

import java.util.List;
import java.util.Map;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.xml.NodeStream;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SinglePropertyExtractor implements PropertiesExtractor<NodeStream> {
	private final String propertyName;
	private final ValueMode valueMode;
	private final WhitespaceMode whitespaceMode;
	
	public SinglePropertyExtractor(final String propertyName) {
		this(propertyName, ValueMode.INITIAL_VALUE, WhitespaceMode.COLLAPSE_AND_TRIM);
	}
	
	public SinglePropertyExtractor(final String propertyName, final ValueMode valueMode) {
		this(propertyName, valueMode, WhitespaceMode.COLLAPSE_AND_TRIM);
	}
	
	public SinglePropertyExtractor(final String propertyName, final WhitespaceMode whitespaceMode) {
		this(propertyName, ValueMode.INITIAL_VALUE, whitespaceMode);
	}
	
	public SinglePropertyExtractor(final String propertyName, final ValueMode valueMode,
			final WhitespaceMode whitespaceMode) {
		this.propertyName = propertyName;
		this.valueMode = valueMode;
		this.whitespaceMode = whitespaceMode;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		if (!nodes.hasNext()) {
			return null;
		}
		
		String value = null;
		List<String> values = null;
		while (nodes.hasNext()) {
			final String text = nodes.take().getTextContent();
			if (!Strings.isNullOrEmpty(text) && !text.trim().isEmpty()) {
				if (Strings.isNullOrEmpty(value)) {
					value = text;
					
					if (valueMode == ValueMode.INITIAL_VALUE) {
						break;
					}
				} else if (valueMode == ValueMode.MULTIPLE_VALUES) {
					if (values == null) {
						values = Lists.newArrayList(whitespaceMode.normalizeWhitespace(value));
					}
					
					values.add(whitespaceMode.normalizeWhitespace(text));
				} else if (valueMode == ValueMode.SINGLE_VALUE) {
					value += " " + text;
				} // else INITIAL_VALUE => NOOP
			}
		}
		
		final Map<String, Object> properties = Maps.newHashMap();

		if (values == null) {
			value = whitespaceMode.normalizeWhitespace(value);
			properties.put(propertyName, value);
		} else {
			properties.put(propertyName, values);
		}
		
		return properties;
	}
}