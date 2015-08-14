package uk.nhs.ciao.docs.parser.rule;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class NodeStream {
	public boolean isEmpty() {
		return !hasNext();
	}
	
	public boolean hasNext() {
		return remaining() > 0;
	}
	
	public abstract int remaining();
	
	public Node peek() {
		return peek(0);
	}
	
	public abstract Node peek(final int index);
	
	public abstract Node take();
	
	public boolean skip() {
		return take() != null;
	}
	
	public int skip(final int amount) {
		int skipped = 0;
		
		for (int count = 0; count < amount; count++) {
			if (skip()) {
				skipped++;
			} else {
				break;
			}
		}
		
		return skipped;
	}
	
	public abstract Mark mark();
	
	public interface Mark {
		NodeStream resetStream();
	}
	
	public static NodeStream createEmptyStream() {
		return createStream((Node)null);
	}
	
	public static NodeStream createStream(final Node node) {
		return new SingleNodeNodeStream(node);
	}
	
	public static NodeStream createStream(final NodeList nodeList) {
		return new NodeListNodeStream(nodeList);
	}
	
	public static NodeStream createStream(final NodeList nodeList, final int index, final int length) {
		return new NodeListNodeStream(nodeList, index, length);
	}
	
	public static NodeStream createStream(final List<? extends Node> list) {
		return new ListNodeStream(list);
	}
	
	public static NodeStream createStream(final List<? extends Node> list, final int index, final int length) {
		return new ListNodeStream(list, index, length);
	}
	
	private static class SingleNodeNodeStream extends NodeStream {
		private final Node node;
		private boolean consumed;
		
		public SingleNodeNodeStream(final Node node) {
			this.node = node;
			this.consumed = node == null;
		}
		
		@Override
		public Node take() {
			final Node result = consumed ? null : node;
			consumed = true;
			return result;
		}
		
		@Override
		public int remaining() {
			return consumed ? 0 : 1;
		}
		
		@Override
		public Node peek(final int index) {
			return index >= 0 && index < remaining() ? node : null;
		}
		
		@Override
		public Mark mark() {
			return new SingleNodeMark();
		}
		
		private class SingleNodeMark implements Mark {
			private final boolean consumed = SingleNodeNodeStream.this.consumed;

			@Override
			public NodeStream resetStream() {
				SingleNodeNodeStream.this.consumed = consumed;
				return SingleNodeNodeStream.this;
			}
		}
	}
	
	private static class NodeListNodeStream extends NodeStream {
		private final NodeList nodeList;
		private final int length;
		private int index;
		
		public NodeListNodeStream(final NodeList nodeList) {
			this(nodeList, 0, nodeList.getLength());
		}

		public NodeListNodeStream(final NodeList nodeList, final int index, final int length) {
			this.nodeList = nodeList;
			this.index = index;
			this.length = length;
		}
		
		@Override
		public Node take() {
			final Node result = peek();
			if (result != null) {
				index++;
			}
			
			return result;
		}
		
		@Override
		public int remaining() {
			return length - index;
		}
		
		@Override
		public Node peek(final int index) {
			if (index < 0 || index >= remaining()) {
				return null;
			}
			
			return nodeList.item(this.index + index);
		}
		
		@Override
		public Mark mark() {
			return new NodeListMark();
		}
		
		private class NodeListMark implements Mark {
			private final int index = NodeListNodeStream.this.index;

			@Override
			public NodeStream resetStream() {
				NodeListNodeStream.this.index = index;
				return NodeListNodeStream.this;
			}
		}
	}
	
	private static class ListNodeStream extends NodeStream {
		private final List<? extends Node> list;
		private final int length;
		private int index;
		
		public ListNodeStream(final List<? extends Node> list) {
			this(list, 0, list.size());
		}

		public ListNodeStream(final List<? extends Node> list, final int index, final int length) {
			this.list = list;
			this.index = index;
			this.length = length;
		}
		
		@Override
		public Node take() {
			final Node result = peek();
			if (result != null) {
				index++;
			}
			
			return result;
		}
		
		@Override
		public int remaining() {
			return length - index;
		}
		
		@Override
		public Node peek(final int index) {
			if (index < 0 || index >= remaining()) {
				return null;
			}
			
			return list.get(this.index + index);
		}
		
		@Override
		public Mark mark() {
			return new ListMark();
		}
		
		private class ListMark implements Mark {
			private final int index = ListMark.this.index;

			@Override
			public NodeStream resetStream() {
				ListNodeStream.this.index = index;
				return ListNodeStream.this;
			}
		}
	}
}
