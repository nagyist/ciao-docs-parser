package uk.nhs.ciao.docs.parser.rule;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.rule.NodeStream.Mark;
import uk.nhs.ciao.util.TreeMerge;

public class SplitterPropertiesExtractor implements PropertiesExtractor<NodeStream> {
	private final List<SelectionHandler> selectionHandlers = Lists.newArrayList();
	private final TreeMerge treeMerge = new TreeMerge();
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		
		for (final SelectionHandler selectionHandler: selectionHandlers) {
			final Map<String, Object> extractedProperties = selectionHandler.selectAndExtract(nodes);
			combineProperties(properties, extractedProperties);
		}
		
		return properties;
	}
	
	public void addSelection(final NodeSelector selector, final PropertiesExtractor<NodeStream> extractor) {
		addSelection(selector, Mode.RESET_NODES, extractor);
	}
	
	public void addSelection(final NodeSelector selector, final Mode mode, final PropertiesExtractor<NodeStream> extractor) {
		selectionHandlers.add(new SelectionHandler(selector, mode, extractor));
	}
	
	private void combineProperties(final Map<String, Object> properties,
			final Map<String, Object> extractedProperties) {
		if (extractedProperties != null) {
			treeMerge.mergeInto(extractedProperties, properties);
		}
	}

	private class SelectionHandler {
		private final NodeSelector selector;
		private final Mode mode;
		private final PropertiesExtractor<NodeStream> extractor;
		
		public SelectionHandler(final NodeSelector selector, final Mode mode, final PropertiesExtractor<NodeStream> extractor) {
			this.selector = Preconditions.checkNotNull(selector);
			this.mode = Preconditions.checkNotNull(mode);
			this.extractor = Preconditions.checkNotNull(extractor);
		}
		
		public Map<String, Object> selectAndExtract(final NodeStream nodes) throws UnsupportedDocumentTypeException {
			final Mark initialMark = nodes.mark();
			final NodeStream selectedNodes = selector.selectNodes(nodes);
			if (selectedNodes == null || selectedNodes.isEmpty()) {
				initialMark.resetStream();
				return null;
			}
			
			final Map<String, Object> extractedProperties =
					extractor.extractProperties(selectedNodes);
			if (mode == Mode.RESET_NODES) {
				initialMark.resetStream();
			}
			
			return extractedProperties;
		}
	}
	
	public enum Mode {
		CONSUME_NODES,		
		RESET_NODES;
	}
}
