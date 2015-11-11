package uk.nhs.ciao.docs.parser.extractor;

import java.util.Map;
import java.util.Map.Entry;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.xml.NodeStream;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Property extractor which uses a delegate to extract a set of properties, then
 * updates each property name with the specified prefix
 */
public class PrefixedPropertyExtractor implements PropertiesExtractor<NodeStream> {
	private final String prefix;
	private final PropertiesExtractor<NodeStream> delegate;
	private final PrefixMode prefixMode;
	
	public PrefixedPropertyExtractor(final String prefix, final PropertiesExtractor<NodeStream> delegate) {
		this(prefix, delegate, PrefixMode.CAMEL_CASE);
	}
	
	public PrefixedPropertyExtractor(final String prefix, final PropertiesExtractor<NodeStream> delegate, final PrefixMode prefixMode) {
		this.prefix = prefix;
		this.delegate = delegate;
		this.prefixMode = prefixMode;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> delegateProperties = delegate.extractProperties(nodes);
		if (delegateProperties == null || delegateProperties.isEmpty()) {
			return null;
		} else if (Strings.isNullOrEmpty(prefix)) {
			return delegateProperties;
		}
		
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		for (final Entry<String, Object> entry: delegateProperties.entrySet()) {
			final String name = prefixMode.addPrefix(prefix, entry.getKey());
			properties.put(name, entry.getValue());
		}
		return properties;
	}
}
