package uk.nhs.ciao.docs.parser;

import java.util.Map;

public interface PropertiesExtractor<T> {
	Map<String, Object> extractProperties(T document) throws UnsupportedDocumentTypeException;
}
