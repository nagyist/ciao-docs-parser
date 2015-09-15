package uk.nhs.ciao.docs.parser.transformer;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class PropertyName {
	private final String path;
	private final PropertyName parent;
	private final String name;
	private final int index;

	public PropertyName(final String path, final PropertyName parent, final String name, final int index) {
		this.path = Preconditions.checkNotNull(path);
		this.parent = parent;
		this.name = Preconditions.checkNotNull(name);
		this.index = index >= 0 ? index : -1;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isNested() {
		return parent != null;
	}

	public PropertyName getParent() {
		return parent;
	}
	
	public boolean isIndexed() {
		return index >= 0;
	}
	
	public int getIndex() {
		return index;
	}
	
	public PropertyName getChild(final PropertyName name) {
		Preconditions.checkNotNull(name);
		return getChild(name.getPath());
	}
	
	public PropertyName getChild(final String childName) {
		Preconditions.checkNotNull(childName);
		final String trimmedChildName = childName.trim();
		Preconditions.checkArgument(!trimmedChildName.isEmpty());
		
		if (trimmedChildName.charAt(0) == '[') {
			return valueOf(path + trimmedChildName);
		} else {
			return valueOf(path + "." + trimmedChildName);
		}
	}
	
	public PropertyName getChild(final int index) {
		Preconditions.checkArgument(index >= 0);
		return valueOf(path + "[" + index + "]");
	}
	
	@Override
	public int hashCode() {
		return path.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || !(obj instanceof PropertyName)) {
			return false;
		}
		
		final PropertyName other = (PropertyName)obj;
		return path.equals(other.path);
	}
	
	@Override
	public String toString() {
		return path;
	}
	
	public static PropertyName valueOf(final String path) {
		if (Strings.isNullOrEmpty(path)) {
			return null;
		}
		
		return CACHE.getUnchecked(path);
	}
	
	public static PropertyName valueOf(final PropertyName parent, final String childPath) {
		if (Strings.isNullOrEmpty(childPath)) {
			return parent;
		} else if (parent == null) {
			return valueOf(childPath);
		} else {
			return parent.getChild(childPath);
		}
	}
	
	private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]\\Z");
	
	private static LoadingCache<String, PropertyName> CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build(new PropertyNameCacheLoader());
	
	private static class PropertyNameCacheLoader extends CacheLoader<String, PropertyName> {
		@Override
		public PropertyName load(final String path) {
			assert !Strings.isNullOrEmpty(path);
			
			final String name;
			final int index;
			final String parentPath;
			
			Matcher matcher = INDEX_PATTERN.matcher(path);
			if (matcher.matches()) {
				index = Integer.valueOf(matcher.group(1));
				name = "[" + index + "]";
				parentPath = path.substring(0, matcher.regionStart());
			} else {
				int dotIndex = path.lastIndexOf('.');
				if (dotIndex < 0) {
					name = path;
					parentPath = null;
				} else {
					name = path.substring(dotIndex + 1);
					parentPath = path.substring(0, dotIndex);
				}
				index = -1;
				
			}
			
			final PropertyName parent = valueOf(parentPath);
			return new PropertyName(path, parent, name, index);
		}
	}
}
