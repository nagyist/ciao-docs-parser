package uk.nhs.ciao.docs.parser.transformer;

import java.util.Set;

import joptsimple.internal.Strings;

import com.google.common.collect.Sets;

public class MappedPropertiesRecorder implements TransformationRecorder {
	private final Set<String> mappedProperties = Sets.newLinkedHashSet();
	
	@Override
	public void record(final String from, final String to) {
		if (!Strings.isNullOrEmpty(from)) {
			mappedProperties.add(from);
		}
	}
	
	public Set<String> getMappedProperties() {
		return mappedProperties;
	}
}
