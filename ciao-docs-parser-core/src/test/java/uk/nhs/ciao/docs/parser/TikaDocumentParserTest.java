package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;

/**
 * Unit tests for {@link TikaDocumentParser}
 */
@RunWith(MockitoJUnitRunner.class)
public class TikaDocumentParserTest {
	private TikaDocumentParser parser;
	
	@Mock
	private Parser tikaParser;
	
	@Mock
	private PropertiesExtractor<Document> propertiesExtractor;

	@Before
	public void setup() throws ParserConfigurationException {
		this.parser = new TikaDocumentParser(tikaParser, propertiesExtractor);
	}
	
	@Test
	public void whenTikaParsesADocumentThenPropertyExtractionIsAttempted() throws IOException,
			SAXException, TikaException, UnsupportedDocumentTypeException {
		final ArgumentCaptor<ContentHandler> captor = ArgumentCaptor.forClass(ContentHandler.class);
		
		final Answer<Void> answer = new Answer<Void>() {			
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final ContentHandler handler = captor.getValue();
				handler.startDocument();
				handler.startElement("uri", "html", "html", mock(Attributes.class));
				handler.endElement("uri", "html", "html");
				handler.endDocument();
				
				return null;
			}
		};
		
		doAnswer(answer).when(tikaParser).parse(any(InputStream.class), captor.capture(),
				any(Metadata.class), any(ParseContext.class));

		
		final Map<String, Object> properties = Maps.newHashMap();
		properties.put("my-key", "my-value");
		when(propertiesExtractor.extractProperties(any(Document.class))).thenReturn(properties);
		
		final Map<String, Object> actual = parser.parseDocument(mock(InputStream.class));
		assertEquals(properties, actual);
	}

	@Test(expected=IOException.class)
	public void testSaxExceptionsAreThrowAsIOExceptions() throws Exception {
		doThrow(SAXException.class).when(tikaParser).parse(any(InputStream.class), any(ContentHandler.class),
				any(Metadata.class), any(ParseContext.class));
		parser.parseDocument(mock(InputStream.class));
	}
	
	@Test(expected=IOException.class)
	public void testTikaExceptionsAreThrowAsIOExceptions() throws Exception {
		doThrow(TikaException.class).when(tikaParser).parse(any(InputStream.class), any(ContentHandler.class),
				any(Metadata.class), any(ParseContext.class));
		parser.parseDocument(mock(InputStream.class));
	}
}
