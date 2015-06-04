package uk.nhs.ciao.docs.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link Document}
 */
public class DocumentTest {
	@Test
	public void testIsEmpty() {
		Document document = new Document("name.txt", new byte[]{});
		Assert.assertTrue(document.isEmpty());
		
		document = new Document("name.txt", new byte[]{1, 2, 3, 4});
		Assert.assertFalse(document.isEmpty());
	}
	
	@Test
	public void testGetBase64Content() {
		final Document document = new Document("name.txt", new byte[]{1, 2, 3, 4});
		Assert.assertEquals("Base64 content", "AQIDBA==", document.getBase64Content());
	}
}
