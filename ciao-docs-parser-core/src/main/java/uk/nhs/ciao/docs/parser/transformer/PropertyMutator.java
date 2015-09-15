package uk.nhs.ciao.docs.parser.transformer;

import java.util.Collection;

import com.google.common.base.Preconditions;

public class PropertyMutator {
	private final PropertyName name;
	
	public PropertyMutator(final PropertyName name) {
		this.name = Preconditions.checkNotNull(name);
	}
	
	public PropertyMutator(final String name) {
		this(PropertyName.valueOf(name));
	}
	
	public void set(final MappedProperties destination, final PropertyName source, final Object value) {
		if (setValue(destination, name, value)) {
			destination.addMappedProperty(source);
		}
	}
	
	public void set(final MappedProperties destination, final Collection<? extends PropertyName> sources, final Object value) {
		if (setValue(destination, name, value)) {
			for (final PropertyName source: sources) {
				destination.addMappedProperty(source);
			}
		}
	}
	
	@Override
	public String toString() {
		return name.toString();
	}
	
	protected boolean setValue(final MappedProperties destination, final PropertyName name, final Object value) {
		return destination.set(name, value);
	}
}
