package uk.nhs.ciao.docs.parser;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Represents a type of container within a dynamic property structure (i.e. contains other properties).
 */
enum ContainerType {
	/**
	 * Represents a map type of string, object pairs
	 * 
	 * @see PropertyPath#ANY_KEY
	 */
	MAP {
		@Override
		public boolean isType(final Object value) {
			return value instanceof Map;
		}
		
		@Override
		public Object get(final Object container, final Object segment) {
			final Map<String, Object> map = toMap(container);
			final String key = toKey(segment);
			if (map == null || key == null) {
				return null;
			}
			
			return map.get(key);
		}
		
		@Override
		public boolean set(final Object container, final Object segment, final Object value) {
			final Map<String, Object> map = toMap(container);
			final String key = toKey(segment);
			if (map == null || key == null) {
				return false;
			}
			
			map.put(key, value);
			return true;
		}
		
		@Override
		public boolean remove(final Object container, final Object segment) {
			final Map<String, Object> map = toMap(container);
			final String key = toKey(segment);
			if (map == null || key == null) {
				return false;
			}
			
			final boolean removed = map.containsKey(key);
			if (removed) {
				map.remove(key);
			}
			return removed;
		}
		
		@Override
		public Map<String, Object> createContainer() {
			return Maps.newLinkedHashMap();
		}
		
		@SuppressWarnings("unchecked")
		private Map<String, Object> toMap(final Object container) {
			return isType(container) ? (Map<String, Object>)container : null;
		}
		
		private String toKey(final Object segment) {
			return segment instanceof String ? (String) segment : null;
		}
	},
	
	/**
	 * Represents a list type of objects
	 * 
	 * @see PropertyPath#ANY_STRING
	 */
	LIST {
		@Override
		public boolean isType(final Object value) {
			return value instanceof List;
		}
		
		@Override
		public Object get(final Object container, final Object segment) {
			final List<Object> list = toList(container);
			final Integer index = toIndex(segment);
			if (list == null || index == null) {
				return null;
			}
			
			return list.size() > index ? list.get(index) : null;
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * If the specified index is greater than the current container size, the list
		 * is extended using null values.
		 */
		@Override
		public boolean set(final Object container, final Object segment, final Object value) {
			final List<Object> list = toList(container);
			final Integer index = toIndex(segment);
			if (list == null || index == null) {
				return false;
			}
			
			while (list.size() <= index) {
				list.add(null);
			}
			
			list.set(index, value);
			return true;
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * If the specified index is the end of the list, the list is truncated. Otherwise
		 * the entry is nulled
		 */
		@Override
		public boolean remove(final Object container, final Object segment) {
			final List<Object> list = toList(container);
			final Integer index = toIndex(segment);
			if (list == null || index == null) {
				return false;
			}
			
			
			boolean removed = false;
			if (index == list.size() - 1) {
				list.remove(index - 1);
				removed = true;
			} else if (index < list.size()) {
				list.set(index, null);
				removed = true;
			}
			
			return removed;
		}
		
		@Override
		public List<Object> createContainer() {
			return Lists.newArrayList();
		}
		
		@SuppressWarnings("unchecked")
		private List<Object> toList(final Object container) {
			return isType(container) ? (List<Object>)container : null;
		}
		
		private Integer toIndex(final Object segment) {
			return segment instanceof Integer ? (Integer) segment : null;
		}
	};
	
	/**
	 * Tests if the specified value is a container of this type
	 */
	public abstract boolean isType(final Object value);
	
	/**
	 * Gets the value of the specified property from a container
	 * 
	 * @param container The container to get the value from
	 * @param segment The segment identifying the child property within the container
	 * @return The value or null if there is no associated value or the container or segment
	 * 			is of the wrong kind
	 */
	public abstract Object get(final Object container, final Object segment);
	
	/**
	 * Sets the value of the specified property on a container
	 * <p>
	 * Existing values are overwritten
	 * 
	 * @param container The container to set the value on
	 * @param segment The segment identifying the child property within the container
	 * @param value The value to set
	 * @return true if the value was set, or false otherwise
	 */
	public abstract boolean set(final Object container, final Object segment, final Object value);
	
	/**
	 * Removes the specified property from a container
	 * 
	 * @param container The container to remove the the value from
	 * @param segment The segment identifying the child property within the container
	 * @return true if the value was removed, or false otherwise
	 * 		(e.g. the container is of the wrong kind or the value did not exist)
	 */
	public abstract boolean remove(final Object container, final Object segment);
	
	/**
	 * Creates an empty container of this type
	 */
	public abstract Object createContainer();
	
	/**
	 * Finds the type of container capable of containing the addressed property
	 * <p>
	 * E.g. if the segment is a string then the type of container must be a map
	 */
	public static ContainerType getContainingType(final Object segment) {
		ContainerType type = null;
		
		if (segment instanceof String || segment == PropertyPath.ANY_INDEX) {
			type = MAP;
		} else if (segment instanceof Integer || segment == PropertyPath.ANY_INDEX) {
			type = LIST;
		}
		
		return type;
	}

	/**
	 * Tests if the specified object can contain other properties
	 */
	public static boolean isContainer(final Object candidate) {
		return candidate instanceof List || candidate instanceof Map;
	}
}