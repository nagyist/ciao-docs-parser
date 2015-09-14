package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;

import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Transforms an incoming set of properties
 */
public class PropertiesTransformer implements PropertiesExtractor<Map<String, Object>> {
	/**
	 * If true, the transformation is performed directly on the input properties,
	 * otherwise the input properties are cloned before transformation
	 */
	private boolean inPlace = true;
	
	/**
	 * The registered list of transformation to perform.
	 * <p>
	 * The transformations are processed sequentially based on registration order
	 */
	private List<PropertiesTransformation> transformations = Lists.newArrayList();
	
	@Override
	public Map<String, Object> extractProperties(final Map<String, Object> source) throws UnsupportedDocumentTypeException {
		final Map<String, Object> destination = inPlace ? source : PropertyCloneUtils.deepClone(source);
		
		for (final PropertiesTransformation transformation: transformations) {
			transformation.apply(source, destination);
		}
		
		return destination;
	}
	
	public boolean isInPlace() {
		return inPlace;
	}
	
	public void setInPlace(final boolean inPlace) {
		this.inPlace = inPlace;
	}
	
	public List<PropertiesTransformation> getTransformations() {
		return transformations;
	}
	
	public void setTransformations(final List<PropertiesTransformation> transformations) {
		this.transformations = Preconditions.checkNotNull(transformations);
	}
	
	public void addTransformation(final PropertiesTransformation transformation) {
		if (transformation != null) {
			transformations.add(transformation);
		}
	}
	
	public void renameProperty(final String from, final String to) {
		transformations.add(new RenamePropertyTransformation(from, to));
	}
	
	public void splitProperty(final String from, final String pattern, final String... to) {
		transformations.add(new SplitPropertyTransformation(from, pattern, to));
	}
}
