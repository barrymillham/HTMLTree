
package htmltree;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Enumeration;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;


public class HtmlTreePanel extends JPanel{
    
     int nodeHeight = 20;
     int nodeWidth = 70;
     int nodeHorizSpacing = 80;
     int nodeVertSpacing = 120;
     int rootX = 1500;
     int rootY = 10;
     
     JTree theTree;
     TreeModel theTreeModel;
     
     public void setTree(JTree t) {
         theTree = t;
         theTreeModel = t.getModel();
     }
     
     public HtmlTreePanel(JTree t) {
         theTree = t;
         theTreeModel = t.getModel();
     }
     
     public HtmlTreePanel() {
         theTree = new JTree();
         theTreeModel = theTree.getModel();
     }
    
     public void paint(Graphics g) {
        super.paint(g);
        
        int[] rowCount = new int[100];
        for (int i = 0; i < 100; i++) rowCount[i] = 0;
        
        DefaultMutableTreeNode theNode = null;
        DefaultMutableTreeNode theRoot = (DefaultMutableTreeNode)theTreeModel.getRoot();
        for (Enumeration e = theRoot.depthFirstEnumeration(); e.hasMoreElements() && theNode == null;) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            rowCount[node.getLevel()]++;
        }
        
        //for (int i = 0; rowCount[i] != 0; i++)
        //    System.out.println("row " + i + " has " + rowCount[i]);
        
        
        int[] rowIndex = new int[100];
        for (int i = 0; i < 100; i++) rowIndex[i] = 0;
       
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)theTreeModel.getRoot();
        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {

          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
          DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
       
          if (parentNode == null)
            paintNode(g, rootX - (rowCount[node.getLevel()]/2 - rowIndex[node.getLevel()]) * nodeHorizSpacing, rootY + node.getLevel() * nodeVertSpacing, node.toString(), 
                         rootX - (rowCount[node.getLevel()]/2 - rowIndex[node.getLevel()]) * nodeHorizSpacing, rootY + node.getLevel() * nodeVertSpacing - nodeHeight);
          else
            paintNode(g, rootX - (rowCount[node.getLevel()]/2 - rowIndex[node.getLevel()]) * nodeHorizSpacing, rootY + node.getLevel() * nodeVertSpacing, node.toString(),
                         rootX - (rowCount[parentNode.getLevel()]/2 - rowIndex[parentNode.getLevel()]) * nodeHorizSpacing, rootY + parentNode.getLevel() * nodeVertSpacing);
          
         //System.out.println("drawing line from " + (rowCount[node.getLevel()]/2 - rowIndex[node.getLevel()] * nodeHorizSpacing) + ", " + (rootY + node.getLevel() * nodeVertSpacing) + " to " + (rootX - (rowCount[parentNode.getLevel()]/2 - rowIndex[parentNode.getLevel()]) * nodeHorizSpacing) + ", " + rootY + parentNode.getLevel() * nodeVertSpacing);
          
          rowIndex[node.getLevel()]++;
        }  
        
     }
     
     public void paintNode(Graphics g, int x, int y, String text, int parentX, int parentY) {
         int[] xCoords = {0, nodeWidth * 1/8, nodeWidth * 7/8, nodeWidth, nodeWidth * 7/8, nodeWidth * 1/8};
         int[] yCoords = {nodeHeight * 1/2, 0, 0, nodeHeight * 1/2, nodeHeight, nodeHeight};
         int numPoints = 6;
         
         for (int i = 0; i < numPoints; i++)
         {
             xCoords[i] += x - nodeWidth * 1/8;
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
         g.drawLine(x + nodeWidth * 3/8, y, parentX + nodeWidth * 3/8, parentY + nodeHeight);
     }
     
  
     
     public Color randColor(String text) { // generates a random color given a word as a seed 
         int r = 0;
         int g = 0;
         int b = 0;
         
         
         for (int i = 0; i < text.length(); i++)
         {
             r += (int)text.charAt(0);
             g += (int)text.charAt(i);
             b += .8 * (int)text.charAt(i);
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
    
}
