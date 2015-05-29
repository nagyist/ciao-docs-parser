package uk.nhs.ciao.docs.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.io.MultiCauseIOException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

/**
 * A document parser which attempts to parse the document using multiple
 * delegate parsers.
 * <p>
 * The parsers are attempted in registration order until one parser completes a successful
 * parse.
 */
public final class MultiDocumentParser implements DocumentParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiDocumentParser.class);
	
	private final Set<DocumentParser> parsers;
	
	/**
	 * Constructs a new empty multi-document parser. Delegate parsers can be added later
	 * after construction.
	 */
	public MultiDocumentParser() {
		parsers = Sets.newLinkedHashSet();
	}
	
	/**
	 * Constructs a new multi-document parser which delegates to the specified parsers
	 * when attempting to parse documents. Additional delegate parsers can be added
	 * later after construction.
	 * <p>
	 * Delegate parsers are attempted in registration order.
	 * 
	 * @param parsers The delegate parsers to register
	 */
	public MultiDocumentParser(final DocumentParser... parsers) {
		this();
		addParsers(parsers);
	}
	
	/**
	 * Registers the specified parser as a delegate to use when attempting to parse documents.
	 * <p>
	 * Delegate parsers are attempted in registration order
	 * 
	 * @param parser The delegate parser to register
	 */
	public void addParser(final DocumentParser parser) {
		if (parser != null) {
			parsers.add(parser);
		}
	}
	
	/**
	 * Registers the specified parsers as delegates to use when attempting to parse documents.
	 * <p>
	 * Delegate parsers are attempted in registration order
	 * 
	 * @param parser The delegate parsers to register
	 */
	public void addParsers(final Iterable<? extends DocumentParser> parsers) {
		for (final DocumentParser parser: parsers) {
			addParser(parser);
		}
	}
	
	/**
	 * Registers the specified parsers as delegates to use when attempting to parse documents.
	 * <p>
	 * Delegate parsers are attempted in registration order
	 * 
	 * @param parser The delegate parsers to register
	 */
	public void addParsers(final DocumentParser... parsers) {
		for (final DocumentParser parser: parsers) {
			addParser(parser);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws UnsupportedDocumentTypeException If no registered parser supports the document type, or if
	 * 			no parses are registered
	 * @throws MultiCauseIOException When all parsers failed and at least one failed with an IOException
	 */
	@Override
	public Map<String, Object> parseDocument(final InputStream in) throws UnsupportedDocumentTypeException, IOException {
		final Map<String, Object> properties;
		
		if (parsers.isEmpty()) {
			throw new UnsupportedDocumentTypeException("No parsers are available");
		} else if (parsers.size() == 1) {
			properties = parseDocumentWithSingleParser(in);
		} else {
			properties = parseDocumentWithMultipleParsers(in);
		}
		
		return properties;
	}
	
	/**
	 * Delegates the parse to the single registered parser
	 */
	private Map<String, Object> parseDocumentWithSingleParser(final InputStream in) throws UnsupportedDocumentTypeException, IOException {
		final DocumentParser parser = parsers.iterator().next();
		return parser.parseDocument(in);
	}
	
	/**
	 * Parses the document when multiple parsers are registered. Each parser is
	 * tried in turn until one succeeds, or all fail.
	 */
	private Map<String, Object> parseDocumentWithMultipleParsers(final InputStream in)
			throws UnsupportedDocumentTypeException, IOException {
		// cache the input stream (multiple reads may be required)
		final ByteArrayInputStream cachedInputStream = cacheInputStream(in);
		
		final List<Exception> suppressedExceptions = Lists.newArrayList();
		for (final DocumentParser parser: parsers) {
			try {
				cachedInputStream.reset();
				return parser.parseDocument(cachedInputStream);
			} catch (final UnsupportedDocumentTypeException e) {
				LOGGER.trace("Parser {} does not support document type", parser, e);
				suppressedExceptions.add(e);				
			} catch (final Exception e) {
				LOGGER.trace("Parser {} failed to parse the document", parser, e);
				suppressedExceptions.add(e);
			}
		}
		
		if (onlyContainsUnsupportedDocumentType(suppressedExceptions)) {
			throw new UnsupportedDocumentTypeException("No parsers support the type of document");
		} else {
			throw new MultiCauseIOException("All parsers failed to parse the document", suppressedExceptions);
		}
	}
	
	/**
	 * Caches the input stream in memory.
	 * <p>
	 * This allows the input stream content to be read multiple times.
	 */
	private ByteArrayInputStream cacheInputStream(final InputStream in) throws IOException {
		if (in instanceof ByteArrayInputStream) {
			return (ByteArrayInputStream)in;
		}
		
		final byte[] bytes = ByteStreams.toByteArray(in);
		return new ByteArrayInputStream(bytes);
	}
	
	/**
	 * Tests if the specified list only contains UnsupportedDocumentTypeExceptions
	 * 
	 * @param suppressedExceptions The list to test
	 * @return true if the list only contains UnsupportedDocumentTypeExceptions, or false otherwise
	 */
	private boolean onlyContainsUnsupportedDocumentType(final List<Exception> suppressedExceptions) {
		for (final Exception exception: suppressedExceptions) {
			if (!(exception instanceof UnsupportedDocumentTypeException)) {
				return false;
			}
		}
		
		return true;
	}
}
