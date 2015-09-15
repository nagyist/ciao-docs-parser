package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;

import uk.nhs.ciao.util.SimpleEntry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Combines a series of input properties into a combined output property.
 * <p>
 * The default combined value is an HTML two-column table (similar to a definition list). To use
 * a different output format, a subclass can be created overriding {@link #combineProperties(List)}.
 */
public class CombinePropertiesTransformation implements PropertiesTransformation {
	private final Set<PropertyName> from;
	private final PropertyMutator to;
	private final boolean retainOriginal;
	
	public CombinePropertiesTransformation(final PropertyMutator to, final String... from) {
		this(to, true, from);
	}
	
	public CombinePropertiesTransformation(final PropertyMutator to, final boolean retainOriginal, final String... from) {
		this.from = Sets.newLinkedHashSet();
		for (final String name: from) {
			this.from.add(PropertyName.valueOf(name));
		}
		
		this.to = Preconditions.checkNotNull(to);
		this.retainOriginal = retainOriginal;
	}
	
	@Override
	public void apply(final Map<String, Object> source, final MappedProperties destination) {
		final List<Entry<String, Object>> properties = Lists.newArrayList();
		for (final PropertyName name: from) {
			final Object value = retainOriginal ? source.get(name.getName()) : source.remove(name.getName());
			properties.add(SimpleEntry.valueOf(name.getName(), value));
		}
		
		final String value = combineProperties(properties);
		to.set(destination, from, value);
	}
	
	/**
	 * Combines the specified properties into a single property value
	 */
	protected String combineProperties(final List<Entry<String, Object>> properties) {
		final StringBuilder builder = new StringBuilder();
		
		builder.append("<table width=\"100%\"><tbody>");
		
		for (final Entry<String, Object> property: properties) {
			builder.append("<tr>");
			
			builder.append("<td>");
			builder.append(encodeXml(property.getKey()));
			builder.append("</td>");
			
			final String value = property.getValue() == null ? null : property.getValue().toString();
			if (Strings.isNullOrEmpty(value)) {
				builder.append("<td />");
			} else {
				builder.append("<td>");
				builder.append(encodeXml(value));
				builder.append("</td>");
			}
			
			builder.append("</tr>");
		}
		
		builder.append("</tbody></table>");
		
		return builder.toString();
	}
	
	protected String encodeXml(final String value) {
		return StringEscapeUtils.escapeXml10(value);
	}
}
