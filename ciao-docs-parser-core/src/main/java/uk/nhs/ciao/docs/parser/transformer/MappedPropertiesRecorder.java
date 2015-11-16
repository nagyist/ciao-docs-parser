package uk.nhs.ciao.docs.parser.transformer;

import java.util.Set;

import uk.nhs.ciao.docs.parser.PropertyName;

import com.google.common.collect.Sets;

public class MappedPropertiesRecorder implements TransformationRecorder {
	private final Set<PropertyName> mappedProperties = Sets.newLinkedHashSet();
	
	@Override
	public void record(final PropertyName from, final PropertyName to) {
		if (from != null) {
			mappedProperties.add(from);
		}
	}
	
	public Set<PropertyName> getMappedProperties() {
		return mappedProperties;
	}
}
