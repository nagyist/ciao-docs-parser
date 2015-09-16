package uk.nhs.ciao.docs.parser.transformer;

import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * Renames / copies a property to a new name
 */
public class RenamePropertyTransformation implements PropertiesTransformation {
	private final String from;
	private final PropertyMutator to;
	private final boolean retainOriginal;
	private final boolean cloneNestedProperties;
	
	/**
	 * Creates a new property rename transformation which retains the original property
	 * in the source map
	 */
	public RenamePropertyTransformation(final String from, final PropertyMutator to) {
		this(from, to, true, false);
	}
	
	/**
	 * Creates a new property rename transformation
	 */
	public RenamePropertyTransformation(final String from, final PropertyMutator to,
			final boolean retainOriginal, final boolean cloneNestedProperties) {
		this.from = Preconditions.checkNotNull(from);
		this.to = Preconditions.checkNotNull(to);
		this.retainOriginal = retainOriginal;
		this.cloneNestedProperties = cloneNestedProperties;
	}
	
	@Override
	public void apply(final TransformationRecorder recorder, final Map<String, Object> source,
			final Map<String, Object> destination) {
		if (!source.containsKey(from)) {
			return;
		}
		
		Object value = retainOriginal ? source.get(from) : source.remove(from);
		if (cloneNestedProperties) {
			value = PropertyCloneUtils.deepCloneNestedProperties(value);
		}
		
		to.set(recorder, from, destination, value);
	}
}