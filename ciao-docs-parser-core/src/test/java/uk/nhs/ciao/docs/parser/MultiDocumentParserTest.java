package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import uk.nhs.ciao.io.MultiCauseIOException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Unit test for {@link MultiDocumentParser}
 */
public class MultiDocumentParserTest {
	private MultiDocumentParser parser;
	private DocumentParser delegate1;
	private DocumentParser delegate2;
	private DocumentParser delegate3;
	private InputStream in;
	
	@Before
	public void setup() throws IOException {
		delegate1 = mock(DocumentParser.class);
		delegate2 = mock(DocumentParser.class);
		delegate3 = mock(DocumentParser.class);
		parser = new MultiDocumentParser(delegate1, delegate2, delegate3);
		
		// Setup a mock (empty) input stream
		in = mock(InputStream.class, CALLS_REAL_METHODS);
		when(in.read()).thenReturn(-1);
	}
	
	@Test(expected=UnsupportedDocumentTypeException.class)
	public void whenNoParsersAreRegisteredThenUnsupportedDocumentTypeShouldBeThrown() throws
			UnsupportedDocumentTypeException, IOException {
		parser = new MultiDocumentParser();
		parser.parseDocument(in);
	}
	
	@Test
	public void whenASingleParserIsRegisteredThenExceptionsShouldBePropegated() throws
			UnsupportedDocumentTypeException, IOException {
		Exception expected = setMockToUnsupportedDocumentType(delegate1);
		parser = new MultiDocumentParser();
		parser.addParser(delegate1);
		
		try {
			parser.parseDocument(in);
			fail("UnsupportedDocumentTypeException should have been thrown");
		} catch (UnsupportedDocumentTypeException e) {
			// Verify that parser was tried
			verify(delegate1).parseDocument(any(InputStream.class));
			assertEquals(expected, e);
		}
		
		expected = setMockToIOException(delegate1);
		try {
			parser.parseDocument(in);
			fail("IOException should have been thrown");
		} catch (IOException e) {
			// Verify that parser was tried
			verify(delegate1).parseDocument(any(InputStream.class));
			assertEquals(expected, e);
		}
	}
	
	@Test
	public void whenANullParserIsRegisteredThenItShouldBeTolerated() throws
			UnsupportedDocumentTypeException, IOException {
		setMockToUnsupportedDocumentType(delegate1);
		setMockToUnsupportedDocumentType(delegate2);
		final Map<String, Object> properties = Maps.newHashMap();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		
		when(delegate3.parseDocument(any(InputStream.class))).thenReturn(properties);
		
		parser = new MultiDocumentParser(delegate2);
		parser.addParsers(Arrays.asList(delegate1, null, delegate3));
		
		final Map<String, Object> actual = parser.parseDocument(in);
		assertEquals(properties, actual);
	}

	@Test
	public void whenAParserSucceedsThenNoFurtherParsersShouldBeTried() throws UnsupportedDocumentTypeException, IOException {
		final Map<String, Object> properties = Maps.newHashMap();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		
		when(delegate1.parseDocument(any(InputStream.class))).thenReturn(properties);
		
		final Map<String, Object> actual = parser.parseDocument(in);
		assertEquals(properties, actual);
		
		verifyZeroInteractions(delegate2, delegate3);
	}
	
	@Test
	public void whenNoParsersSupportTheDocumentTypeThenUnsupportedDocumentTypeShouldBeThrow() throws UnsupportedDocumentTypeException, IOException {
		setMockToUnsupportedDocumentType(delegate1);
		setMockToUnsupportedDocumentType(delegate2);
		setMockToUnsupportedDocumentType(delegate3);
		
		try {
			parser.parseDocument(in);
			fail("UnsupportedDocumentTypeException should have been thrown");
		} catch (UnsupportedDocumentTypeException e) {
			// Verify that all parsers were tried
			verify(delegate1).parseDocument(any(InputStream.class));
			verify(delegate2).parseDocument(any(InputStream.class));
			verify(delegate3).parseDocument(any(InputStream.class));
		}
	}
	
	@Test
	public void whenAllParsersFailAndAtLeastOneThrowsIOExceptionThenMultiCauseIOExceptionShouldBeThrow() throws UnsupportedDocumentTypeException, IOException {
		final Set<Exception> causes = Sets.newHashSet(
				setMockToUnsupportedDocumentType(delegate1),
				setMockToIOException(delegate2),
				setMockToUnsupportedDocumentType(delegate3));
		
		try {
			parser.parseDocument(in);
			fail("MultiCauseIOException should have been thrown");
		} catch (MultiCauseIOException e) {
			// Verify that all parsers were tried
			verify(delegate1).parseDocument(any(InputStream.class));
			verify(delegate2).parseDocument(any(InputStream.class));
			verify(delegate3).parseDocument(any(InputStream.class));
			
			assertEquals(causes, Sets.newHashSet(e.getCauses()));
		}
	}
	
	private UnsupportedDocumentTypeException setMockToUnsupportedDocumentType(final DocumentParser delegate) throws UnsupportedDocumentTypeException, IOException {
		final UnsupportedDocumentTypeException exception = new UnsupportedDocumentTypeException();
		reset(delegate);
		when(delegate.parseDocument(any(InputStream.class))).thenThrow(exception);
		return exception;
	}
	
	private IOException setMockToIOException(final DocumentParser delegate) throws UnsupportedDocumentTypeException, IOException {
		final IOException exception = new IOException();
		reset(delegate);
		when(delegate.parseDocument(any(InputStream.class))).thenThrow(exception);
		return exception;
	}
}
