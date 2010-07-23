/**
 * 
 */
import java.awt.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.*;

import com.pff.*;

import java.util.*;

/**
 * @author toweruser
 *
 */
public class TestGui {
	private PSTFile pstFile;
	private EmailTableModel emailTableModel;
	private JTextPane emailText;
	
	public TestGui() throws PSTException, IOException {
		
		// attempt to open the pst file
		try {
			pstFile = new PSTFile("Outlook-new.pst");

		} catch (Exception err) {
			err.printStackTrace();
			System.exit(1);
		}
		
		// setup the basic window
        JFrame f = new JFrame("PST Browser");
        
        // do the tree thing
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(pstFile.getMessageStore()); 
        try {
        	buildTree(top, pstFile.getRootFolder());
        } catch (Exception err) {
        	err.printStackTrace();
        	System.exit(1);
        }
        
        final JTree folderTree = new JTree(top){
        	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        		DefaultMutableTreeNode nodeValue = (DefaultMutableTreeNode)value;
        		if (nodeValue.getUserObject() instanceof PSTFolder) {
        			PSTFolder folderValue = (PSTFolder)nodeValue.getUserObject();
        			
        			return folderValue.getDescriptorNode().descriptorIdentifier+" - "+folderValue.getDisplayName()+" "+folderValue.getAssociateContentCount()+"";
        		} else if (nodeValue.getUserObject() instanceof PSTMessageStore) {
        			PSTMessageStore folderValue = (PSTMessageStore)nodeValue.getUserObject();
        			return folderValue.getDisplayName();
        		} else {
        			return value.toString();
        		}
        	}
        };
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getDefaultClosedIcon());
        folderTree.setCellRenderer(renderer);
        
        // event handler for changing...
        folderTree.addTreeSelectionListener(new TreeSelectionListener() {
        	public void valueChanged(TreeSelectionEvent e) {
        		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                folderTree.getLastSelectedPathComponent();
        		if (node == null) {
        			return;
        		}
        		if (node.getUserObject() instanceof PSTFolder) {
	        		PSTFolder folderValue = (PSTFolder)node.getUserObject();
	        		selectFolder(folderValue);
        		}
        	}
        });
        final JScrollPane treePane = new JScrollPane(folderTree);
        
        // the table
        JScrollPane emailTablePanel = null;
        try {
	        emailTableModel = new EmailTableModel(pstFile.getRootFolder(), pstFile);
	        final JTable emailTable = new JTable(emailTableModel);
	        emailTablePanel = new JScrollPane(emailTable);
        	emailTable.setFillsViewportHeight(true);
        	ListSelectionModel selectionModel = emailTable.getSelectionModel();
        	selectionModel.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					JTable source = emailTable;
					PSTMessage selectedMessage = emailTableModel.getMessageAtRow(source.getSelectedRow());
					if (selectedMessage instanceof PSTContact) {
						PSTContact contact = (PSTContact)selectedMessage;
						emailText.setText(contact.toString());
					} else if (selectedMessage instanceof PSTTask) {
						PSTTask task = (PSTTask)selectedMessage;
						emailText.setText(task.toString());
					} else if (selectedMessage instanceof PSTActivity) {
						PSTActivity journalEntry = (PSTActivity)selectedMessage;
						emailText.setText(journalEntry.toString());
					} else if (selectedMessage instanceof PSTRss) {
						PSTRss rss = (PSTRss)selectedMessage;
						emailText.setText(rss.toString());
					} else if (selectedMessage != null) {
//						System.out.println(selectedMessage.getMessageClass());
//						emailText.setText(selectedMessage.getBody());
						emailText.setText(selectedMessage.getBodyHTML());
						//emailText.setText(selectedMessage.toString());
//						PSTTask task = selectedMessage.toTask();
//						emailText.setText(task.toString());
					}
					
//					treePane.getViewport().setViewPosition(new Point(0,0));
					emailText.setCaretPosition(0);
				}
        	});
        } catch (Exception err) {
        	err.printStackTrace();
        }
        
        
        // the email
        emailText = new JTextPane();
        emailText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JSplitPane emailSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailTablePanel, new JScrollPane(emailText));
        emailSplitPane.setOneTouchExpandable(true);
        emailSplitPane.setDividerLocation(0.25);
        
        
        // add a split pane, 1 for our tree, the other for our emails
        JSplitPane primaryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, emailSplitPane);
        primaryPane.setOneTouchExpandable(true);
        primaryPane.setDividerLocation(0.3);
        f.add(primaryPane);
 
        // Set the default close operation for the window, 
        // or else the program won't exit when clicking close button
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
        // Set the visibility as true, thereby displaying it
        f.setVisible(true);
