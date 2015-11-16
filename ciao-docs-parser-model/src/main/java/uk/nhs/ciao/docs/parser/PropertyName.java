package uk.nhs.ciao.docs.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

public final class PropertyName {
	private static final PropertyName ROOT = new PropertyName(new Object[0]);
	
	private final Object[] segments;
	private volatile String path;
	private int hash;
	
	public static PropertyName valueOf(final String path) {
		return Strings.isNullOrEmpty(path) ? ROOT : new PropertyName(PropertyPath.parse(path));
	}
	
	public static PropertyName getRoot() {
		return ROOT;
	}
	
	PropertyName(final Object[] segments) {
		this(segments, null);
	}
	
	private PropertyName(final Object[] segments, final String path) {
		this.segments = segments;
		this.path = path;
	}
	
	public boolean isRoot() {
		return segments.length == 0;
	}
	
	public boolean isIndexed() {
		return lastSegment() instanceof Integer;
	}
	
	public boolean isNamed() {
		return lastSegment() instanceof String;
	}
	
	public String getName() {
		return isNamed() ? (String)lastSegment() : null;
	}
	
	public int getIndex() {
		return isIndexed() ? (Integer)lastSegment() : -1;
	}
	
	public String getPath() {
		if (path == null) {
			path = PropertyPath.toString(segments);
		}
		
		return path;
	}
	
	/**
	 * Returns the PropertySelector equivalent to this name
	 */
	public PropertySelector toPropertySelector() {
		return new PropertySelector(Arrays.copyOf(segments, segments.length));
	}
	
	public PropertyName getChild(final PropertyName name) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isRoot());
		
		final Object[] childSegments = ObjectArrays.concat(segments, name.segments, Object.class);
		return new PropertyName(childSegments);
	}
	
	public PropertyName getChild(final String name) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		
		final Object[] childSegments = Arrays.copyOf(segments, segments.length + 1);
		childSegments[segments.length] = name;
		return new PropertyName(childSegments);
	}
	
	public PropertyName getChild(final int index) {
		Preconditions.checkArgument(index >= 0);
		
		final Object[] childSegments = Arrays.copyOf(segments, segments.length + 1);
		childSegments[segments.length] = index;
		return new PropertyName(childSegments);
	}
	
	public PropertyName getParent() {
		return isRoot() ? null : new PropertyName(Arrays.copyOf(segments, segments.length - 1));
	}
	
	public List<PropertyName> listChildren(final Object source) {
		final List<PropertyName> children = Lists.newArrayList();

		final Object value = PropertyPath.getValue(Object.class, source, segments);
		if (value instanceof List) {
			final List<?> list = (List<?>)value;
			for (int index = 0; index < list.size(); index++) {
				children.add(getChild(index));
			}
		} else if (value instanceof Map) {
			final Map<?, ?> map = (Map<?, ?>)value;
			for (final Object key: map.keySet()) {
				if (key instanceof String) {
					children.add(getChild((String)key));
				}
			}
		}
		
		return children;
	}
	
	public Object get(final Object source) {
		return get(Object.class, source);
	}
	
	public <T> T get(final Class<T> type, final Object source) {
		return PropertyPath.getValue(type, source, segments);
	}
	
	public boolean set(final Object source, final Object value) {
		return PropertyPath.setValue(source, segments, value);
	}
	
	public Object getParentContainer(final Object source) {
		final boolean createIfMissing = false;
		return getParentContainer(source, createIfMissing);
	}
	
	public Object getParentContainer(final Object source, final boolean createIfMissing) {
		return PropertyPath.getParentContainer(source, segments, createIfMissing);
	}
	
	public boolean remove(final Object source) {
		return PropertyPath.remove(source, segments);
	}
	
	@Override
	public int hashCode() {
		int result = hash;
		if (result == 0) {
			result = Arrays.hashCode(segments);
			hash = result; // safe to publish without volatile
		}
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return Arrays.equals(segments, ((PropertyName)obj).segments);
	}
	
	@Override
	public String toString() {
		return getPath();
	}
	
	private Object lastSegment() {
		return segments.length == 0 ? null : segments[segments.length - 1];
	}
	
	public void accept(final Object source, final PropertyVisitor visitor) {
		visitor.onProperty(this, source);

		if (source instanceof List<?>) {
			int index = 0;
			for (final Object childValue: (List<?>)source) {
				getChild(index).accept(childValue, visitor);
				index++;
			}
		} else if (source instanceof Map<?,?>) {
			final Map<?, ?> map = (Map<?, ?>)source;
			for (final Entry<?, ?> entry: map.entrySet()) {
				if (entry.getKey() instanceof String) {
					getChild((String)entry.getKey()).accept(entry.getValue(),
							visitor);
				}
			}
		}
	}
	
	public static Set<PropertyName> findAll(final Object source, final boolean includeContainers) {
		final Set<PropertyName> names = Sets.newLinkedHashSet();
		ROOT.accept(source, new PropertyVisitor() {
			@Override
			public void onProperty(final PropertyName name, final Object value) {
				if (includeContainers || !ContainerType.isContainer(value)) {
					names.add(name);
				}
 			}
		});
		return names;
	}
	
	public static void main(final String[] args) throws Exception {
		for (final String path: Arrays.asList("", "value", "[1]", "nested.value", "multi[1][2]", "combined.values[12].t1", "escape\\.d")) {
			final PropertyName name = valueOf(path);
			System.out.println("segments: " + Arrays.toString(name.segments));
			System.out.println("path: " + name);
			System.out.println();
		}
	}
}
