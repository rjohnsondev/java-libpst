/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pst2Gmail;

import com.sun.mail.imap.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.pff.*;
import java.io.*;
import javax.activation.*;
import javax.mail.Flags.Flag;

/**
 *
 * @author toweruser
 */
public class Transfer
        implements Runnable
{

    /**
     * gmail vars
     */
    private String username = "";
    private String password = "";
    private String host = "imap.gmail.com";

    private IMAPStore store = null;

    /**
     * PST Vars
     */
    private PSTFile pstFile = null;

    /**
     * set the credentials we will use to connect to the gmail services
     * @param host
     * @param username
     * @param password
     */
    public void setCredentials(String username, String password) {
        disconnect();
        this.username = username;
        this.password = password;
    }

    /**
     * attempt to connect to the various GMail services we will be using...
     * @throws javax.mail.NoSuchProviderException
     * @throws javax.mail.MessagingException
     */
    public void connect()
            throws javax.mail.NoSuchProviderException, javax.mail.MessagingException
    {

        Properties props = System.getProperties();

        // attempt to start a session with an imap provider
        Session session = Session.getInstance(props, null);

        store = (IMAPStore)session.getStore("imaps");
        store.connect(this.host,this.username,this.password);
    }

    /**
     * are we connected to the IMAP server
     */
    public boolean isIMAPConnected() {
        if (this.store == null)
            return false;
        return this.store.isConnected();
    }

    /**
     * disconnect from the services we may be connected to
     */
    public void disconnect()
    {
        if (this.store != null && this.store.isConnected()) {
            try {
                this.store.close();
            } catch (MessagingException err) {
                // we don't really care, we're just closing anyways.
            }
        }
    }

    public Vector getIMAPFolders() {
        if (!isIMAPConnected()) {
            try {
                connect();
            } catch (Exception err) {
                return new Vector();
            }
        }

        try {
            return getIMAPFolders(this.store.getDefaultFolder().list());
            //Vector<Folder> output = new Vector<Folder>(Arrays.asList(this.store.getDefaultFolder().list()));
            //System.out.println(output);
            //return output;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return new Vector();
    }

    private Vector<IMAPFolderSelectItem> getIMAPFolders(Folder[] children) {
        try {
            Vector<IMAPFolderSelectItem> outputSet = new Vector<IMAPFolderSelectItem>();
            for (int x = 0; x < children.length; x++) {
                Folder child = children[x];
                outputSet.add(new IMAPFolderSelectItem(child));
                Folder[] childsKids = child.list();
                if (childsKids.length > 0) { 
                    outputSet.addAll(getIMAPFolders(childsKids));
                }
            }
            return outputSet;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return new Vector<IMAPFolderSelectItem>();
    }

    /* -------------------------
     *  PST related stuff below
     * ------------------------- */

    public void openPST(File PSTFile)
            throws FileNotFoundException, PSTException, IOException
    {
        this.pstFile = new PSTFile(PSTFile.getAbsolutePath());
    }

    public boolean canTransfer() {
        return (this.isIMAPConnected() && this.pstFile != null);
    }

    public String getPSTDetails() {
        if (pstFile == null) {
            return "No PST File Loaded";
        }
        // otherwise, try and get some stats
        try {
            return
                "File Size: "+this.pstFile.getFileHandle().length()/1024/1024+"MB\n"+
                "Message Store Name: "+pstFile.getMessageStore().getDisplayName();
        } catch (Exception err) {
            return err.toString();
        }
    }

    public int getItemCount() {
        this.totalItems = getItemCount(this.sourceFolder);
        return this.totalItems;
    }

    public int getItemCount(PSTFolder folder) {
        int output = 0;
        if (folder.hasSubfolders() && this.includeSubFolders) {
            try {
                Vector<PSTFolder> children = folder.getSubFolders();
                for (PSTFolder child : children) {
                    // recurse!
                    output += getItemCount(child);
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        output += folder.getContentCount();
        return output;
    }

    Vector<PSTFolderSelectItem> getPSTFolders() {
        if (pstFile == null) {
            return new Vector<PSTFolderSelectItem>();
        }
        try {
            //Vector<PSTFolderSelectItem> items = getFolders(pstFile.getRootFolder(), pstFile.getMessageStore().getDisplayName());
            Vector<PSTFolderSelectItem> items = getPSTFolders(pstFile.getRootFolder(), "");
            return items;
        } catch (Exception err) {
            err.printStackTrace();
            return new Vector<PSTFolderSelectItem>();
        }
    }

    Vector<PSTFolderSelectItem> getPSTFolders(PSTFolder parent, String name) {
        try {
            Vector<PSTFolder> children = parent.getSubFolders();
            Iterator iterator = children.iterator();
            Vector<PSTFolderSelectItem> outputSet = new Vector<PSTFolderSelectItem>();
            while (iterator.hasNext()) {
                PSTFolder next = (PSTFolder)iterator.next();
                String newName;
                if (name.length() > 0) {
                    newName = name +" > " + next.getDisplayName();
                } else {
                    newName = next.getDisplayName();
                }
                PSTFolderSelectItem item = new PSTFolderSelectItem(next, newName);
                outputSet.add(item);
                // update approx defaults
                if (newName.equals("Top of Personal Folders > Inbox")) {
                    this.inbox = item;
                } else if (newName.equals("Top of Personal Folders > Contacts")) {
                    this.contacts = item;
                } else if (newName.equals("Top of Personal Folders > Calendar")) {
                    this.calendar = item;
                }

                if (next.hasSubfolders()) {
                    outputSet.addAll(getPSTFolders(next, newName));
                }
            }
            return outputSet;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return new Vector<PSTFolderSelectItem>();
    }

    private PSTFolderSelectItem inbox = null;
    private PSTFolderSelectItem contacts = null;
    private PSTFolderSelectItem calendar = null;

    public PSTFolderSelectItem getInbox() {
        return inbox;
    }
    public PSTFolderSelectItem getContacts() {
        return contacts;
    }
    public PSTFolderSelectItem getCalendar() {
        return calendar;
    }

    /* -----------------------------
     *  Transfer stuff below
     * ----------------------------- */

    public void run() {
        if (this.canTransfer() && 
            this.sourceFolder != null &&
            this.destinationFolder != null)
        {
            // start the process!
            try {
                if (this.totalItems == 0) {
                    this.totalItems = getItemCount(this.sourceFolder);
                }
                PrintStream out = new PrintStream(new File("log.txt"));
                transferFolder(this.sourceFolder, this.destinationFolder, out);
                this.hasCompleted = true;
                this.updateStatus("Completed");
            } catch (Exception err) {
                err.printStackTrace();
            }
        } else {
            System.out.println("Not ready to transfer");
        }
    }

    PSTFolder sourceFolder = null;
    IMAPFolder destinationFolder = null;

    public void setFolderSelections(PSTFolderSelectItem from, IMAPFolderSelectItem to)
    {
        // get our testing email
        this.sourceFolder = from.getFolder();
        this.destinationFolder = (IMAPFolder)to.getFolder();
    }

    public void addStatusListener(TransferStatusListener listener) {
        this.listeners.add(listener);
    }
    Vector<TransferStatusListener> listeners = new Vector<TransferStatusListener>();
    public void updateStatus(String newStatus) {
        for (TransferStatusListener listener : this.listeners) {
            listener.statusUpdated(newStatus);
        }
    }

    public void updateNumberUploded(int numberUploaded) {
        this.completedItems += numberUploaded;
        for (TransferStatusListener listener : this.listeners) {
            listener.progressUpdated(totalItems, completedItems);
        }
    }

    public void updateExpectedDuration(long transferSize) {
        // rotate and get the average.
        int totalSize = 0;
        for (int x = 1; x < AVERAGE_TRANSFER_OVER; x++) {
            transferTimestamps[x-1] = transferTimestamps[x];
            totalSize += transferSizes[x];
            transferSizes[x-1] = transferSizes[x];
        }
        transferTimestamps[AVERAGE_TRANSFER_OVER-1] = new Date().getTime();
        transferSizes[AVERAGE_TRANSFER_OVER-1] = transferSize;
        totalSize += transferSize;
        long duration = transferTimestamps[AVERAGE_TRANSFER_OVER-1] - transferTimestamps[0];
        double kbps = (double)(totalSize / 1024) / ((double)duration / 1000);
        int numberOfChildrenInWindow = AVERAGE_TRANSFER_OVER * CHILDREN_TO_TRANSFER_AT_ONCE ;
        double emailsps = (double)numberOfChildrenInWindow / ((double)duration / 1000);
        Date expectionCompletion = new Date((long)((totalItems - completedItems) / emailsps)*1000 + transferTimestamps[AVERAGE_TRANSFER_OVER-1]);
        for (TransferStatusListener listener : this.listeners) {
            listener.ratesUpdated(kbps, emailsps, expectionCompletion);
        }
    }

    private int totalItems = 0;
    private int completedItems = 0;
    private boolean hasCompleted = false;

    private static final int CHILDREN_TO_TRANSFER_AT_ONCE = 2;
    private static final int AVERAGE_TRANSFER_OVER = 4;

    private long[] transferTimestamps = new long[AVERAGE_TRANSFER_OVER];
    private long[] transferSizes = new long[AVERAGE_TRANSFER_OVER];

    private boolean includeSubFolders = false;

    public void includeSubFolders() {
        this.includeSubFolders = true;
    }
    
    public boolean hasCompleted() {
        return this.hasCompleted;
    }

    public void transferFolder(PSTFolder from, IMAPFolder to, PrintStream log)
    {
        // go through the emails in this folder and transfer them.
        // let's do this in groups of 32...
        int numberOfChildren = CHILDREN_TO_TRANSFER_AT_ONCE;
        try {
            // transfer the emails in this folder
            updateStatus("Starting Folder: "+from.getDisplayName());
            to.open(IMAPFolder.READ_WRITE);
            Vector<PSTObject> children = from.getChildren(numberOfChildren);
            while (children.size() > 0) {
                Vector<MimeMessage> mimeChildren = new Vector<MimeMessage>();
                String messageList = "";
                long transferSize = 0;
                for (PSTObject obj : children) {
                    // tell our log where we are up to...
                    if (obj instanceof PSTMessage) {
                        PSTMessage msg = (PSTMessage)obj;
                        log.print("Creating "+msg.getDescriptorNode().descriptorIdentifier+"\n");
                        updateStatus("Creating IMAP Version of msg #"+msg.getDescriptorNode().descriptorIdentifier+"  \""+msg.getSubject()+"\" <"+msg.getClientSubmitTime()+">");
                        MimeMessage mimeVersion = convertMessage(msg);
                        mimeChildren.add(mimeVersion);
                        messageList += msg.getDescriptorNode().descriptorIdentifier+",";
                        transferSize += msg.getMessageSize();
                    } else {
                        log.print("Skipping "+obj.getDescriptorNode().descriptorIdentifier+", it doesn't appear to be a PSTMessage...\n");
                    }
                }
                // attempt to insert the bunch
                log.print("Uploading messages: "+messageList.substring(0, messageList.length()-1)+"\n");
                updateStatus("Uploading messages: "+messageList.substring(0, messageList.length()-1));
                to.addMessages(mimeChildren.toArray(new MimeMessage[0]));
                log.print("Completed Upload of: "+messageList.substring(0, messageList.length()-1)+"\n");
                updateStatus("Uploading Complete");
                updateNumberUploded(mimeChildren.size());
                updateExpectedDuration(transferSize);
                // get the next bunch of messages
                children = from.getChildren(numberOfChildren);
            }
            to.close(false);
            updateStatus("Completed Folder: "+from.getDisplayName());

            // now, recurse!
            if (this.includeSubFolders) {
                Vector<PSTFolder> childFolders = from.getSubFolders();
                for (PSTFolder childFolder : childFolders) {
                    // see if there is a folder of this name in imap
                    IMAPFolder childIMAPFolder = (IMAPFolder)to.getFolder(childFolder.getDisplayName());
                    if (!childIMAPFolder.exists()) {
                        childIMAPFolder.create(IMAPFolder.HOLDS_MESSAGES);
                    }
                    // do the transfer!
                    transferFolder(childFolder, childIMAPFolder, log);
                }
            }

        } catch (Exception err) {
            System.out.println("Error transferring folder: "+from.getDisplayName()+"\n");
            err.printStackTrace();
            System.exit(0);
        }
    }

    public MimeMessage convertMessage(PSTMessage msg)
            throws PSTException, IOException, MessagingException
    {
        Properties props = System.getProperties();
        Session session = Session.getInstance(props, null);
        MimeMessage newMsg = new MimeMessage(session);

        // content of our email.
        MimeMultipart content = new MimeMultipart("alternative");

        // now the attachments
        // we have to save the attachments from the PST to a temporary file.
        for (int x =0; x < msg.getNumberOfAttachments(); x ++) {
            MimeBodyPart attach = new MimeBodyPart();
            PSTAttachment pstAttach = msg.getAttachment(x);
            String filename = pstAttach.getLongFilename();
            File tmpFile = File.createTempFile("pst", filename.substring(filename.length()-4));
            tmpFile.deleteOnExit();
            // populate our tempFile with contents from the pst
            FileOutputStream out = new FileOutputStream(tmpFile);
            try {
                out.write(pstAttach.getFileContents());
            } catch (PSTException err){
                // attachment is most likely empty...
                System.out.println("Warning accessing attachment "+filename+": "+err.toString());
            }
            out.close();
            DataSource source = new FileDataSource(tmpFile);
            attach.setDataHandler(new DataHandler(source));
            attach.setFileName(pstAttach.getLongFilename());
            content.addBodyPart(attach);
        }

        // now the body
        MimeBodyPart text = new MimeBodyPart();
        text.setText( msg.getBody() );
        content.addBodyPart(text);
        MimeBodyPart html = new MimeBodyPart();
        html.setContent(msg.getBodyHTML(), "text/html");
        content.addBodyPart(html);

        // set the content
        newMsg.setText(msg.getBody());
        newMsg.setContent( content );
        newMsg.setHeader("Content-Type" , content.getContentType() );

        // okay, go through the other headers
        String headers = msg.getTransportMessageHeaders();
        String[] bits = headers.split("\n");
        String header = bits[0];
        for (int x = 1; x < bits.length; x++) {
            // if we are a continuation of the previous header, append! otherwise add!
            String nextHeader = bits[x];
            if (Character.isWhitespace(nextHeader.charAt(0))) {
                // we need to append!
                header = header + "\n" + nextHeader;
            } else {
                // we are good to add!
                if (header.toLowerCase().startsWith("to:")) {
                    newMsg.addRecipients(Message.RecipientType.TO, header.substring(4));
                } else if (header.toLowerCase().startsWith("cc:")) {
                    newMsg.addRecipients(Message.RecipientType.CC, header.substring(4));
                } else if (header.toLowerCase().startsWith("bcc:")) {
                    newMsg.addRecipients(Message.RecipientType.BCC, header.substring(5));
                } else if (header.toLowerCase().startsWith("from:")) {
                    newMsg.setFrom(new InternetAddress(header.substring(6)));
                } else {
                    newMsg.addHeaderLine(header);
                }
                //System.out.println("added: "+header);
                header = nextHeader;
            }
        }
        newMsg.setSubject(msg.getSubject());
        // Flags
        newMsg.setFlag(Flag.ANSWERED, msg.hasReplied());
        //newMsg.setFlag(Flag.DRAFT, msg.isDraft());  // for some reason, the draft property as document doesn't actually exist??
        newMsg.setFlag(Flag.FLAGGED, msg.isFlagged());
        newMsg.setFlag(Flag.SEEN, msg.isRead());
        return newMsg;
    }

}


class PSTFolderSelectItem {
    private PSTFolder folder;
    private String name;
    public PSTFolderSelectItem(PSTFolder folder, String name) {
        this.folder = folder;
        this.name = name;
    }
    public PSTFolder getFolder() {
        return this.folder;
    }
    @Override
    public String toString() {
        return this.name;
    }
}

class IMAPFolderSelectItem {
    private Folder folder;
    private String name;
    public IMAPFolderSelectItem(Folder folder) {
        this.folder = folder;
    }
    public IMAPFolderSelectItem(Folder folder, String name) {
        this.folder = folder;
        this.name = name;
    }
    public Folder getFolder() {
        return this.folder;
    }
    @Override
    public String toString() {
        return this.folder.getFullName();
    }
}

interface TransferStatusListener {
    public void statusUpdated(String message);
    public void progressUpdated(int totalNumberOfMessages, int numberMessagesCompleted);
    public void ratesUpdated(double kbps, double emailsps, Date completionTime);
}