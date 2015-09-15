package uk.nhs.ciao.docs.parser.transformer;

/**
 * PropertyMutator which appends the new value to the original one (as String) rather that replacing the original value
 */
public class PropertyAppender extends PropertyMutator {
	public PropertyAppender(final PropertyName name) {
		super(name);
	}
	
	public PropertyAppender(final String name) {
		super(name);
	}
	
	@Override
	protected boolean setValue(final MappedProperties destination, final PropertyName name, final Object value) {
		final Object originalValue = destination.get(name);
		final Object newValue = originalValue == null ? value : originalValue.toString() + value.toString();
		return super.setValue(destination, name, newValue);
	}
}
