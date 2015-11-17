package uk.nhs.ciao.docs.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Unit tests for {@link PropertyName}
 */
public class PropertyNameTest {
	@Test
	public void testListChildren() {
		final PropertyName root = PropertyName.getRoot();
		
		final Map<String, Object> map = Maps.newLinkedHashMap();
		map.put("name", "John Smith");
		map.put("title", "Mr");
		map.put("age", 23);
		
		final List<PropertyName> expected = Lists.newArrayList();
		for (final String key: Arrays.asList("name", "title", "age")) {
			expected.add(PropertyName.valueOf(key));
		}
		
		Assert.assertEquals(expected, root.listChildren(map));
		
		final List<?> list = Arrays.asList("first", "second", "third");
		expected.clear();
		for (final String key: Arrays.asList("[0]", "[1]", "[2]")) {
			expected.add(PropertyName.valueOf(key));
		}
		
		Assert.assertEquals(expected, root.listChildren(list));
		
		expected.clear();
		Assert.assertEquals(expected, root.listChildren("not a containter"));
	}
	
	@Test
	public void testIsRoot() {
		Assert.assertTrue(PropertyName.getRoot().isRoot());
		Assert.assertTrue(PropertyName.valueOf(null).isRoot());
		Assert.assertTrue(PropertyName.valueOf("").isRoot());
		Assert.assertFalse(PropertyName.valueOf("name").isRoot());
		Assert.assertFalse(PropertyName.valueOf("[0]").isRoot());
	}
	
	@Test
	public void testIsIndexed() {
		Assert.assertTrue(PropertyName.valueOf("[0]").isIndexed());
		Assert.assertTrue(PropertyName.valueOf("keys[0][1]").isIndexed());
		Assert.assertFalse(PropertyName.valueOf("keys").isIndexed());
		Assert.assertFalse(PropertyName.getRoot().isIndexed());
		Assert.assertFalse(PropertyName.valueOf("[0].keys").isIndexed());
	}
	
	@Test
	public void testGetIndex() {
		Assert.assertEquals(0, PropertyName.valueOf("[0]").getIndex());
		Assert.assertEquals(1, PropertyName.valueOf("keys[0][1]").getIndex());
		Assert.assertEquals(-1, PropertyName.valueOf("keys").getIndex());
		Assert.assertEquals(-1, PropertyName.getRoot().getIndex());
	}
	
	@Test
	public void testIsNamed() {
		Assert.assertTrue(PropertyName.valueOf("title").isNamed());
		Assert.assertTrue(PropertyName.valueOf("keys[0][1].name").isNamed());
		Assert.assertFalse(PropertyName.valueOf("[2]").isNamed());
		Assert.assertFalse(PropertyName.getRoot().isNamed());
		Assert.assertFalse(PropertyName.valueOf("keys[2]").isNamed());
	}
	
	@Test
	public void testGetName() {
		Assert.assertEquals("title", PropertyName.valueOf("title").getName());
		Assert.assertEquals("name", PropertyName.valueOf("keys[0][1].name").getName());
		Assert.assertNull(PropertyName.valueOf("[2]").getName());
		Assert.assertNull(PropertyName.getRoot().getName());
	}
	
	@Test
	public void testGetParentContainer() {
		final PropertyName name = PropertyName.valueOf("[2].names.title");
		final List<Object> list = Lists.newArrayList();
		
		// Default option is to not create missing parents
		Assert.assertNull(name.getParentContainer(list));
		Assert.assertTrue(list.isEmpty());
		
		// Check that the parents are created when the option is enabled
		final Object container = name.makeParents(list);
		Assert.assertNotNull(container);
		Assert.assertTrue(container instanceof Map);
		
		// 'missing' indices should be null filled
		Assert.assertEquals(3, list.size());
		Assert.assertNull(list.get(0));
		Assert.assertNull(list.get(1));
		Assert.assertTrue(list.get(2) instanceof Map);
		Assert.assertEquals(container, ((Map<?,?>)list.get(2)).get("names"));
	}
	
	@Test
	public void testRemoveFromList() {
		final PropertyName name = PropertyName.getRoot().getChild(1);
		
		// Elements at the end of the list are removed - the size is shortened
		List<?> list = Lists.newArrayList("first", "second");	
		Assert.assertTrue(name.remove(list));
		Assert.assertEquals(1, list.size());
		
		// Elements in the middle of the list are nulled - the size is not changed
		list = Lists.newArrayList("first", "second", "third");	
		Assert.assertTrue(name.remove(list));
		Assert.assertEquals(3, list.size());
		Assert.assertNull(list.get(1));
		
		// Out of bounds indices have no effect
		list = Lists.newArrayList("first");	
		Assert.assertFalse(name.remove(list));
		Assert.assertEquals(1, list.size());
		
		// Containers of the wrong kind have no effect
		Assert.assertFalse(name.remove(Maps.newLinkedHashMap()));
	}
	
	@Test
	public void testRemoveFromMap() {
		final PropertyName name = PropertyName.getRoot().getChild("name");
		final Map<String, Object> map = Maps.newLinkedHashMap();
		map.put("name", "Mr Example");
		
		// Existing keys are removed
		Assert.assertTrue(name.remove(map));
		Assert.assertFalse(map.containsKey("name"));
		Assert.assertTrue(map.isEmpty());
		
		// Missing keys have no effect
		map.put("another_name", "Someone Else");
		Assert.assertFalse(name.remove(map));
		Assert.assertEquals(1, map.size());
		
		// Containers of the wrong kind have no effect
		Assert.assertFalse(name.remove(Lists.newArrayList("name")));
	}
	
	@Test
	public void testGetParent() {
		final PropertyName parent = PropertyName.valueOf("names");
		Assert.assertEquals(parent, PropertyName.valueOf("names[2]").getParent());
		Assert.assertEquals(parent, PropertyName.valueOf("names.title").getParent());
		Assert.assertNull(PropertyName.getRoot().getParent());
	}
	
	@Test
	public void testGetChild() {
		final PropertyName parent = PropertyName.valueOf("names");
		Assert.assertEquals(PropertyName.valueOf("names[1]"), parent.getChild(PropertyName.valueOf("[1]")));
	}
	
	@Test
	public void testToPropertySelectorRoundtrip() {
		final PropertyName name = PropertyName.valueOf("names[2].extended.title");
		final PropertySelector expected = PropertySelector.valueOf("names[2].extended.title");
		final PropertySelector actual = name.toPropertySelector();
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(name, actual.toPropertyName());
	}
}
