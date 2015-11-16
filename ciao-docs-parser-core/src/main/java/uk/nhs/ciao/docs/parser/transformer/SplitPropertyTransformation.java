package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.nhs.ciao.docs.parser.PropertyName;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Splits a property value into multiple values and assigns each to a specified target property
 */
public class SplitPropertyTransformation implements PropertiesTransformation {
	private final PropertyName from;
	private final Pattern pattern;
	private final List<PropertyMutator> to;
	private final boolean retainOriginal;
	
	/**
	 * Creates a new property split transformation which retains the original property
	 * in the source map
	 */
	public SplitPropertyTransformation(final PropertyName from, final String pattern, final PropertyMutator... to) {
		this(from, Pattern.compile(pattern), to);
	}
	
	/**
	 * Creates a new property split transformation which retains the original property
	 * in the source map
	 */
	public SplitPropertyTransformation(final PropertyName from, final Pattern pattern, final PropertyMutator... to) {
		this(from, true, pattern, to);
	}
	
	/**
	 * Creates a new property split transformation
	 */
	public SplitPropertyTransformation(final PropertyName from, final boolean retainOriginal, final Pattern pattern, final PropertyMutator... to) {
		this.from = Preconditions.checkNotNull(from);
		this.retainOriginal = retainOriginal;
		this.pattern = Preconditions.checkNotNull(pattern);
		this.to = Lists.newArrayList(to);
	}
	
	@Override
	public void apply(final TransformationRecorder recorder, final Map<String, Object> source,
			final Map<String, Object> destination) {
		Object originalValue = from.get(source);
		if (!(originalValue instanceof CharSequence)) {
			return;
		}
		
		final Matcher matcher = pattern.matcher((CharSequence)originalValue);
		if (!matcher.matches()) {
			return;
		} else if (!retainOriginal) {
			from.remove(source);
		}
		
		for (int index = 0; index < matcher.groupCount(); index++) {
			final String value = matcher.group(index + 1); // one-based
			
			if (index < to.size()) {
				to.get(index).set(recorder, from, destination, value);
			}
		}
	}
}
