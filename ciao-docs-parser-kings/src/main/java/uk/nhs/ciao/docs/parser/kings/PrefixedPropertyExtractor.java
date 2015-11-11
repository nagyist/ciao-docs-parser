package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;
import java.util.Map.Entry;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.base.MoreObjects;
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
	
	public enum PrefixMode {
		/**
		 * The prefix and value are concatenated as-is
		 */
		CONCATENATE {
			public String addPrefix(final String prefix, final String value) {
				if (prefix == null && value == null) {
					return null;
				} else if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(prefix)) {
					return MoreObjects.firstNonNull(prefix, value);
				}
				return prefix + value;
			}
		},
		
		/**
		 * The first letter of the value is converted to upper case
		 */
		CAMEL_CASE {
			public String addPrefix(final String prefix, final String value) {
				String camelCaseValue;
				if (Strings.isNullOrEmpty(value)) {
					camelCaseValue = value;
				} else if (value.length() == 1) {
					camelCaseValue = Character.toString(Character.toUpperCase(value.charAt(0)));
				} else {
					camelCaseValue = Character.toString(Character.toUpperCase(value.charAt(0)))
							+ value.substring(1);
				}
				
				return CONCATENATE.addPrefix(prefix, camelCaseValue);
			}
		};
		
		public abstract String addPrefix(final String prefix, final String value);
	}
	
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
