package htmltree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class HtmlTree extends JPanel {

    static Document document;
    static final int windowHeight = 700;
    static final int leftWidth = 200;
    static final int rightWidth = 1000;
    static final int windowWidth = leftWidth + rightWidth;
    static int paneWidth = 1800;
    static int paneHeight = 2000;
    static JTree tree;
    static JScrollPane htmlView;
    static HtmlTreePanel htmlPane;
    static JFrame frame;

    static final String[] typeName
            = {"none", "Element", "Attr", "Text", "CDATA", "EntityRef", "Entity",
                "ProcInstr", "Comment", "Document", "DocType", "DocFragment", "Notation",};

    public HtmlTree() {

        // Make a nice border
        EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb, bb);
        this.setBorder(new CompoundBorder(cb, eb));

        // Set up the tree
        tree = new JTree(new DomToTreeModelAdapter());
        tree.setEditable(true);
        
        // Iterate over the tree and make nodes visible
        // (Otherwise, the tree shows up fully collapsed)
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(leftWidth, windowHeight));
        
        // Build right-side view
        htmlPane = new HtmlTreePanel();
        TextField tf = new TextField("Courier New");
        
        htmlPane.setPreferredSize(new Dimension(paneWidth, paneHeight));
        Font f = new Font("Courier New", Font.PLAIN, 15);
        htmlPane.setFont(f);
        htmlView = new JScrollPane(htmlPane);
        htmlView.setPreferredSize(new Dimension(rightWidth, windowHeight));

        // Build split-pane view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView, htmlView);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(leftWidth);
        splitPane.setPreferredSize(new Dimension(windowWidth + 10, windowHeight + 10));
        
        // Add GUI components
        this.setLayout(new BorderLayout());
        this.add("Center", splitPane);
        
        //Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton deleteNodeButton = new JButton("Delete child nodes");
        JButton addNodeButton = new JButton("Add leaf");
        JButton saveButton = new JButton("Save File");
        JButton loadButton = new JButton("Load File");
        
        //Delete node button 
        ActionListener deleteNodeEvent = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Deletes a node and its children.
                System.out.println("Delete node");
                
                TreePath[] paths = tree.getSelectionPaths();
                for (TreePath path : paths) {
                    AdapterNode node = (AdapterNode) path.getLastPathComponent();
                    if (node != null) {
                        node.removeAllChildren();
                    }
                }
                updateFrame();
                
            }
        };
        deleteNodeButton.addActionListener(deleteNodeEvent);

        //Add node button
        ActionListener addNodeEvent = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Todo - Whatever you want to happen for adding a node
                System.out.println("Add node");
                
                String newNodeName = (String)JOptionPane.showInputDialog(
                    frame,
                    "Enter name of new node:\n"
                    + "(examples: div, span, ul, li, html, body)",
                    "Add Node",
                    JOptionPane.PLAIN_MESSAGE, null, null, "div");
                
                TreePath[] paths = tree.getSelectionPaths();
                for (TreePath path : paths) {
                    AdapterNode node = (AdapterNode) path.getLastPathComponent();
                    
                    if (node != null) {
                        AdapterNode parent = node.parent();
                        if (parent == null) System.out.println("Parent is null.");
                        else {
                            int index = parent.index(node);
                            System.out.println("Adding to index " + String.valueOf(index));
                            System.out.println("document has " + String.valueOf(document.getChildNodes().getLength())+" children");
                            node.domNode.appendChild(document.createElement(newNodeName));
                            //problem is it's adding it to the document's index. it should be adding it to the parent node.
                            //document.getChildNodes().item(index).appendChild(document.createElement(newNodeName));
                        }
                    }
                }
                updateFrame();
            }
        };
        addNodeButton.addActionListener(addNodeEvent);
        
        ActionListener saveFileEvent = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                htmlPane.saveInfo();
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        };
        saveButton.addActionListener(saveFileEvent);
        
        ActionListener loadFileEvent = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(new File("outputfile.html"));
                
                } catch (SAXParseException spe) {
                    // Error generated by the parser
                    System.out.println("\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
                    System.out.println("   " + spe.getMessage());
                    Exception x = spe;

                    if (spe.getException() != null) {
                        x = spe.getException();
                    }

                    x.printStackTrace();
                } catch (SAXException sxe) {
                    Exception x = sxe;
                    if (sxe.getException() != null) {
                        x = sxe.getException();
                    }

                    x.printStackTrace();
                } catch (ParserConfigurationException pce) {
                    // Parser with specified options can't be built
                    pce.printStackTrace();
                } catch (IOException ioe) {
                    // I/O error
                    ioe.printStackTrace();
                }
                updateFrame();
            }
        };
        loadButton.addActionListener(loadFileEvent);
        
        //Add buttons to panel
        buttonPanel.add(deleteNodeButton);
        buttonPanel.add(addNodeButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        
    } // constructor

    public static void main(String[] argv) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        //Speed up parsing!
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

        // Ask the user which URL they want to fetch  
       
        String theirInputOfWhichUrlToParse = (String)JOptionPane.showInputDialog(
            frame,
            "Enter a URL to fetch:\n"
            + "(must contain strictly well-formed XML)\n"
            + "(examples: http://basement.com, http://w3.org)",
            "Choose wisely",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            "http://basement.com");
        
        
            //After searching for about a half hour, these are the only two websites I could find that work.
            //The website needs to be 100% valid strict XML, 0 mistakes, 0 warnings. Very few sites out there are.
            // http://basement.com
            // http://w3.org
            
        
            String documentString = buildDocumentString(theirInputOfWhichUrlToParse);
            
            
            //________________
            // String cleanup:
            
            // this fixes the error of an erroneous <html></html> 
            // that can appear at the beginning of the document 
            // just ignore the <!DOCTYPE
            for (int i = 0; i < documentString.length() - 10; i++)
            {
                if (documentString.substring(i,i+5) == "<!DOC");
                {
                    int j;
                    for (j = i; j < documentString.length(); j++)
                        if (documentString.charAt(j) == '>')
                            break;
                    if (j != documentString.length())
                        documentString = documentString.substring(0, i) + documentString.substring(j+1, documentString.length()); 
                    break;
                }
                
            }
            
            // end string cleanup
            
            
            

            InputStream stream = new ByteArrayInputStream(documentString.getBytes("UTF-8"));
            
            //document = builder.parse(new File("fun.html")); // test debug file 
            //document = builder.parse(new File("outputfile.html")); // load saved file 
            document = builder.parse(stream);
            
            makeFrame();

        } catch (SAXParseException spe) {
            // Error generated by the parser
            JOptionPane.showMessageDialog(frame, "Error: XML is not well formed.");
            System.out.println("\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("   " + spe.getMessage());
            Exception x = spe;

            if (spe.getException() != null) {
                x = spe.getException();
            }

            x.printStackTrace();
        } catch (SAXException sxe) {
            Exception x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }

            x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
    } // main

    public static String buildDocumentString(String link) throws Exception {
        String content = "";
        URL url = new URL(link);
        URLConnection c = url.openConnection();

        c.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)");
        BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
        String strLine = "";
        while ((strLine = in.readLine()) != null) {
            content += strLine;
        }
        return content;
    }

    public static void updateFrame() {
         SwingUtilities.updateComponentTreeUI(frame);
    }

    public static void makeFrame() { // Set up a GUI framework
        
        //pullThePlug(); //Removes old JFrame if one exists
        frame = new JFrame("DOM Echo");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        // Set up the tree, the views, and display it all
        final HtmlTree echoPanel = new HtmlTree();
        frame.getContentPane().add("Center", echoPanel);
        frame.pack();
       

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = windowWidth + 10;
        int h = windowHeight + 10;
        frame.setLocation((screenSize.width / 3) - (w / 2), (screenSize.height / 2) - (h / 2));
        frame.setSize(w, h);
        
        htmlView.getHorizontalScrollBar().setValue(htmlView.getHorizontalScrollBar().getMaximum() / 4);
        
        frame.setVisible(true);
    }
  

    public static void pullThePlug() {
        if (frame != null) frame.setVisible(false);
    }
    
    // This class wraps a DOM node and returns the text we want to display in the tree. 
    public class AdapterNode extends DefaultMutableTreeNode {

        org.w3c.dom.Node domNode;

        public AdapterNode(org.w3c.dom.Node node) {
            domNode = node;
        }
        
        public org.w3c.dom.Node getDomNode() {return domNode;}

        // Return a string that identifies this node in the tree
        @Override
        public String toString() {
            String s = typeName[domNode.getNodeType()];
            String nodeName = domNode.getNodeName();

            if (!nodeName.startsWith("#"))
                s = (nodeName);

            if (domNode.getNodeValue() != null) {
                if (s.startsWith("ProcInstr")) {
                    s += ", ";
                } else {
                    s += ": ";
                }

                // Trim the value to get rid of NL's at the front
                String t = domNode.getNodeValue().trim();
                int x = t.indexOf("\n");

                if (x >= 0) 
                    t = t.substring(0, x);
                
                s += t;
            }
            return s;
        }
        
        
        @Override
        public void removeAllChildren() {       
            for (int i = childCount()-1; i >= 0; i--)
                remove(i);
        }
        
        @Override
        public void remove(int childIndex) {
            AdapterNode child = child(childIndex);
            child.parent().domNode.removeChild(child.domNode);
            
            //parent.remove(child);
            //domNode.getChildNodes().item(childIndex)
            //children.removeElementAt(childIndex);
            child.setParent(null);        
        }
        
        @Override
        public void removeFromParent() {
            AdapterNode parentNode = this.parent();
            if (parentNode != null)
                parentNode.remove(index(this));
            
        }
        
        
        public int index(AdapterNode child) {
            int count = 0;
            try {
            count = childCount();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "WARNING: Cannot add more than one node to Document.");
            }
            for (int i = 0; i < count; i++) {
                AdapterNode n = this.child(i);

                if (child.domNode == n.domNode)
                    return i;
            
            }
            return -1; // Should never get here.
        }

        public AdapterNode child(int searchIndex) {
            //Note: JTree index is zero-based. 
            org.w3c.dom.Node node = domNode.getChildNodes().item(searchIndex);

            return new AdapterNode(node);
        }

        public int childCount() {
            return domNode.getChildNodes().getLength();
        }
        
        public AdapterNode parent() {
            org.w3c.dom.Node node = domNode.getParentNode();
            return new AdapterNode(node);
        }
    }

    // This adapter converts the current Document (a DOM) into a JTree model. 
    public class DomToTreeModelAdapter implements javax.swing.tree.TreeModel {
        
        private Vector listenerList = new Vector();

        public Object getRoot() {
            return new AdapterNode(document);
        }

        public boolean isLeaf(Object aNode) {
            // Determines whether the littl lead icon thing shows up to the left.
            // Return true for any node with no children
            AdapterNode node = (AdapterNode) aNode;

            if (node.childCount() > 0)
                return false;
            

            return true;
        }

        public int getChildCount(Object parent) {
            AdapterNode node = (AdapterNode) parent;
            return node.childCount();
        }

        public Object getChild(Object parent, int index) {
            AdapterNode node = (AdapterNode) parent;
            return node.child(index);
        }

        public int getIndexOfChild(Object parent, Object child) {
            AdapterNode node = (AdapterNode) parent;
            return node.index((AdapterNode) child);
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
            // We want to ensure the new value is really new,
            // adjust the model, and then fire a TreeNodesChanged event.
            System.out.println("value for path changed");
            AdapterNode node = (AdapterNode) path.getLastPathComponent();
            
            node.getDomNode().setNodeValue((String)newValue);
            fireTreeNodesChanged(new TreeModelEvent(this, path));
            
        }

        public void addTreeModelListener(TreeModelListener listener) {
            if ((listener != null) && !listenerList.contains(listener)) {
                listenerList.addElement(listener);
            }
        }

        public void removeTreeModelListener(TreeModelListener listener) {
            if (listener != null) {
                listenerList.removeElement(listener);
            }
        }
        
        //CHRISTIAN USE THESE FUNCTIONS IF YOU WOULD LIKE
        public void fireTreeNodesChanged(TreeModelEvent e) {
            System.out.println("Fire Tree Nodes Changed");
            Enumeration listeners = listenerList.elements();
            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeNodesChanged(e);
            }
        }

        public void fireTreeNodesInserted(TreeModelEvent e) {
            Enumeration listeners = listenerList.elements();

            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeNodesInserted(e);
            }
        }

        public void fireTreeNodesRemoved(TreeModelEvent e) {
            Enumeration listeners = listenerList.elements();
            System.out.println("Fire Tree Nodes Removed");
            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeNodesRemoved(e);
            }
        }

        public void fireTreeStructureChanged(TreeModelEvent e) {
            Enumeration listeners = listenerList.elements();
            System.out.println("Fire Tree Structure Changed");
            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeStructureChanged(e);
            }
        }
    }

    public class HtmlTreePanel extends JPanel {

        int nodeHeight = 20;
        int nodeWidth = 120;
        int nodeHorizSpacing = 130;
        int nodeVertSpacing = 120;
        int rootX = 1450;
        int rootY = 10;
        int[] rowCount = new int[100];
        int level = -1;
        int[] rowIndex = new int[100];
        boolean panelResizedYet = false;


        public HtmlTreePanel() {
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            //System.out.println("Paint");
           
            

            AdapterNode theNode = null;
            AdapterNode theRoot = (AdapterNode) tree.getModel().getRoot();
            
           //int dismal = ((AdapterNode)(tree.getModel().getRoot())).childCount();
           //System.out.println("dismal = " + dismal);
           
            for (int i = 0; i < 100; i++)
                rowIndex[i] = 0;
            
            for (int i = 0; i < 100; i++)
                rowCount[i] = 0;
            
            countAllNodes(theRoot);
            
            resizePanel();
            
            
            //for (int i = 0; rowCount[i] > 0; i++) 
            //    System.out.println("row " + i + " has " + rowCount[i]);
       
            level = -1;
            //System.out.println("Paint all nodes!");
            
            paintAllNodes(g, theRoot);
        }
        
        public void resizePanel()
        {
            int biggestRow = 0;
            for (int i = 0; i < 100; i++)
                if (rowCount[i] > biggestRow)
                    biggestRow = rowCount[i];
            
            if (biggestRow > 15)
                paneWidth = biggestRow * nodeHorizSpacing + 200;
            
            int deepestRow = 0;
            for (int i = 0; i < 100 && rowCount[i] != 0; i++)
                deepestRow = i;
            
            if (deepestRow > 10)
                paneHeight = deepestRow * nodeVertSpacing + 100;
            
            rootX = paneWidth / 2;
            
            htmlPane.setPreferredSize(new Dimension(paneWidth, paneHeight));
            
            if (!panelResizedYet)
                //htmlView.getHorizontalScrollBar().setValue((int) (htmlView.getHorizontalScrollBar().getMaximum() * .3));
                htmlView.getHorizontalScrollBar().setValue((int) (paneWidth * .45));
            panelResizedYet = true;
        }
        
        public void countAllNodes(AdapterNode root) {
            if (root.toString().equals("Text: ")) return;
            level++;
            
            for (int i = 0; i < root.childCount(); i++) 
                countAllNodes(root.child(i));
            
            rowCount[level]++;
            level--;
        }
        
        
       public void paintAllNodes(Graphics g, AdapterNode parent) {
           //If this next line is missing, the tree will contain a bunch of blank Text: nodes (The ones that are displayed
           //in the left pane. For some reason, the parser likes to add blank Text nodes every time there is an open/close tag
           //with no text between the two tags.
           
           if (parent.toString().equals("Text: ")) return;
            level++;
            for (int i = 0; i < parent.childCount(); i++) 
                paintAllNodes(g, parent.child(i));
            
            if (level > 0)
             paintNode(g, rootX - (rowCount[level] / 2 - rowIndex[level]) * nodeHorizSpacing, rootY + level * nodeVertSpacing, parent.toString(),
                            rootX - (rowCount[level - 1] / 2 - rowIndex[level - 1]) * nodeHorizSpacing, rootY + (level - 1) * nodeVertSpacing);
            else
             paintNode(g, rootX - (rowCount[level] / 2 - rowIndex[level]) * nodeHorizSpacing, rootY + level * nodeVertSpacing, parent.toString(),
                            rootX - (rowCount[level] / 2 - rowIndex[level]) * nodeHorizSpacing, rootY + (level - 1) * nodeVertSpacing - nodeHeight);
                
            rowIndex[level]++;
            level--;     
        }

        public void paintNode(Graphics g, int x, int y, String text, int parentX, int parentY) {
            //System.out.println("Paint node!");
            int modifiedWidth = 0;
            if (text.length() > 15) 
                modifiedWidth = nodeWidth + 12;
            
            else modifiedWidth = nodeWidth;
            int[] xCoords = {0, modifiedWidth * 1 / 8, modifiedWidth * 7 / 8, modifiedWidth, modifiedWidth * 7 / 8, modifiedWidth * 1 / 8};
            int[] yCoords = {nodeHeight * 1 / 2, 0, 0, nodeHeight * 1 / 2, nodeHeight, nodeHeight};
            
            int numPoints = 6;

            for (int i = 0; i < numPoints; i++) {
                xCoords[i] += x - modifiedWidth * 1 / 8;
                yCoords[i] += y;
            }

            g.setColor(randColor(text));
            g.fillPolygon(xCoords, yCoords, numPoints);

            xCoords[0] += 2;
            xCoords[1] += 1;
            xCoords[2] -= 1;
            xCoords[3] -= 2;
            xCoords[4] -= 1;
            xCoords[5] += 1;
            yCoords[1] += 2;
            yCoords[2] += 2;
            yCoords[4] -= 2;
            yCoords[5] -= 2;

            g.setColor(randColor(text).brighter());
            g.fillPolygon(xCoords, yCoords, numPoints);

            g.setColor(Color.WHITE);
            g.drawString(text, x + 5, y + 14);

            g.setColor(Color.BLACK);
            g.drawLine(x + modifiedWidth * 3 / 8, y, parentX + modifiedWidth * 3 / 8, parentY + nodeHeight);
        }

        public Color randColor(String text) { // generates a random color given a word as a seed 
            int r = 0;
            int g = 0;
            int b = 0;

            for (int i = 0; i < text.length(); i++) {
                r += (int) text.charAt(0);
                g += (int) text.charAt(i);
                b += .8 * (int) text.charAt(i);
            }

            r %= 100;
            r += 50;
            g %= 100;
            g += 50;
            b %= 100;
            b += 50;

            Color x = new Color(r, g, b);
            return x;
        }
        public void saveInfo() throws IOException
        {
            level = -1;
            for (int i = 0; i < 100; i++)
                rowIndex[i] = 0;
            
            for (int i = 0; i < 100; i++)
                rowCount[i] = 0;
            
            
              
            AdapterNode theRoot = (AdapterNode) tree.getModel().getRoot();
            
            countAllNodes(theRoot);
              
            PrintWriter out = new PrintWriter(new FileWriter("outputfile.html"));
            //out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.print(outputAllNodes(theRoot, ""));
            level = -1;
            
            JOptionPane.showMessageDialog(frame, "Tree has been saved.");

            out.close();

        }
        
        public String outputAllNodes(AdapterNode root, String currentStr)
        {
           
            if (root.toString().equals("Text: ")) return " ";
            level++;
            
            for (int i = 0; i < root.childCount(); i++) 
                currentStr += outputAllNodes(root.child(i), "");
            
            rowCount[level]++;
            level--;
            
            if (root.toString().equals("Document"))
                return currentStr;
            
            if (root.toString().startsWith("Text: "))
                currentStr += root.toString().substring(6);
            else 
                currentStr = "\n" + "<" + root.toString() + ">" + currentStr + "</" + root.toString() + ">" + "\n";
            
               
            return currentStr;
        }

    }

}
