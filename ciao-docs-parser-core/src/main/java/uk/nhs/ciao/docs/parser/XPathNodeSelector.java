package uk.nhs.ciao.docs.parser;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class XPathNodeSelector implements NodeSelector {
	private final XPathExpression expression;
	
	public XPathNodeSelector(final XPath xpath, final String expression) throws XPathExpressionException {
		this(xpath.compile(expression));
	}
	
	public XPathNodeSelector(final XPathExpression expression) {
		this.expression = Preconditions.checkNotNull(expression);
	}
	
	@Override
	public NodeStream selectNodes(final NodeStream nodes) {
		NodeList nodeList = null;
		List<Node> list = null;
		
		while (nodes.hasNext()) {
			final Node node = nodes.take();
			try {
				final NodeList selection = (NodeList)expression.evaluate(node, XPathConstants.NODESET);
				if (selection == null || selection.getLength() == 0) {
					continue;
				} else if (nodeList == null) {
					nodeList = selection;
				} else if (list == null) {
					list = Lists.newArrayList();
					addToList(list, nodeList);
					addToList(list, selection);
				} else {
					addToList(list, selection);
				}
				
			} catch (XPathExpressionException e) {
				throw Throwables.propagate(e);
			}
		}
		
		final NodeStream result;
		if (nodeList == null && list == null) {
			result = NodeStream.createEmptyStream();
		} else if (list == null) {
			result = NodeStream.createStream(nodeList);
		} else {
			result = NodeStream.createStream(list);
		}
		
		return result;
	}
	
	private void addToList(final List<Node> list, final NodeList nodesToAdd) {
		for (int index = 0; index < nodesToAdd.getLength(); index++) {
			list.add(nodesToAdd.item(index));
		}
	}
}
