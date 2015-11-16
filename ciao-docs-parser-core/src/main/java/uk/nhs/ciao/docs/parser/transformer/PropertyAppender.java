package uk.nhs.ciao.docs.parser.transformer;

import java.util.Map;

import uk.nhs.ciao.docs.parser.PropertyName;

/**
 * PropertyMutator which appends the new value to the original one (as String) rather that replacing the original value
 */
public class PropertyAppender extends PropertyMutator {
	public PropertyAppender(final String name) {
		super(name);
	}
	
	public PropertyAppender(final PropertyName name) {
		super(name);
	}
	
	@Override
	protected void setValue(final Map<String, Object> destination, final PropertyName name, final Object value) {
		final Object originalValue = name.get(destination);
		final Object newValue = originalValue == null ? value : originalValue.toString() + value.toString();
		super.setValue(destination, name, newValue);
	}
}
