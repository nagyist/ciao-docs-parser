package uk.nhs.ciao.docs.parser.transformer;

import java.util.Map;

import uk.nhs.ciao.docs.parser.PropertyName;

import com.google.common.base.Preconditions;

/**
 * Renames / copies a property to a new name
 */
public class RenamePropertyTransformation implements PropertiesTransformation {
	private final PropertyName from;
	private final PropertyMutator to;
	private final boolean retainOriginal;
	private final boolean cloneNestedProperties;
	
	/**
	 * Creates a new property rename transformation which retains the original property
	 * in the source map
	 */
	public RenamePropertyTransformation(final PropertyName from, final PropertyMutator to) {
		this(from, to, true, false);
	}
	
	/**
	 * Creates a new property rename transformation
	 */
	public RenamePropertyTransformation(final PropertyName from, final PropertyMutator to,
			final boolean retainOriginal, final boolean cloneNestedProperties) {
		this.from = Preconditions.checkNotNull(from);
		this.to = Preconditions.checkNotNull(to);
		this.retainOriginal = retainOriginal;
		this.cloneNestedProperties = cloneNestedProperties;
	}
	
	@Override
	public void apply(final TransformationRecorder recorder, final Map<String, Object> source,
			final Map<String, Object> destination) {
		Object value = from.get(source);
		if (value == null) {
			return;
		} else if (!retainOriginal) {
			from.remove(source);
		}
		
		if (cloneNestedProperties) {
			value = PropertyCloneUtils.deepCloneNestedProperties(value);
		}
		
		to.set(recorder, from, destination, value);
	}
}