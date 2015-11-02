package uk.nhs.ciao.docs.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.nhs.ciao.util.SimpleEntry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PropertySelector {
	private static final Object ANY_KEY = new Object() {
		public String toString() {
			return "*";
		}
	};
	
	private static final Object ANY_INDEX = new Object() {
		public String toString() {
			return "[*]";
		}
	};
	
	private static final PropertySelector ROOT = new PropertySelector(Lists.newArrayList());
	
	private final List<Object> segments;
	private final boolean multi;
	
	public static PropertySelector valueOf(final String path) {
		return Strings.isNullOrEmpty(path) ? ROOT : new PropertySelector(getSegmentsFromPath(path));
	}
	
	private PropertySelector(final List<Object> segments) {
		this.segments = segments;
		this.multi = containsWildcard(segments);
	}
	
	public boolean isRoot() {
		return segments.isEmpty();
	}
	
	public boolean isMulti() {
		return multi;
	}
	
	public String getPath() {
		final StringBuilder builder = new StringBuilder();
		
		for (final Object segment: segments) {
			if (segment instanceof Integer) {
				builder.append('[').append(segment).append(']');
			} else {
				if (segment != ANY_INDEX && builder.length() > 0) {
					builder.append('.');
				}
				builder.append(segment);
			}
		}
		
		return builder.toString();
	}
	
	public PropertySelector getParent() {
		return isRoot() ? null : new PropertySelector(Lists.newArrayList(segments.subList(0, segments.size() - 1)));
	}
	
	public PropertySelector getChild(final String childPath) {
		final List<Object> childSegments = getSegmentsFromPath(childPath);
		Preconditions.checkArgument(!childSegments.isEmpty(), "childPath must be provided");
		
		final List<Object> joinedSegments = Lists.newArrayList(segments);
		joinedSegments.addAll(childSegments);
		return new PropertySelector(joinedSegments);
	}
	
	public Entry<String, Object> select(final Map<String, Object> properties) {
		return select(Object.class, properties);
	}
	
	public Object selectValue(final Map<String, Object> properties) {
		return selectValue(Object.class, properties);
	}
	
	public <T> Entry<String, T> select(final Class<T> type, final Map<String, Object> properties) {
		if (properties == null) {
			return null;
		} else if (isRoot()) {
			return type.isInstance(properties) ? SimpleEntry.valueOf("", type.cast(properties)) : null;
		}
		
		final Map<String, T> results = selectAll(type, properties);
		return Iterables.getFirst(results.entrySet(), null);
	}
	
	public <T> T selectValue(final Class<T> type, final Map<String, Object> properties) {
		final Entry<String, T> entry = select(type, properties);
		return entry == null ? null : entry.getValue();
	}
	
	public Map<String, Object> selectAll(final Map<String, Object> properties) {
		return selectAll(Object.class, properties);
	}
	
	public Collection<Object> selectAllValues(final Map<String, Object> properties) {
		return selectAll(properties).values();
	}
	
	public <T> Map<String, T> selectAll(final Class<T> type, final Map<String, Object> properties) {
		if (properties == null) {
			return Maps.<String, T>newHashMap();
		}
		
		final Map<String, T> results = Maps.newLinkedHashMap();
		if (isRoot()) {
			if (type.isInstance(properties)) {
				results.put("", type.cast(properties));
			}
		} else {
			final StringBuilder prefix = new StringBuilder();
			findAndAddSelected(type, results, prefix, properties, 0);
		}
		
		return results;
	}
	
	public <T> Collection<T> selectAllValues(final Class<T> type, final Map<String, Object> properties) {
		return selectAll(type, properties).values();
	}
	
	@Override
	public int hashCode() {
		return segments.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final PropertySelector other = (PropertySelector) obj;
		return segments.equals(other.segments);
	}
	
	@Override
	public String toString() {
		return segments.toString();
	}

	private <T> void findAndAddSelected(final Class<T> type, final Map<String, T> results,
			final StringBuilder prefix, final Object value, final int index) {
		if (value == null) {
			return;
		} else if (index >= segments.size()) {
			// Found a potential match
			if (type.isInstance(value)) {
				results.put(prefix.toString(), type.cast(value));
			}
			return;
		}
		
		final Object segment = segments.get(index);
		final int prefixLength = prefix.length();
		if (segment == ANY_KEY) {
			// Loop all elements in map
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>)value;
				for (final Entry<String, Object> entry: map.entrySet()) {
					if (prefixLength > 0) {
						prefix.append('.');
					}
					prefix.append(entry.getKey());
					findAndAddSelected(type, results, prefix, entry.getValue(), index + 1);
					prefix.setLength(prefixLength);
				}
			}
		} else if (segment == ANY_INDEX) {
			// Loop all elements in list
			if (value instanceof List) {
				int listIndex = 0;
				for (final Object next:(List<?>)value) {
					prefix.append('[').append(listIndex).append(']');
					findAndAddSelected(type, results, prefix, next, index + 1);
					prefix.setLength(prefixLength);
					listIndex++;
				}
			}
		} else if (segment instanceof Integer) {
			// Match index in list
			final int targetIndex = (Integer)segment;
			if (value instanceof List && targetIndex < ((List<?>)value).size()) {
				final Object next = ((List<?>)value).get(targetIndex);
				prefix.append('[').append(targetIndex).append(']');
				findAndAddSelected(type, results, prefix, next, index + 1);
				prefix.setLength(prefixLength);
			}
		} else { // String
			// Match named key in map
			final String targetKey = (String)segment;
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>)value;
				final Object next = map.get(targetKey);
				
				if (prefixLength > 0) {
					prefix.append('.');
				}
				prefix.append(targetKey);
				findAndAddSelected(type, results, prefix, next, index + 1);
				prefix.setLength(prefixLength);
			}
		}
	}
	
	private static boolean containsWildcard(final List<Object> segments) {
		return segments.contains(null);
	}
	
	private static final Pattern PATTERN = Pattern.compile("(.+?)(?:(?:(?:\\[(\\d+|\\*)\\])\\.?)|\\.|\\z)");
	private static List<Object> getSegmentsFromPath(final String path) {
		if (path == null || path.length() == 0) {
			return Lists.newArrayList();
		}
		
		final List<Object> segments = Lists.newArrayList();
		final Matcher matcher = PATTERN.matcher(path);
		while (matcher.find()) {
			if ("*".equals(matcher.group(1))) {
				segments.add(ANY_KEY);
			} else {
				segments.add(matcher.group(1));
			}
			
			if (matcher.group(2) != null) {
				if ("*".equals(matcher.group(2))) {
					segments.add(ANY_INDEX);
				} else {
					segments.add(Integer.valueOf(matcher.group(2)));
				}
			}
		}
		
		return segments;
	}
}
