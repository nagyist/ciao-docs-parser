package uk.nhs.ciao.docs.parser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PropertyName {
	private final Object[] segments;
	private volatile String path;
	private int hash;

	public static PropertyName valueOf(final String path) {
		return new PropertyName(parsePath(path));
	}
	
	private PropertyName(final Object[] segments) {
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
	
	public String getPath() {
		if (path != null) {
			return path;
		}
		
		final StringBuilder builder = new StringBuilder();
		for (final Object segment: segments) {
			if (segment instanceof Integer) {
				builder.append('[').append(segment).append(']');
			} else {
				if (builder.length() > 0) {
					builder.append('.');
				}
				builder.append(encodeSegment(segment.toString()));
			}
		}
		
		path = builder.toString();
		
		return path;
	}
	
	public List<PropertyName> getChildren(final Object source) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int hashCode() {
		int result = hash;
		if (result == 0) {
			// safe to publish without volatile
			hash = Arrays.hashCode(segments);
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
	
	private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("([\\.\\[\\]\\\\])");
	
	/**
	 * regex to replace all special characters . [ ] \ with \. \[ \] \\
	 */
	private static String encodeSegment(final String segment) {
		final Matcher matcher = SPECIAL_CHARACTERS_PATTERN.matcher(segment);
		return matcher.replaceAll("\\\\$1");
	}
	
	private static Object[] parsePath(final String path) {
		if (Strings.isNullOrEmpty(path)) {
			return new Object[0];
		}
		
		final List<Object> segments = Lists.newArrayList();
		final StringBuilder builder = new StringBuilder();
		ParseMode mode = ParseMode.ROOT;
		boolean delimited = false;
		for (int index = 0; index < path.length(); index++) {
			char c = path.charAt(index);
			// delimiter
			if (c == '\\') {
				switch (mode) {
				case ROOT:
				case KEY:
				case AFTER_KEY:
					if (delimited) {
						builder.append(c);
						mode = ParseMode.KEY;
					} else if (index + 1 == path.length()) {
						throw new IllegalArgumentException("Dangling escape character - pos: " + index);
					}
					delimited = !delimited;
					break;
				default:
					throw new IllegalArgumentException("Invalid escape character - pos: " + index);
				}
			} else if (delimited) {
				builder.append(c);
				mode = ParseMode.KEY;
				delimited = false;
			}
			
			// special characters
			else if (c == '[') {
				if (mode == ParseMode.KEY) {
					segments.add(builder.toString());
					builder.setLength(0);
				} else if (mode != ParseMode.ROOT && mode != ParseMode.AFTER_INDEX) {
					throw new IllegalArgumentException("Invalid start index character - pos: " + index);
				}
				
				mode = ParseMode.INDEX;
			} else if (c == ']') {
				if (mode != ParseMode.INDEX) {
					throw new IllegalArgumentException("Invalid close index character - pos: " + index);
				} else if (builder.length() == 0) {
					throw new IllegalArgumentException("Invalid close index character - missing index - pos: " + index);
				}
				
				segments.add(Integer.valueOf(builder.toString()));
				builder.setLength(0);
				
				mode = ParseMode.AFTER_INDEX;
			} else if (c == '.') {
				if (mode == ParseMode.KEY) {
					segments.add(builder.toString());
					builder.setLength(0);
				} else if (mode != ParseMode.AFTER_INDEX) {
					throw new IllegalArgumentException("Invalid key separator - pos: " + index);
				}
				
				mode = ParseMode.AFTER_KEY;
			}
			
			// digits
			else if (c >= '0' && c <= '9') {
				if (mode == ParseMode.AFTER_INDEX) {
					throw new IllegalArgumentException("Invalid character - expected delimiter - pos: " + index);
				} else if (mode != ParseMode.INDEX) {
					mode = ParseMode.KEY;
				}
				builder.append(c);
			}
			
			// standard characters
			else {
				if (mode == ParseMode.AFTER_INDEX) {
					throw new IllegalArgumentException("Invalid character - expected delimiter - pos: " + index);
				} else if (mode == ParseMode.INDEX) {
					throw new IllegalArgumentException("Invalid character - expected digit - pos: " + index);
				}
				
				builder.append(c);
				mode = ParseMode.KEY;
			}
		}
		
		switch (mode) {
		case KEY:
			segments.add(builder.toString());
			builder.setLength(0);
			break;
		case ROOT:
		case AFTER_INDEX:
			// NOOP
			break;
		default:
			throw new IllegalArgumentException("Incomplete segment in path");
		}
		
		return segments.toArray();
	}
	
	private enum ParseMode {
		ROOT, KEY, AFTER_KEY, INDEX, AFTER_INDEX;
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
