package uk.nhs.ciao.docs.parser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;

/**
 * Utility GUI to run a file through Tika and
 * check the resulting XHTML output
 */
public class TikaOutputViewer extends JFrame {
	private static final long serialVersionUID = 4227898678735264085L;

	private final JFileChooser chooser;
	private final JTextField fileText;
	private final JTextArea content;
	private final DocumentTreeModel model;	
	private final Parser parser;
	private final SAXContentToDOMHandler handler;
	private final Transformer transformer;
	
	public static void main(final String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		final TikaOutputViewer viewer = new TikaOutputViewer();
		viewer.setVisible(true);
	}
	
	public TikaOutputViewer() throws Exception {
		super("Tika Output Viewer");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		chooser = new JFileChooser();
		chooser.setDialogTitle("Select file");
		
		fileText = new JTextField();
		model = new DocumentTreeModel();
		content = new JTextArea();
		
		parser = TikaParserFactory.createParser();
		handler = new SAXContentToDOMHandler(
				DocumentBuilderFactory.newInstance().newDocumentBuilder(), true);
		
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", 3);
		transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT,"yes");

		initContentPanel();
		
		setSize(800, 600);
		setLocationRelativeTo(null);
	}
	
	private void initContentPanel() throws Exception {		
		final JButton selectButton = new JButton("Select");
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		
		final JButton parseButton = new JButton("Parse");
		parseButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				parseFile();
			}
		});
		
		final JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BorderLayout(5, 5));
		optionPanel.add(new JLabel("File:"), BorderLayout.LINE_START);
		optionPanel.add(fileText);
		optionPanel.add(selectButton, BorderLayout.LINE_END);

		final JPanel parsePanel = new JPanel();
		parsePanel.add(parseButton);
		optionPanel.add(parsePanel, BorderLayout.PAGE_END);
		
		final JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(new JTree(model)), new JScrollPane(content));
		horizontalSplit.setResizeWeight(0.25);
		final JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionPanel,
				horizontalSplit);		
		
		setContentPane(verticalSplit);
	}
	
	private void selectFile() {
		final String filePath = fileText.getText();
		if (filePath != null && !filePath.isEmpty()) {
			final File file = new File(filePath);
			if (file.isDirectory()) {
				chooser.setCurrentDirectory(file);
			} else if (file.getParentFile().isDirectory()) {
				chooser.setCurrentDirectory(file.getParentFile());
			}
		}
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			fileText.setText(chooser.getSelectedFile().getAbsolutePath());
			parseFile();
		}
	}
	
	private void parseFile() {
		content.setText("");
		final File file = new File(fileText.getText());
		InputStream in = null;
		try {
			in = new FileInputStream(file);					
			final Metadata metadata = new Metadata();
			final ParseContext context = new ParseContext();
			parser.parse(in, handler, metadata, context);
			
			final Document document = handler.getDocument();
			model.setDocument(document);
			
			
			final StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));					
			content.setText(writer.toString());
		} catch (Exception ex) {
			model.setDocument(null);
			throw Throwables.propagate(ex);
		} finally {
			Closeables.closeQuietly(in);
			handler.clear();
		}
	}
	
	/**
	 * TreeModel wrapping a DOM
	 */
	private static class DocumentTreeModel implements TreeModel {
		private TreeNode root = new TreeNode(null);

		private final Set<TreeModelListener> listeners = new CopyOnWriteArraySet<TreeModelListener>();
		
		public void setDocument(final Document document) {
			root = new TreeNode(document == null ? null : document.getDocumentElement());
			
			for (final TreeModelListener listener: listeners) {
				listener.treeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
			}
		}
		
		@Override
		public TreeNode getRoot() {
			return root;
		}

		@Override
		public TreeNode getChild(final Object parent, final int index) {
			return ((TreeNode)parent).getChild(index);
		}

		@Override
		public int getChildCount(final Object parent) {
			return ((TreeNode)parent).getChildCount();
		}

		@Override
		public boolean isLeaf(final Object node) {
			return getChildCount(node) == 0;
		}

		@Override
		public void valueForPathChanged(final TreePath path, final Object newValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getIndexOfChild(final Object parent, final Object child) {
			return ((TreeNode)parent).getIndexOfChild((TreeNode)child);
		}

		@Override
		public void addTreeModelListener(final TreeModelListener l) {
			listeners.add(l);
		}

		@Override
		public void removeTreeModelListener(final TreeModelListener l) {
			listeners.remove(l);
		}
	}
	
	/**
	 * Tree node built from a DOM node
	 */
	private static class TreeNode {
		private final Node node;
		private final List<TreeNode> children;

		public TreeNode(final Node node) {
			this.node = node;
			this.children = new ArrayList<TreeNode>();
			if (node != null) {
				
				final NamedNodeMap map = node.getAttributes();
				if (map != null) {
					for (int index = 0; index < map.getLength(); index++) {
						children.add(new TreeNode(map.item(index)));
					}
				}
				
				final NodeList nodes = node.getChildNodes();
				for (int index = 0; index < nodes.getLength(); index++) {
					children.add(new TreeNode(nodes.item(index)));
				}
			}
		}

		public int getIndexOfChild(final TreeNode child) {
			return children.indexOf(child);
		}

		public int getChildCount() {
			return children.size();
		}

		public TreeNode getChild(final int index) {
			return children.get(index);
		}
		
		@Override
		public String toString() {
			final String value;
			
			if (node == null) {
				value = "Empty";
			} else if (node.getNodeType() == Node.TEXT_NODE) {
				value = node.getTextContent();
			} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				value = "@" + node.getNodeName();
			} else {
				value = node.getNodeName();
			}
			
			return value;
		}
	}
}
