/**
 * 
 */

package example;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.JMenuItem;
import java.io.*;
import javax.swing.tree.*;

import com.pff.*;

import java.util.*;

/**
 * @author toweruser
 *
 */
public class TestGui implements ActionListener {
	private PSTFile pstFile;
	private EmailTableModel emailTableModel;
	private JTextPane emailText;
	private JPanel emailPanel;
	private JPanel attachPanel;
	private JLabel attachLabel;
	private JTextField attachText;
	private PSTMessage selectedMessage;
	private JFrame f;
	
	public TestGui() throws PSTException, IOException {

		// setup the basic window
        f = new JFrame("PST Browser");
		
		// attempt to open the pst file
		try {
			/*
			JFileChooser chooser = new JFileChooser();
			if (chooser.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) {
			} else {
				System.exit(0);
			}

			String filename = chooser.getSelectedFile().getCanonicalPath();
			 */
			String filename = "Outlook-new.pst";
			filename = "G:\\From old Tower\\pff\\java\\Old Email.pst";
			//filename = "RichardJohnson@sumac.uk.com - exchange.ost";
			//String filename = "Outlook 32bit.pst";
			//String russian = "Узеи́р Абду́л-Гусе́йн оглы́ Гаджибе́ков (азерб. Üzeyir bəy Əbdülhüseyn oğlu Hacıbəyov; 18 сентября 1885, Агджабеди, Шушинский уезд, Елизаветпольская губерния, Российская империя — 23 ноября 1948, Баку, Азербайджанская ССР, СССР) — азербайджанский композитор, дирижёр, публицист, фельетонист, драматург и педагог, народный артист СССР (1938), дважды лауреат Сталинских премий (1941, 1946). Действительный член АН Азербайджана (1945), профессор (1940), ректор Азербайджанской государственной ";

			//System.out.println(java.nio.charset.Charset.availableCharsets());

			//byte[] russianBytes = russian.getBytes("UTF-8");
			//PSTObject.printHexFormatted(russianBytes, true);

			//String filename = "Outlook 32bit.pst";
			//filename = "RichardJohnson@sumac.uk.com - exchange.ost";
			pstFile = new PSTFile(filename);
			//pstFile = new PSTFile("RichardJohnson@sumac.uk.com - exchange.ost");


			//PSTFolder folder = (PSTFolder)PSTObject.detectAndLoadPSTObject(pstFile, 32898);
			//System.out.println(folder.getEmailCount());
			//System.exit(0);


			//"г ь ы";
			//System.out.println(java.nio.charset.Charset.availableCharsets().keySet());
			//PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, 2097604);
			//System.out.println(msg);

			//PSTObject.printHexFormatted("г ь ы".getBytes("koi8-r"), true);
			//System.exit(0);
			//PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, 2097668);
			//System.out.println(msg.getRTFBody());
			//System.exit(0);

			//PSTAppointment msg = (PSTAppointment)PSTObject.detectAndLoadPSTObject(pstFile, 2097252);
			////System.out.println(msg.getStartTime());
			//System.exit(0);

			//int[] emails = {
				//2098180
				/*
				2097348,
				2097380,
				2097412,
				2097444,
				2097476,
				2097508,
				2097540,
				2097572
				 *
				 */
			//};

			/*

			RandomAccessFile tmpIn = new RandomAccessFile("test - httpdocs.tar.gz", "r");
			PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, emails[0]);
			PSTAttachmentInputStream attachmentStream = msg.getAttachment(0).getFileInputStream();

			byte[] tmp = new byte[1024];
			byte[] tmp2 = new byte[1024];

			for (int y = 1; y < 2000; y++) {
				tmpIn.seek(760*y-50);
				tmpIn.read(tmp);

				attachmentStream.seek(760*y-50);
				attachmentStream.read(tmp2);

				for (int x = 0; x< tmp2.length; x++) {
					if (tmp[x] != tmp2[x]) {


						PSTObject.printHexFormatted(tmp, true);
						PSTObject.printHexFormatted(tmp2, true);

						System.out.println(y);
						System.out.println("Error");
						System.exit(0);
					}
				}
				System.out.println("Worked");
			}
			 *
			 */

			/*
			for (int x = 0; x < emails.length; x++) {
				PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, emails[x]);
				PSTAttachment attach = msg.getAttachment(0);
				//System.out.println(attach);
				InputStream attachmentStream = msg.getAttachment(0).getFileInputStream();
				FileOutputStream out = new FileOutputStream("test - "+attach.getLongFilename());

				int bufferSize = 8176;

				byte[] buffer = new byte[bufferSize];
				int count = attachmentStream.read(buffer, 0, 8176);
				while (count == bufferSize) {
					out.write(buffer);
					count = attachmentStream.read(buffer, 0, 8176);
				}
				byte[] endBuffer = new byte[count];
				System.arraycopy(buffer, 0, endBuffer, 0, count);
				out.write(endBuffer);
				out.close();
			}
			 *
			 */
			//System.exit(0);

		} catch (Exception err) {
			err.printStackTrace();
			System.exit(1);
		}
		
        
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
        			
        			return folderValue.getDescriptorNodeId()+" - "+folderValue.getDisplayName()+" "+folderValue.getAssociateContentCount()+"";
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
					try {
	        		selectFolder(folderValue);
					} catch(Exception err) {
						System.out.println("unable to change folder");
						err.printStackTrace();
					}
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
					selectedMessage = emailTableModel.getMessageAtRow(source.getSelectedRow());
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
						emailText.setText(selectedMessage.getBody());
						//System.out.println(selectedMessage);
						//emailText.setText(selectedMessage.toString());
						//emailText.setText(selectedMessage.toString());
//						PSTTask task = selectedMessage.toTask();
//						emailText.setText(task.toString());
					}
					setAttachmentText();
					
//					treePane.getViewport().setViewPosition(new Point(0,0));
					emailText.setCaretPosition(0);
				}
        	});
        } catch (Exception err) {
        	err.printStackTrace();
        }
        
        
		f.setJMenuBar(createMenu());

        // the email
        emailText = new JTextPane();
        emailText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        //emailText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));
        
		emailPanel = new JPanel(new BorderLayout());
		attachPanel = new JPanel(new BorderLayout());
		attachLabel = new JLabel("Attachments:");
		attachText = new JTextField("");
		attachText.setEditable(false);
		attachPanel.add(attachLabel, BorderLayout.WEST);
		attachPanel.add(attachText, BorderLayout.CENTER);
		emailPanel.add(attachPanel, BorderLayout.NORTH);
		emailPanel.add(emailText, BorderLayout.CENTER);

        JSplitPane emailSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailTablePanel, new JScrollPane(emailPanel));
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

	void setAttachmentText() {
		StringBuffer s = new StringBuffer();

		try {
			if (selectedMessage != null) {
				int numAttach = selectedMessage.getNumberOfAttachments();
				for (int x = 0; x < numAttach; x++) {
					PSTAttachment attach = selectedMessage.getAttachment(x);
					String filename = attach.getLongFilename();
					if (filename.isEmpty()) {
						filename = attach.getFilename();
					}
					if (!filename.isEmpty()) {
						if (x != 0) {
							s.append(", ");
						}
						s.append(filename);
					}
				}
			}
		} catch (Exception e) {
		}

		attachText.setText(s.toString());
	}

	void selectFolder(PSTFolder folder)
			throws IOException, PSTException
	{
		// load up the non-folder children.
		
		emailTableModel.setFolder(folder);
		
	}

	public JMenuBar createMenu() {
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Save Attachments", KeyEvent.VK_S);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		return menuBar;
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		if (source.getText() == "Save Attachments")
		{
			saveAttachments();
		}
	}

	private void saveAttachments() {
		if (selectedMessage != null) {
			int numAttach = selectedMessage.getNumberOfAttachments();
			if (numAttach == 0) {
				JOptionPane.showMessageDialog(f, "Email has no attachments");
				return;
			}
			try {
				for (int x = 0; x < numAttach; x++) {
					PSTAttachment attach = selectedMessage.getAttachment(x);
					InputStream attachmentStream = attach.getFileInputStream();
					String filename = attach.getLongFilename();
					if (filename.isEmpty()) {
						filename = attach.getFilename();
					}
					JFileChooser chooser = new JFileChooser();
					chooser.setSelectedFile(new File(filename));
					int r = chooser.showSaveDialog(f);
					if (r == JFileChooser.APPROVE_OPTION) {
						FileOutputStream out = new FileOutputStream(chooser.getSelectedFile());
						// 8176 is the block size used internally and should give the best performance
						int bufferSize = 8176;
						byte[] buffer = new byte[bufferSize];
						int count;
						do {
							count = attachmentStream.read(buffer);
							out.write(buffer, 0, count);
						} while (count == bufferSize);
						out.close();
					}
					attachmentStream.close();
				}
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(f, "Failed writing to file");
			} catch (PSTException pste) {
				JOptionPane.showMessageDialog(f, "Error in PST file");
			}
		}
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
			//System.out.println("Email count: "+theFolder.getEmailCount());
    		return theFolder.getContentCount();
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

			if (next == null) {
				return null;
			}
    		
			switch (col) {
				case 0:
					return next.getDescriptorNode().descriptorIdentifier+"";
				case 1:
					return next.getSubject();
				case 2:
					return next.getSentRepresentingName() + " <"+ next.getSentRepresentingEmailAddress() +">";
				case 3:
					return next.getReceivedByName() + " <"+next.getReceivedByAddress()+">" + 
						next.getDisplayTo();
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
    
    public void setFolder(PSTFolder theFolder)
			throws PSTException, IOException
	{
    	theFolder.moveChildCursorTo(0);
    	this.theFolder = theFolder;
    	cache = new HashMap();
    	this.fireTableDataChanged();
    }

}
