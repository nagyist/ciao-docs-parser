package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Splits a property value into multiple values and assigns each to a specified target property
 */
public class SplitPropertyTransformation implements PropertiesTransformation {
	private final String from;
	private final Pattern pattern;
	private final List<PropertyMutator> to;
	private final boolean retainOriginal;
	
	/**
	 * Creates a new property split transformation which retains the original property
	 * in the source map
	 */
	public SplitPropertyTransformation(final String from, final String pattern, final PropertyMutator... to) {
		this(from, Pattern.compile(pattern), to);
	}
	
	/**
	 * Creates a new property split transformation which retains the original property
	 * in the source map
	 */
	public SplitPropertyTransformation(final String from, final Pattern pattern, final PropertyMutator... to) {
		this(from, true, pattern, to);
	}
	
	/**
	 * Creates a new property split transformation
	 */
	public SplitPropertyTransformation(final String from, final boolean retainOriginal, final Pattern pattern, final PropertyMutator... to) {
		this.from = Preconditions.checkNotNull(from);
		this.retainOriginal = retainOriginal;
		this.pattern = Preconditions.checkNotNull(pattern);
		this.to = Lists.newArrayList(to);
	}
	
	@Override
	public void apply(final Map<String, Object> source, final MappedProperties destination) {
		if (!source.containsKey(from)) {
			return;
		}
		
		Object originalValue = source.get(from);
		if (!(originalValue instanceof CharSequence)) {
			return;
		}
		
		final Matcher matcher = pattern.matcher((CharSequence)originalValue);
		if (!matcher.matches()) {
			return;
		} else if (!retainOriginal) {
			source.remove(from);
		}
		
		for (int index = 0; index < matcher.groupCount(); index++) {
			final String value = matcher.group(index + 1); // one-based
			
			if (index < to.size()) {
				to.get(index).set(destination, PropertyName.valueOf(from), value);
			}
		}
	}
}
