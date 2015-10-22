# Parsed Document

The `ciao-docs-parser-model` provides a model ([ParsedDocument](../ciao-docs-parser-model/src/main/java/uk/nhs/ciao/docs/parser/ParsedDocument.java)) for representing a binary document and set of associated properties extracted from it.

The model is used internally and as a JSON transfer representation by multiple CIPs involved in a *document upload process*, including `ciao-docs-parser`, `ciao-docs-enricher`, `ciao-cda-builder` and `ciao-transport-itk`.

## Java Model

The model is defined in the following class structure:

**ParsedDocument:**
-	**originalDocument**: Document
-	**properties**: Map

**Document:**
-	**name**: String
-	**content**: byte array
-	**mediaType**: String

The properties Map may be flat or hierarchical. If included, the `metadata` property should form a (flat) Map of key, value pairs.

## JSON

> CIAO uses [Jackson](https://github.com/FasterXML/jackson) to perform the serialization between Java and JSON. 

The JSON representation of `ParsedDocument` follows the outlined structure. All keys in the structure should be strings and the values should be encoded following the standard JSON mappings.

**`originalDocument.content` property is encoded in JSON as a Base64 string.**

### Example
```javascript
{
  "originalDocument": {
    "name": "hello.txt",
    "content": "SGVsbG8gV29ybGQh",
    "mediaType": "text/plain"
  },
  "properties": {
    "metadata": {
      "some-key": "some-value"
    },
    "extracted-property": "extracted value"
  }
}
```
