package uk.nhs.ciao.docs.parser.kings;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

public class NestedObjectPropertyExtractor implements PropertiesExtractor<NodeStream> {
	private final String propertyName;
	private final PropertiesExtractor<NodeStream> delegate;
	private final DelegationMode delegationMode;
	
	public enum DelegationMode {
		ONCE_PER_NODE,
		ONCE_PER_STREAM;
	}
	
	public NestedObjectPropertyExtractor(final String propertyName, final PropertiesExtractor<NodeStream> delegate) {
		this(propertyName, delegate, DelegationMode.ONCE_PER_STREAM);
	}
	
	public NestedObjectPropertyExtractor(final String propertyName, final PropertiesExtractor<NodeStream> delegate, final DelegationMode delegationMode) {
		this.propertyName = propertyName;
		this.delegate = delegate;
		this.delegationMode = delegationMode;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		Map<String, Object> properties = null;
		
		switch(delegationMode) {
		case ONCE_PER_STREAM:
			properties = extractPropertiesFromEntireStream(nodes);
			break;
		case ONCE_PER_NODE:
			properties = extractPropertiesFromEachNode(nodes);
			break;
		}
		
		return properties;
	}
	
	private Map<String, Object> extractPropertiesFromEntireStream(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> value = delegate.extractProperties(nodes);
		if (value == null || value.isEmpty()) {
			return null;
		}
		
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		properties.put(propertyName, value);
		return properties;
	}
	
	private Map<String, Object> extractPropertiesFromEachNode(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		Map<String, Object> value = null;
		List<Map<String, Object>> values = null;
		while (nodes.hasNext()) {
			final Node node = nodes.take();
			final Map<String, Object> candidate = delegate.extractProperties(NodeStream.createStream(node));
			if (candidate == null || candidate.isEmpty()) {
				continue;
			} else if (value == null) {
				value = candidate;
			} else {
				if (values == null) {
					values = Lists.newArrayList();
					values.add(value);
				}
				values.add(candidate);
			}
		}
		
		
		if (values == null && (value == null || value.isEmpty())) {
			return null;
		}
		
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		if (values != null) {
			properties.put(propertyName, values);
		} else {
			properties.put(propertyName, value);
		}
		
		return properties;
	}
}
