package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

public class NestedPropertiesTransformation implements PropertiesTransformation {
	private final String from;
	private final PropertiesTransformation transformation;
	
	public NestedPropertiesTransformation(final String from, final PropertiesTransformation transformation) {
		this.from = Preconditions.checkNotNull(from);
		this.transformation = Preconditions.checkNotNull(transformation);
	}
	
	@Override
	public void apply(final Map<String, Object> source, final Map<String, Object> destination) {
		final Object originalValue = source.get(from);
		
		if (originalValue instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> nestedProperties = (Map<String, Object>)originalValue;
			transformation.apply(nestedProperties, destination);
		} else if (originalValue instanceof List) {
			@SuppressWarnings("unchecked")
			final List<Map<String, Object>> list = (List<Map<String, Object>>)originalValue;
			for (final Map<String, Object> nestedProperties: list) {
				transformation.apply(nestedProperties, destination);
			}
		}
	}
}
