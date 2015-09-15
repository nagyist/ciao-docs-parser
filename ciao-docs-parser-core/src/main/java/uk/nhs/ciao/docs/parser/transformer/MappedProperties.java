package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class MappedProperties {
	public abstract Object get(final PropertyName name);
	public abstract boolean set(final PropertyName name, final Object value);
	public abstract void addMappedProperty(final PropertyName source);
	
	public static class RootMappedProperties extends MappedProperties {
		private final Map<String, Object> destination;
		private final Set<PropertyName> sourceProperties;
		private final Set<PropertyName> mappedProperties;

		public RootMappedProperties(final Map<String, Object> source, final Map<String, Object> destination) {
			this.destination = Preconditions.checkNotNull(destination);
			this.mappedProperties = Sets.newLinkedHashSet();
			this.sourceProperties = findAllLeafPropertyNames(source);
		}
		
		public Set<PropertyName> getUnmappedProperties() {
			final Set<PropertyName> unmappedProperties = Sets.newLinkedHashSet(sourceProperties);
			unmappedProperties.removeAll(mappedProperties);
			return unmappedProperties;
		}
		
		public Map<String, Object> getDestination() {
			return destination;
		}
		
		@Override
		public Object get(final PropertyName name) {
			Object container;
			if (name.isNested()) {
				container = get(name.getParent());
			} else {
				container = destination;
			}
			
			Object result = null;
			if (name.isIndexed()) {
				if (container instanceof List<?>) {
					final List<?> list = (List<?>)container;
					if (name.getIndex() < list.size()) {
						result = list.get(name.getIndex());
					}
				}
			} else {
				if (container instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
					final Map<String, Object> map = (Map<String, Object>)container;
					result = map.get(name.getName());
				}
			}
			
			return result;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean set(final PropertyName name, final Object value) {
			final Object container = getOrCreateParent(name);
			if (container == null) {
				return false;
			}
			
			if (name.isIndexed()) {
				final List<Object> list = (List<Object>)container;
				while (name.getIndex() >= list.size()) {
					list.add(null);
				}
				list.set(name.getIndex(), value);
			} else {
				final Map<String, Object> map = (Map<String, Object>)container;
				map.put(name.getName(), value);
			}
			
			return true;
		}
		
		@Override
		public void addMappedProperty(final PropertyName source) {
			if (source != null) {
				mappedProperties.add(source);
			}
		}
		
		@SuppressWarnings("unchecked")
		private Object getOrCreateParent(final PropertyName name) {
			if (!name.isNested()) {
				return destination;
			}
			
			final PropertyName parentName = name.getParent();
			final Object parentContainer = getOrCreateParent(parentName);
			if (parentContainer == null) {
				return null;
			}
			
			Object container = null;
			
			if (parentName.isIndexed()) {
				final int index = parentName.getIndex();
				final List<Object> list = (List<Object>)parentContainer;
				while (parentName.getIndex() >= list.size()) {
					list.add(null);
				}
				
				container = list.get(index);
				if (container == null) {
					if (name.isIndexed()) {
						container = Lists.<Object>newArrayList();
					} else {
						container = Maps.<String, Object>newLinkedHashMap();
					}
					list.set(index, container);
				}
			} else {
				final Map<String, Object> map = (Map<String, Object>)parentContainer;
				container = map.get(parentName.getName());
				
				if (container == null) {
					if (name.isIndexed()) {
						container = Lists.<Object>newArrayList();
					} else {
						container = Maps.<String, Object>newLinkedHashMap();
					}
					map.put(parentName.getName(), container);
				}
			}
			
			// Sanity check for previously existing entries
			if (name.isIndexed()) {
				if (!(container instanceof List<?>)) {
					container = null;
				}
			} else {
				if (!(container instanceof Map<?, ?>)) {
					container = null;
				}
			}
			
			return container;
		}
	}
	
	public static class NestedMappedProperties extends MappedProperties {
		private final MappedProperties parent;
		private final PropertyName sourcePrefix;
		
		public NestedMappedProperties(final MappedProperties parent, final PropertyName sourcePrefix) {
			this.parent = Preconditions.checkNotNull(parent);
			this.sourcePrefix = Preconditions.checkNotNull(sourcePrefix);
		}
		
		@Override
		public Object get(final PropertyName name) {
			return parent.get(name);
		}
		
		@Override
		public boolean set(final PropertyName name, final Object value) {
			return parent.set(name, value);
		}
		
		@Override
		public void addMappedProperty(final PropertyName source) {
			if (source == null) {
				return;
			}
			
			parent.addMappedProperty(sourcePrefix.getChild(source));
		}
	}
	
	private static Set<PropertyName> findAllLeafPropertyNames(final Map<String, Object> map) {
		final Set<PropertyName> names = Sets.newLinkedHashSet();
		findAllLeafPropertyNames(null, map, names);
		return names;
	}
	
	@SuppressWarnings("unchecked")
	private static void findAllLeafPropertyNames(final PropertyName parent, final Map<String, Object> map, final Set<PropertyName> names) {
		for (final Entry<String, Object> entry: map.entrySet()) {
				final PropertyName name = parent == null ? PropertyName.valueOf(entry.getKey()) : parent.getChild(entry.getKey());
				
				final Object value = entry.getValue();
				if (value instanceof List<?>) {
					findAllLeafPropertyNames(name, (List<?>)value, names);
				} else if (value instanceof Map<?,?>) {
					findAllLeafPropertyNames(name, (Map<String,Object>)value, names);
				} else {
					names.add(name);
				}
			}
	}
	
	@SuppressWarnings("unchecked")
	private static void findAllLeafPropertyNames(final PropertyName parent, final List<?> list, final Set<PropertyName> names) {
		for (int index = 0; index < list.size(); index++) {
			final PropertyName name = parent.getChild(index);
			
			final Object value = list.get(index);
			if (value instanceof List<?>) {
				findAllLeafPropertyNames(name, (List<?>)value, names);
			} else if (value instanceof Map<?,?>) {
				findAllLeafPropertyNames(name, (Map<String,Object>)value, names);
			} else {
				names.add(name);
			}
		}
	}
}
