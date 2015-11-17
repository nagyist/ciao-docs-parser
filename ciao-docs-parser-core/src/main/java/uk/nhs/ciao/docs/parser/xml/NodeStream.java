package uk.nhs.ciao.docs.parser.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a stream of XML {@link Node}s.
 * <p>
 * Multiple {@link Mark}s can be created to return to previous points in the stream.
 */
public abstract class NodeStream {
	/**
	 * Tests if the stream is empty
	 */
	public boolean isEmpty() {
		return !hasNext();
	}
	
	/**
	 * Tests if the stream contains more nodes
	 */
	public boolean hasNext() {
		return remaining() > 0;
	}
	
	/**
	 * The number of nodes remaining in the stream
	 */
	public abstract int remaining();
	
	/**
	 * Retrieves the next node in the stream (but does not increment the current position)
	 */
	public Node peek() {
		return peek(0);
	}
	
	/**
	 * Retrieves a node further in the stream (but does not increment the current position)
	 */
	public abstract Node peek(final int index);
	
	/**
	 * Retrieves the next node in the stream and increments the stream position
	 */
	public abstract Node take();
	
	/**
	 * Skips the next node in the stream by incrementing the current position.
	 * 
	 * @return true if the node could be skipped, or false otherwise
	 */
	public boolean skip() {
		return take() != null;
	}
	
	/**
	 * Skips the specified number of nodes in the stream by incrementing the current position.
	 * 
	 * @return true if the nodes could be skipped, or false otherwise
	 */
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
	
	/**
	 * Marks the current position in the stream - the mark can be used to
	 * return the stream to this position.
	 */
	public abstract Mark mark();
	
	/**
	 * Represents a marked location within a node stream
	 */
	public interface Mark {
		/**
		 * Resets the stream to the previously marked position
		 * 
		 * @return The stream that was marked
		 */
		NodeStream resetStream();
	}
	
	/**
	 * Creates a new empty stream
	 */
	public static NodeStream createEmptyStream() {
		return createStream((Node)null);
	}
	
	/**
	 * Creates a new stream backed by the single specified node
	 */
	public static NodeStream createStream(final Node node) {
		return new SingleNodeNodeStream(node);
	}
	
	/**
	 * Creates a new stream backed by the specified NodeList
	 */
	public static NodeStream createStream(final NodeList nodeList) {
		return createStream(nodeList, 0, nodeList.getLength());
	}
	
	/**
	 * Creates a new stream backed by the specified NodeList and index range
	 */
	public static NodeStream createStream(final NodeList nodeList, final int index, final int length) {
		return new NodeListNodeStream(nodeList, index, length);
	}
	
	/**
	 * Node stream backed by a single Node
	 */
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
			return new SingleNodeMark(this);
		}
	}
	
	/**
	 * Marks a position within a {@link SingleNodeNodeStream}
	 */
	private static class SingleNodeMark implements Mark {
		private final SingleNodeNodeStream stream;
		private final boolean consumed;
		
		public SingleNodeMark(final SingleNodeNodeStream stream) {
			this.stream = stream;
			this.consumed = stream.consumed;
		}

		@Override
		public NodeStream resetStream() {
			stream.consumed = consumed;
			return stream;
		}
	}
	
	/**
	 * Node stream backed by a NodeList
	 */
	private static class NodeListNodeStream extends NodeStream {
		private final NodeList nodeList;
		private final int length;
		private int index;

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
			return new NodeListMark(this);
		}
	}
	
	/**
	 * Marks a position within a {@link NodeListNodeStream}
	 */
	private static class NodeListMark implements Mark {
		private final NodeListNodeStream stream;
		private final int index;
		
		public NodeListMark(final NodeListNodeStream stream) {
			this.stream = stream;
			this.index = stream.index;
		}

		@Override
		public NodeStream resetStream() {
			stream.index = index;
			return stream;
		}
	}
}
