package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.collect.Maps;

/**
 * PropertyExtractor which uses a regex to split text into key + value pairs
 * <p>
 * The value is only added if the specified pattern matches the input
 */
public class KeyValuePropertyExtractor implements PropertiesExtractor<NodeStream> {
	private final Pattern splitPattern;
	private final WhitespaceMode whitespaceMode;
	
	public KeyValuePropertyExtractor(final String splitPattern) {
		this(splitPattern, WhitespaceMode.COLLAPSE_AND_TRIM);
	}
	
	public KeyValuePropertyExtractor(final String splitPattern, final WhitespaceMode whitespaceMode) {
		this.splitPattern = Pattern.compile(splitPattern);
		this.whitespaceMode = whitespaceMode;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		if (!nodes.hasNext()) {
			return null;
		}
		
		final Map<String, Object> properties = Maps.newHashMap();
		
		while (nodes.hasNext()) {
			final String text = nodes.take().getTextContent();
			final Matcher matcher = splitPattern.matcher(text);
			
			if (matcher.find()) {
				final String propertyName = whitespaceMode.normalizeWhitespace(text.substring(0, matcher.start()));
				final String value;
				if (matcher.end() < text.length()) {
					value = whitespaceMode.normalizeWhitespace(text.substring(matcher.end()));
				} else {
					value = "";
				}
				
				if (!propertyName.isEmpty()) {
					properties.put(propertyName, value);
				}
			}
		}
		
		return properties;
	}
}
