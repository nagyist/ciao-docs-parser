package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import uk.nhs.ciao.docs.parser.transformer.MappedProperties.NestedMappedProperties;

public class NestedPropertiesTransformation implements PropertiesTransformation {
	private final PropertyName from;
	private final PropertiesTransformation transformation;
	
	public NestedPropertiesTransformation(final String from, final PropertiesTransformation transformation) {
		this.from = Preconditions.checkNotNull(PropertyName.valueOf(from));
		this.transformation = Preconditions.checkNotNull(transformation);
	}
	
	@Override
	public void apply(final Map<String, Object> source, final MappedProperties destination) {
		final Object originalValue = source.get(from.getName());
		
		if (originalValue instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> nestedProperties = (Map<String, Object>)originalValue;
			transformation.apply(nestedProperties, new NestedMappedProperties(destination, from));
		} else if (originalValue instanceof List) {
			@SuppressWarnings("unchecked")
			final List<Map<String, Object>> list = (List<Map<String, Object>>)originalValue;
			
			for (int index = 0; index < list.size(); index++) {
				final Map<String, Object> nestedProperties = list.get(index);
				transformation.apply(nestedProperties, new NestedMappedProperties(destination, from.getChild(index)));
			}
		}
	}
}
