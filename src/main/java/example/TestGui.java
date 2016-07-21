/**
 *
 */

package example;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.pff.PSTActivity;
import com.pff.PSTAttachment;
import com.pff.PSTContact;
import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTMessageStore;
import com.pff.PSTRss;
import com.pff.PSTTask;

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
        this.f = new JFrame("PST Browser");

        // attempt to open the pst file
        try {
            /*
             * JFileChooser chooser = new JFileChooser();
             * if (chooser.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) {
             * } else {
             * System.exit(0);
             * }
             * 
             * String filename = chooser.getSelectedFile().getCanonicalPath();
             */
            String filename = "Outlook-new.pst";
            filename = "G:\\From old Tower\\pff\\java\\Old Email.pst";
            // filename = "RichardJohnson@sumac.uk.com - exchange.ost";
            // String filename = "Outlook 32bit.pst";
            // String russian = "Узеи́р Абду́л-Гусе́йн оглы́ Гаджибе́ков (азерб.
            // Üzeyir bəy Əbdülhüseyn oğlu Hacıbəyov; 18 сентября 1885,
            // Агджабеди, Шушинский уезд, Елизаветпольская губерния, Российская
            // империя — 23 ноября 1948, Баку, Азербайджанская ССР, СССР) —
            // азербайджанский композитор, дирижёр, публицист, фельетонист,
            // драматург и педагог, народный артист СССР (1938), дважды лауреат
            // Сталинских премий (1941, 1946). Действительный член АН
            // Азербайджана (1945), профессор (1940), ректор Азербайджанской
            // государственной ";

            // System.out.println(java.nio.charset.Charset.availableCharsets());

            // byte[] russianBytes = russian.getBytes("UTF-8");
            // PSTObject.printHexFormatted(russianBytes, true);

            // String filename = "Outlook 32bit.pst";
            // filename = "RichardJohnson@sumac.uk.com - exchange.ost";
            this.pstFile = new PSTFile(filename);
            // pstFile = new PSTFile("RichardJohnson@sumac.uk.com -
            // exchange.ost");

            // PSTFolder folder =
            // (PSTFolder)PSTObject.detectAndLoadPSTObject(pstFile, 32898);
            // System.out.println(folder.getEmailCount());
            // System.exit(0);

            // "г ь ы";
            // System.out.println(java.nio.charset.Charset.availableCharsets().keySet());
            // PSTMessage msg =
            // (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, 2097604);
            // System.out.println(msg);

            // PSTObject.printHexFormatted("г ь ы".getBytes("koi8-r"), true);
            // System.exit(0);
            // PSTMessage msg =
            // (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, 2097668);
            // System.out.println(msg.getRTFBody());
            // System.exit(0);

            // PSTAppointment msg =
            // (PSTAppointment)PSTObject.detectAndLoadPSTObject(pstFile,
            // 2097252);
            //// System.out.println(msg.getStartTime());
            // System.exit(0);

            // int[] emails = {
            // 2098180
            /*
             * 2097348,
             * 2097380,
             * 2097412,
             * 2097444,
             * 2097476,
             * 2097508,
             * 2097540,
             * 2097572
             *
             */
            // };

            /*
             * 
             * RandomAccessFile tmpIn = new
             * RandomAccessFile("test - httpdocs.tar.gz", "r");
             * PSTMessage msg =
             * (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, emails[0]);
             * PSTAttachmentInputStream attachmentStream =
             * msg.getAttachment(0).getFileInputStream();
             * 
             * byte[] tmp = new byte[1024];
             * byte[] tmp2 = new byte[1024];
             * 
             * for (int y = 1; y < 2000; y++) {
             * tmpIn.seek(760*y-50);
             * tmpIn.read(tmp);
             * 
             * attachmentStream.seek(760*y-50);
             * attachmentStream.read(tmp2);
             * 
             * for (int x = 0; x< tmp2.length; x++) {
             * if (tmp[x] != tmp2[x]) {
             * 
             * 
             * PSTObject.printHexFormatted(tmp, true);
             * PSTObject.printHexFormatted(tmp2, true);
             * 
             * System.out.println(y);
             * System.out.println("Error");
             * System.exit(0);
             * }
             * }
             * System.out.println("Worked");
             * }
             *
             */

            /*
             * for (int x = 0; x < emails.length; x++) {
             * PSTMessage msg =
             * (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, emails[x]);
             * PSTAttachment attach = msg.getAttachment(0);
             * //System.out.println(attach);
             * InputStream attachmentStream =
             * msg.getAttachment(0).getFileInputStream();
             * FileOutputStream out = new
             * FileOutputStream("test - "+attach.getLongFilename());
             * 
             * int bufferSize = 8176;
             * 
             * byte[] buffer = new byte[bufferSize];
             * int count = attachmentStream.read(buffer, 0, 8176);
             * while (count == bufferSize) {
             * out.write(buffer);
             * count = attachmentStream.read(buffer, 0, 8176);
             * }
             * byte[] endBuffer = new byte[count];
             * System.arraycopy(buffer, 0, endBuffer, 0, count);
             * out.write(endBuffer);
             * out.close();
             * }
             *
             */
            // System.exit(0);

        } catch (final Exception err) {
            err.printStackTrace();
            System.exit(1);
        }

        // do the tree thing
        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(this.pstFile.getMessageStore());
        try {
            this.buildTree(top, this.pstFile.getRootFolder());
        } catch (final Exception err) {
            err.printStackTrace();
            System.exit(1);
        }

        final JTree folderTree = new JTree(top) {
            @Override
            public String convertValueToText(final Object value, final boolean selected, final boolean expanded,
                final boolean leaf, final int row, final boolean hasFocus) {
                final DefaultMutableTreeNode nodeValue = (DefaultMutableTreeNode) value;
                if (nodeValue.getUserObject() instanceof PSTFolder) {
                    final PSTFolder folderValue = (PSTFolder) nodeValue.getUserObject();

                    return folderValue.getDescriptorNodeId() + " - " + folderValue.getDisplayName() + " "
                        + folderValue.getAssociateContentCount() + "";
                } else if (nodeValue.getUserObject() instanceof PSTMessageStore) {
                    final PSTMessageStore folderValue = (PSTMessageStore) nodeValue.getUserObject();
                    return folderValue.getDisplayName();
                } else {
                    return value.toString();
                }
            }
        };
        final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getDefaultClosedIcon());
        folderTree.setCellRenderer(renderer);

        // event handler for changing...
        folderTree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) folderTree.getLastSelectedPathComponent();
            if (node == null) {
                return;
            }
            if (node.getUserObject() instanceof PSTFolder) {
                final PSTFolder folderValue = (PSTFolder) node.getUserObject();
                try {
                    TestGui.this.selectFolder(folderValue);
                } catch (final Exception err) {
                    System.out.println("unable to change folder");
                    err.printStackTrace();
                }
            }
        });
        final JScrollPane treePane = new JScrollPane(folderTree);

        // the table
        JScrollPane emailTablePanel = null;
        try {
            this.emailTableModel = new EmailTableModel(this.pstFile.getRootFolder(), this.pstFile);
            final JTable emailTable = new JTable(this.emailTableModel);
            emailTablePanel = new JScrollPane(emailTable);
            emailTable.setFillsViewportHeight(true);
            final ListSelectionModel selectionModel = emailTable.getSelectionModel();
            selectionModel.addListSelectionListener(e -> {
                final JTable source = emailTable;
                TestGui.this.selectedMessage = TestGui.this.emailTableModel.getMessageAtRow(source.getSelectedRow());
                if (TestGui.this.selectedMessage instanceof PSTContact) {
                    final PSTContact contact = (PSTContact) TestGui.this.selectedMessage;
                    TestGui.this.emailText.setText(contact.toString());
                } else if (TestGui.this.selectedMessage instanceof PSTTask) {
                    final PSTTask task = (PSTTask) TestGui.this.selectedMessage;
                    TestGui.this.emailText.setText(task.toString());
                } else if (TestGui.this.selectedMessage instanceof PSTActivity) {
                    final PSTActivity journalEntry = (PSTActivity) TestGui.this.selectedMessage;
                    TestGui.this.emailText.setText(journalEntry.toString());
                } else if (TestGui.this.selectedMessage instanceof PSTRss) {
                    final PSTRss rss = (PSTRss) TestGui.this.selectedMessage;
                    TestGui.this.emailText.setText(rss.toString());
                } else if (TestGui.this.selectedMessage != null) {
                    // System.out.println(selectedMessage.getMessageClass());
                    TestGui.this.emailText.setText(TestGui.this.selectedMessage.getBody());
                    // System.out.println(selectedMessage);
                    // emailText.setText(selectedMessage.toString());
                    // emailText.setText(selectedMessage.toString());
                    // PSTTask task = selectedMessage.toTask();
                    // emailText.setText(task.toString());
                }
                TestGui.this.setAttachmentText();

                // treePane.getViewport().setViewPosition(new Point(0,0));
                TestGui.this.emailText.setCaretPosition(0);
            });
        } catch (final Exception err) {
            err.printStackTrace();
        }

        this.f.setJMenuBar(this.createMenu());

        // the email
        this.emailText = new JTextPane();
        this.emailText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        // emailText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));

        this.emailPanel = new JPanel(new BorderLayout());
        this.attachPanel = new JPanel(new BorderLayout());
        this.attachLabel = new JLabel("Attachments:");
        this.attachText = new JTextField("");
        this.attachText.setEditable(false);
        this.attachPanel.add(this.attachLabel, BorderLayout.WEST);
        this.attachPanel.add(this.attachText, BorderLayout.CENTER);
        this.emailPanel.add(this.attachPanel, BorderLayout.NORTH);
        this.emailPanel.add(this.emailText, BorderLayout.CENTER);

        final JSplitPane emailSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailTablePanel,
            new JScrollPane(this.emailPanel));
        emailSplitPane.setOneTouchExpandable(true);
        emailSplitPane.setDividerLocation(0.25);

        // add a split pane, 1 for our tree, the other for our emails
        final JSplitPane primaryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, emailSplitPane);
        primaryPane.setOneTouchExpandable(true);
        primaryPane.setDividerLocation(0.3);
        this.f.add(primaryPane);

        // Set the default close operation for the window,
        // or else the program won't exit when clicking close button
        this.f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Set the visibility as true, thereby displaying it
        this.f.setVisible(true);
        // f.setSize(800, 600);
        this.f.setExtendedState(this.f.getExtendedState() | Frame.MAXIMIZED_BOTH);
    }

    private void buildTree(final DefaultMutableTreeNode top, final PSTFolder theFolder) {
        // this is recursive, try and keep up.
        try {
            final Vector children = theFolder.getSubFolders();
            final Iterator childrenIterator = children.iterator();
            while (childrenIterator.hasNext()) {
                final PSTFolder folder = (PSTFolder) childrenIterator.next();

                final DefaultMutableTreeNode node = new DefaultMutableTreeNode(folder);

                if (folder.getSubFolders().size() > 0) {
                    this.buildTree(node, folder);
                } else {
                }
                top.add(node);
            }
        } catch (final Exception err) {
            err.printStackTrace();
            System.exit(1);
        }
    }

    void setAttachmentText() {
        final StringBuffer s = new StringBuffer();

        try {
            if (this.selectedMessage != null) {
                final int numAttach = this.selectedMessage.getNumberOfAttachments();
                for (int x = 0; x < numAttach; x++) {
                    final PSTAttachment attach = this.selectedMessage.getAttachment(x);
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
        } catch (final Exception e) {
        }

        this.attachText.setText(s.toString());
    }

    void selectFolder(final PSTFolder folder) throws IOException, PSTException {
        // load up the non-folder children.

        this.emailTableModel.setFolder(folder);

    }

    public JMenuBar createMenu() {
        JMenuBar menuBar;
        JMenu menu;

        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        final JMenuItem menuItem = new JMenuItem("Save Attachments", KeyEvent.VK_S);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        return menuBar;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JMenuItem source = (JMenuItem) (e.getSource());
        if (source.getText() == "Save Attachments") {
            this.saveAttachments();
        }
    }

    private void saveAttachments() {
        if (this.selectedMessage != null) {
            final int numAttach = this.selectedMessage.getNumberOfAttachments();
            if (numAttach == 0) {
                JOptionPane.showMessageDialog(this.f, "Email has no attachments");
                return;
            }
            try {
                for (int x = 0; x < numAttach; x++) {
                    final PSTAttachment attach = this.selectedMessage.getAttachment(x);
                    final InputStream attachmentStream = attach.getFileInputStream();
                    String filename = attach.getLongFilename();
                    if (filename.isEmpty()) {
                        filename = attach.getFilename();
                    }
                    final JFileChooser chooser = new JFileChooser();
                    chooser.setSelectedFile(new File(filename));
                    final int r = chooser.showSaveDialog(this.f);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        final FileOutputStream out = new FileOutputStream(chooser.getSelectedFile());
                        // 8176 is the block size used internally and should
                        // give the best performance
                        final int bufferSize = 8176;
                        final byte[] buffer = new byte[bufferSize];
                        int count;
                        do {
                            count = attachmentStream.read(buffer);
                            out.write(buffer, 0, count);
                        } while (count == bufferSize);
                        out.close();
                    }
                    attachmentStream.close();
                }
            } catch (final IOException ioe) {
                JOptionPane.showMessageDialog(this.f, "Failed writing to file");
            } catch (final PSTException pste) {
                JOptionPane.showMessageDialog(this.f, "Error in PST file");
            }
        }
    }

    /**
     * @param args
     * @throws IOException
     * @throws PSTException
     */
    public static void main(final String[] args) throws PSTException, IOException {
        new TestGui();
    }

}

