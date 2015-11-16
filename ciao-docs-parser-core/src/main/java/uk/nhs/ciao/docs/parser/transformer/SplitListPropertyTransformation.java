package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.nhs.ciao.docs.parser.PropertyName;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Splits a property value into a list of values (using the specified patttern)
 * and assigns the result to the target property
 */
public class SplitListPropertyTransformation implements PropertiesTransformation {
	private final PropertyName from;
	private final Pattern pattern;
	private final PropertyMutator to;
	private final boolean retainOriginal;
	
	/**
	 * Creates a new property split transformation which retains the original property
	 * in the source map
	 */
	public SplitListPropertyTransformation(final PropertyName from, final String pattern, final PropertyMutator to) {
		this(from, Pattern.compile(pattern), to);
	}
	
	/**
	 * Creates a new property split transformation which retains the original property
	 * in the source map
	 */
	public SplitListPropertyTransformation(final PropertyName from, final Pattern pattern, final PropertyMutator to) {
		this(from, true, pattern, to);
	}
	
	/**
	 * Creates a new property split transformation
	 */
	public SplitListPropertyTransformation(final PropertyName from, final boolean retainOriginal, final Pattern pattern, final PropertyMutator to) {
		this.from = Preconditions.checkNotNull(from);
		this.retainOriginal = retainOriginal;
		this.pattern = Preconditions.checkNotNull(pattern);
		this.to = Preconditions.checkNotNull(to);
	}
	
	@Override
	public void apply(final TransformationRecorder recorder, final Map<String, Object> source,
			final Map<String, Object> destination) {
		Object originalValue = from.get(source);
		if (!(originalValue instanceof CharSequence)) {
			return;
		}
		
		final String stringValue = originalValue.toString();
		final Matcher matcher = pattern.matcher(stringValue);
		final List<String> values = Lists.newArrayList();

		int previousIndex = 0;
		while (matcher.find()) {
			if (previousIndex < matcher.start()) {
				values.add(stringValue.substring(previousIndex, matcher.start()));
			}
			previousIndex = matcher.end();
		}

		if (previousIndex < stringValue.length()) {
			values.add(stringValue.substring(previousIndex));
		}
		
		if (values.isEmpty()) {
			return;
		} else if (!retainOriginal) {
			from.remove(source);
		}
		
		to.set(recorder, from, destination, values);
	}
}
