# ciao-docs-parser
*CIP to parse documents such as PDF or DOC for key/value properties*

Introduction
------------

The purpose of this CIP is to process an incoming binary document by parsing it and extracting a set of key/value properties before publishing the parsed document for further processing by other CIPs.

`ciao-docs-parser` is built on top of [Apache Camel][2] and [Spring
Framework][3], and can be run as a stand-alone Java application, or via
[Docker][4].

[2]: <http://camel.apache.org/>

[3]: <http://projects.spring.io/spring-framework/>

[4]: <https://www.docker.com/>

Each application can host multiple [routes][5], where each route follows the
following basic structure:

[5]: <http://camel.apache.org/routes.html>

>   input folder -\> `DocumentParser` -\> output queue (JMS)

The details of the JMS queues and document parsers are specified at runtime
through a combination of [ciao-configuration][6] properties and Spring XML
files.

[6]: <https://github.com/nhs-ciao/ciao-utils>

The following document parsers are provided:

-   [TikaDocumentParser][7] - A parser backed by [Apache Tika](https://tika.apache.org/). Tika is used to interpret the document file format and a configured 'PropertiesExtractor' is used to extract key/value pairs from the text.

[7]: <./ciao-docs-parser-core/src/main/java/uk/nhs/ciao/docs/parser/TikaDocumentParser.java>

-   [MultiDocumentEnricher](./ciao-docs-parser-core/src/main/java/uk/nhs/ciao/docs/parser/MultiDocumentParser.java) - A parser which sequentially delegates to multiple configured parsers until one succeeds or all fail to parse the document.

The following properties extractors are provided:

-   [RegexPropertiesExtractor](./ciao-docs-parser-core/src/main/java/uk/nhs/ciao/docs/parser/RegexPropertiesExtractor.java) - Properties are extracted through a series of regular expressions.

- [ciao-docs-parser-kings](./ciao-docs-parser-kings) - Module which provides various parsers for Kings College University Hospital PDF documents.

For more advanced usages, a custom document parser can be integrated by
implementing parser Java interfaces and providing a suitable spring
XML configuration on the classpath.

Configuration
-------------

For further details of how ciao-configuration and Spring XML interact, please
see [ciao-core][8].

[8]: <https://github.com/nhs-ciao/ciao-core>

### Spring XML

On application start-up, a series of Spring Framework XML files are used to
construct the core application objects. The created objects include the main
Camel context, input/output components, routes and any intermediate processors.

The configuration is split into multiple XML files, each covering a separate
area of the application. These files are selectively included at runtime via
CIAO properties, allowing alternative technologies and/or implementations to be
chosen. Each imported XML file can support a different set of CIAO properties.

The Spring XML files are loaded from the classpath under the
[META-INF/spring][9] package.

[9]: <./ciao-docs-parser/src/main/resources/META-INF/spring>

**Core:**

-   `beans.xml` - The main configuration responsible for initialising
    properties, importing additional resources and starting Camel.

**Repositories:**

> An `IdempotentRepository' is configured to enable [multiple consumers](http://camel.apache.org/competing-consumers.html) access the same folder concurrently.

- 'repository/memory.xml' - An in-memory implementation suitable for use when there is only a single consumer, or multiple-consumers are all contained within the same JVM instance.

- 'repository/hazelcast.xml' - A grid-based implementation backed by [Hazelcast](http://camel.apache.org/hazelcast-component.html). The component is hosted entirely within the JVM process and uses a combination of multicast and point-to-point networking to maintain a cross-server data grid.

**Processors:**

-   `processors/default.xml` - Creates individual parsers from the `ciao-docs-parser-kings` module, and initialises an auto-detect parser to try each sequentially until a match is found.

**Messaging:**

-   `messaging/activemq.xml` - Configures ActiveMQ as the JMS implementation for input/output queues.

-   `messaging/activemq-embedded.xml` - Configures an internal embedded ActiveMQ as the JMS implementation for input/output queues. *(For use during
    development/testing)*

### CIAO Properties

At runtime ciao-docs-parser uses the available CIAO properties to determine
which Spring XML files to load, which Camel routes to create, and how individual routes and components should be wired.

**Spring Configuration:**

-   `repositoryConfig` - Selects which repository configuration to load:
    `repositories/${repositoryConfig}.xml`

-   `processorConfig` - Selects which processor configuration to load:
    `processors/${processorConfig}.xml`

-   `messagingConfig` - Selects which messaging configuration to load:
    `messaging/${messagingConfig}.xml`

**Routes:**

-   `documentParserRoutes` - A comma separated list of route names to build

The list of route names serves two purposes. Firstly it determines how many
routes to build, and secondly each name is used as a prefix to specify the
individual properties of that route.

**Route Configuration:**

>   For 'specific' properties unique to a single route, use the prefix:
>   `documentParserRoutes.${routeName}.`
>
>   For 'generic' properties covering all routes, use the prefix:
>   `documentParserRoutes.`

-   `inputFolder` - Selects which folder to consume incoming documents from

- `inProgressFolder` - Selects which folder files should be moved to while they are being processing

- `completedFolder` - Selects which folder files should be moved to after they have processing has completed

- `errorFolder` - Selects which folder files should be moved to if they cannot be processed due to an unrecoverable error (e.g. unsupported file format)

- `idempotentRepositoryId` - The Spring ID of the `IdempotentRepository` used by the route. This enables support for the [Competing Consumers Pattern](http://camel.apache.org/competing-consumers.html).

- `inProgressRepositoryId` - The Spring ID of the in-progress `IdempotentRepository` used by the route. This enables support for the [Competing Consumers Pattern](http://camel.apache.org/competing-consumers.html).

-   `processorId` - The Spring ID of the parser to use when parsing documents

-   `outputQueue` - Selects which queue to publish parsed documents to

**Folder Configuration:**

The `completedFolder` and `errorFolder` route options can include [Camel Simple Language] (https://camel.apache.org/simple.html) expressions. The following additional headers can be referenced:

- `CamelCorrelationId` - A unique ID associated with the processing of the source document

- `ciaoSourceFileName` - The file name of the source document

- `ciaoTimestamp` - The time processing was started expressed as a Unix timestamp (i.e. milliseconds since 1970)

The `inProgressFolder` folder option does not support Simple expressions - instead this option should be specified as a standard file path (absolute or relative to the working directory). While a document is being processed, data relating to the processing will be stored in a sub-folder of inProgressFolder/{correlationId}.

### Example
```INI
# Select which processor config to use (via dynamic spring imports)
processorConfig=default

# Select which idempotent repository config to use (via dynamic spring imports)
repositoryConfig=memory
# repositoryConfig=hazelcast

# Select which messaging config to use (via dynamic spring imports)
messagingConfig=activemq
# messagingConfig=activemq-embedded

# ActiveMQ settings (if messagingConfig=activemq)
activemq.brokerURL=tcp://localhost:61616
activemq.userName=smx
activemq.password=smx

# Setup route names (and how many routes to build)
documentParserRoutes=discharge-notification,ed-discharge,auto-detect

# Setup 'shared' properties across all-routes
documentParserRoutes.outputQueue=parsed-documents
documentParserRoutes.inProgressFolder=./in-progress
documentParserRoutes.idempotentRepositoryId=idempotentRepository
documentParserRoutes.inProgressRepositoryId=inProgressRepository

# Setup per-route properties (can override the shared properties)
documentParserRoutes.discharge-notification.inputFolder=./input/discharge-notifications
documentParserRoutes.discharge-notification.completedFolder=./completed/discharge-notifications/${date:now:yyyy-MM-dd}/${header.CamelCorrelationId}
documentParserRoutes.discharge-notification.errorFolder=./error/discharge-notifications/${date:now:yyyy-MM-dd}/${header.CamelCorrelationId}
documentParserRoutes.discharge-notification.processorId=dischargeNotificationProcessor

documentParserRoutes.ed-discharge.inputFolder=./input/ed-discharges
documentParserRoutes.ed-discharge.completedFolder=./completed/ed-discharges/${date:now:yyyy-MM-dd}/${header.CamelCorrelationId}
documentParserRoutes.ed-discharge.errorFolder=./error/ed-discharges/${date:now:yyyy-MM-dd}/${header.CamelCorrelationId}
documentParserRoutes.ed-discharge.processorId=edDischargeProcessor

documentParserRoutes.auto-detect.inputFolder=./input/auto-detect
documentParserRoutes.auto-detect.completedFolder=./completed/auto-detect/${date:now:yyyy-MM-dd}/${header.CamelCorrelationId}
documentParserRoutes.auto-detect.errorFolder=./error/auto-detect/${date:now:yyyy-MM-dd}/${header.CamelCorrelationId}
documentParserRoutes.auto-detect.processorId=autoDetectProcessor
```

Building and Running
--------------------

To pull down the code, run:

	git clone https://github.com/nhs-ciao/ciao-docs-parser.git
	
You can then compile the module via:

    cd ciao-docs-parser-parent
	mvn clean install -P bin-archive

This will compile a number of related modules - the main CIP module is `ciao-docs-parser`, and the full binary archive (with dependencies) can be found at `ciao-docs-parser\target\ciao-docs-parser-{version}-bin.zip`. To run the CIP, unpack this zip to a directory of your choosing and follow the instructions in the README.txt.

The CIP requires access to various file system directories and network ports (dependent on the selected configuration):

**etcd**:
 -  Connects to: `localhost:2379`

**ActiveMQ**:
 -  Connects to: `localhost:61616`

**Hazelcast**:
 -  Multicast discovery: `224.2.2.3:54327`
 -  Listens on: `*:5701` (If port is already taken, the port number is incremented until a free port is found)

**Filesystem**:
 -  If etcd is not available, CIAO properties will be loaded from: `~/.ciao/`
 -  The default configuration creates/uses `input`, `completed`, and `error` directories in the CIP working directory. These can be altered by changing the CIAO properties configuration (via etcd, or the properties file in `~/.ciao/`)
