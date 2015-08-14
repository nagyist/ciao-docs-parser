package uk.nhs.ciao.docs.parser.rule;

public interface NodeSelector {
	NodeStream selectNodes(final NodeStream nodes);
}