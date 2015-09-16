package uk.nhs.ciao.docs.parser.transformer;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class PropertyMutator {
	private final String name;
	private final String[] segments;
	
	public PropertyMutator(final String name) {
		this.name = Preconditions.checkNotNull(name);
		this.segments = name.split("\\.");
	}
	
	public void set(final TransformationRecorder recorder, final String from,
			final Map<String, Object> destination, final Object value) {
		set(destination, value);
		recorder.record(from, name);
	}
	
	public void set(final TransformationRecorder recorder, final Collection<String> fromAll,
			final Map<String, Object> destination, final Object value) {
		set(destination, value);
		for (final String from: fromAll) {
			recorder.record(from, name);
		}
	}
	
	private void set(final Map<String, Object> destination, final Object value) {
		Map<String, Object> target = destination;
		for (int index = 0; index < segments.length - 1; index++) {
			final String segment = segments[index];
			@SuppressWarnings("unchecked")
			Map<String, Object> candidate = (Map<String, Object>)destination.get(segment);
			if (candidate == null) {
				candidate = Maps.newLinkedHashMap();
				target.put(segment, candidate);
			}
			
			target = candidate;
		}
		
		setValue(target, segments[segments.length - 1], value);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	protected void setValue(final Map<String, Object> destination, final String name, final Object value) {
		destination.put(name, value);
	}
}