//        f.setSize(800, 600);
        f.setExtendedState(f.getExtendedState() | f.MAXIMIZED_BOTH);
	}
	
	private void buildTree(DefaultMutableTreeNode top, PSTFolder theFolder) {
		// this is recursive, try and keep up.
		try {
			Vector children = theFolder.getSubFolders();
			Iterator childrenIterator = children.iterator();
			while (childrenIterator.hasNext()) {
				PSTFolder folder = (PSTFolder)childrenIterator.next();

				DefaultMutableTreeNode node = new DefaultMutableTreeNode(folder);
				
				if (folder.getSubFolders().size() > 0) {
					buildTree(node, folder);
				} else {
				}
				top.add(node);
			}
		} catch (Exception err) {
			err.printStackTrace();
			System.exit(1);
		}
	}

	void selectFolder(PSTFolder folder) {
		// load up the non-folder children.
		
		emailTableModel.setFolder(folder);
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws PSTException 
	 */
	public static void main(String[] args) throws PSTException, IOException {
		new TestGui();
	}

}

class EmailTableModel extends AbstractTableModel {
	
	PSTFolder theFolder = null;
	PSTFile theFile = null;
	
	HashMap cache = new HashMap();
	
	public EmailTableModel(PSTFolder theFolder, PSTFile theFile) {
		super();
		
		this.theFolder = theFolder;
		this.theFile = theFile;
	}
	
	String[] columnNames = {
    		"Descriptor ID",	
    		"Subject",
    		"From",
    		"To",
    		"Date",
    		"Has Attachments"
	};
	String[][] rowData = {{"","","","",""}};
	int rowCount = 0;
	public String getColumnName(int col) {
        return columnNames[col].toString();
    }
    public int getColumnCount() { return columnNames.length; }
    
    public int getRowCount() { 
    	try {
    		return theFolder.getEmailCount();
    	} catch (Exception err) {
    		err.printStackTrace();
    		System.exit(0);
    	}
    	return 0;
    }
    
    public PSTMessage getMessageAtRow(int row) {
    	PSTMessage next = null;
		try {
	    	if (cache.containsKey(row)) {
				next = (PSTMessage)cache.get(row);
			} else {
	    		theFolder.moveChildCursorTo(row);
				next = (PSTMessage)theFolder.getNextChild();
	    		cache.put(row, next);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return next;
    }
    
    
    public Object getValueAt(int row, int col) {
    	// get the child at...
    	try {
			PSTMessage next = getMessageAtRow(row);
    		
			switch (col) {
				case 0:
					return next.getDescriptorNode().descriptorIdentifier+"";
				case 1:
					return next.getSubject();
				case 2:
					return next.getSentRepresentingName() + " <"+ next.getSentRepresentingEmailAddress() +">";
				case 3:
					return next.getReceivedByName() + " <"+next.getReceivedByAddress()+">" + 
						next.displayTo();
				case 4:
					return next.getClientSubmitTime();
//					return next.isFlagged();
//					return next.isDraft();
//					PSTTask task = next.toTask();
//					return task.toString();
				case 5:
					return (next.hasAttachments() ? "Yes" : "No");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
    	
        return "";
    }
    public boolean isCellEditable(int row, int col) { return false; }
    
    public void setFolder(PSTFolder theFolder) {
    	theFolder.moveChildCursorTo(0);
    	this.theFolder = theFolder;
    	cache = new HashMap();
    	this.fireTableDataChanged();
    }

}