class EmailTableModel extends AbstractTableModel {

    PSTFolder theFolder = null;
    PSTFile theFile = null;

    HashMap cache = new HashMap();

    public EmailTableModel(final PSTFolder theFolder, final PSTFile theFile) {
        super();

        this.theFolder = theFolder;
        this.theFile = theFile;
    }

    String[] columnNames = { "Descriptor ID", "Subject", "From", "To", "Date", "Has Attachments" };
    String[][] rowData = { { "", "", "", "", "" } };
    int rowCount = 0;

    @Override
    public String getColumnName(final int col) {
        return this.columnNames[col].toString();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public int getRowCount() {
        try {
            // System.out.println("Email count: "+theFolder.getEmailCount());
            return this.theFolder.getContentCount();
        } catch (final Exception err) {
            err.printStackTrace();
            System.exit(0);
        }
        return 0;
    }

    public PSTMessage getMessageAtRow(final int row) {
        PSTMessage next = null;
        try {
            if (this.cache.containsKey(row)) {
                next = (PSTMessage) this.cache.get(row);
            } else {
                this.theFolder.moveChildCursorTo(row);
                next = (PSTMessage) this.theFolder.getNextChild();
                this.cache.put(row, next);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return next;
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        // get the child at...
        try {
            final PSTMessage next = this.getMessageAtRow(row);

            if (next == null) {
                return null;
            }

            switch (col) {
            case 0:
                return next.getDescriptorNode().descriptorIdentifier + "";
            case 1:
                return next.getSubject();
            case 2:
                return next.getSentRepresentingName() + " <" + next.getSentRepresentingEmailAddress() + ">";
            case 3:
                return next.getReceivedByName() + " <" + next.getReceivedByAddress() + ">" + next.getDisplayTo();
            case 4:
                return next.getClientSubmitTime();
            // return next.isFlagged();
            // return next.isDraft();
            // PSTTask task = next.toTask();
            // return task.toString();
            case 5:
                return (next.hasAttachments() ? "Yes" : "No");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return "";
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
        return false;
    }

    public void setFolder(final PSTFolder theFolder) throws PSTException, IOException {
        theFolder.moveChildCursorTo(0);
        this.theFolder = theFolder;
        this.cache = new HashMap();
        this.fireTableDataChanged();
    }

}
