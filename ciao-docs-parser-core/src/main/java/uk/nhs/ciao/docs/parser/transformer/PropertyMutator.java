package uk.nhs.ciao.docs.parser.transformer;

import java.util.Collection;
import java.util.Map;

import uk.nhs.ciao.docs.parser.PropertyName;

import com.google.common.base.Preconditions;

public class PropertyMutator {
	private final PropertyName name;
	
	public PropertyMutator(final String name) {
		this(PropertyName.valueOf(name));
	}
	
	public PropertyMutator(final PropertyName name) {
		this.name = Preconditions.checkNotNull(name);
	}
	
	public void set(final TransformationRecorder recorder, final PropertyName from,
			final Map<String, Object> destination, final Object value) {
		setValue(destination, name, value);
		recorder.record(from, name);
	}
	
	public void set(final TransformationRecorder recorder, final Collection<PropertyName> fromAll,
			final Map<String, Object> destination, final Object value) {
		setValue(destination, name, value);
		for (final PropertyName from: fromAll) {
			recorder.record(from, name);
		}
	}
	
	@Override
	public String toString() {
		return name.toString();
	}
	
	protected void setValue(final Map<String, Object> destination, final PropertyName name, final Object value) {
		name.set(destination, value);
	}
}
