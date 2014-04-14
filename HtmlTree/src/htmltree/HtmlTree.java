/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package htmltree;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author MARTJA1
 */
public class HtmlTree extends JFrame{
    
    private             HtmlTreePanel	thePanel;
    private static	JTree		tree;
    private             JScrollPane     miniTreeView;
    private static      JScrollPane     scrollFrame;
    
    public HtmlTree()
    {
            Point DEFAULTBASE = new Point(50,50);
            final int DEFAULTWIDTH = 1200, DEFAULTHEIGHT = 700; 
            setTitle( "HTML Tree" );
            setBackground( Color.gray );
            setBounds(DEFAULTBASE.x,DEFAULTBASE.y,DEFAULTWIDTH,DEFAULTHEIGHT);

            
            // Create a new tree control
            tree = new JTree();
            
            // Create the primary view
            thePanel = new HtmlTreePanel();
            scrollFrame = new JScrollPane(thePanel);
            thePanel.setPreferredSize(new Dimension(3000, 2000));
            getContentPane().add(scrollFrame);
            
            
            // Create the mini tree view
            miniTreeView = new JScrollPane(tree);
            miniTreeView.setPreferredSize(new Dimension(200, 100));
            getContentPane().add(miniTreeView, BorderLayout.WEST);
            
            // Create top level container
            //topPanel = new JPanel();
            //topPanel.setLayout( new BorderLayout() );
            //getContentPane().add( topPanel );

            

            // Add the listbox to a scrolling pane
            //scrollPane = new JScrollPane();
            //scrollPane.getViewport().add( tree );
            //topPanel.add( scrollPane, BorderLayout.CENTER );
            
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
           
    }

    public static void main(String[] args) {
        HtmlTree root	= new HtmlTree();
        try{
            BuildTree("http://nothing.com", root);
        } catch (Exception e) {
            System.out.println("That website doesn't work!");
        }
        
        
        root.setVisible( true );
        scrollFrame.getHorizontalScrollBar().setValue(1000);
        
    }
    
    public static void BuildTree(String link, HtmlTree root) throws Exception {
        URL url = new URL(link);
        URLConnection c = url.openConnection();
        
        c.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)");
        BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
        String strLine = "";
        while ((strLine = in.readLine()) != null) {
                if (!strLine.equals("")) {
                    /* 
                        Test if it has '<'
                            if so, is it "</"?
                                if so
                        
                    
                    */ 
                    
                    
                    
                    
                }
            }
        }
    }