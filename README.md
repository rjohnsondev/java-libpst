The PST File format is used by Outlook for the storage of emails.  Over the years many people have accumulated a large amount of important email and other information into these files, this project aims to allow people to access and extract this information so that it may be migrated to other messaging systems.

This project was originally based off the documentation created through the fantastic reverse engineering effort made by the [libpff](https://sourceforge.net/projects/libpff) project.  The library has been improved with information provided by the release of the official PST specs by Microsoft.

The functional goals are:

  * Efficiency; should be able to work with very large PST files with reasonable speed
  * Support for compressible encryption (on by default with newer versions of Outlook)
  * Intuitive API
  * Support for ANSI (32bit), Unicode (64bit) Outlook PST and Exchange OST Files.

Things that the library will most likely not do:

  * Fix or work with broken PST files
  * Provide write access to PST files
  * Recover deleted email items

For example usage of the library please see the TestGui application stored in the examples folder.  Javadocs are available here: http://rjohnsondev.github.io/java-libpst

Accessing the contents of a PSTFile is a matter of following the folder structure down to the desired email.  This example reads a PST and prints the tree structure to the console:

```java
package example;
import com.pff.*;
import java.util.*;

public class Test {
    public static void main(String[] args)
    {
        new Test(args[0]);
    }

    public Test(String filename) {
        try {
            PSTFile pstFile = new PSTFile(filename);
            System.out.println(pstFile.getMessageStore().getDisplayName());
            processFolder(pstFile.getRootFolder());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    int depth = -1;
    public void processFolder(PSTFolder folder)
            throws PSTException, java.io.IOException
    {
        depth++;
        // the root folder doesn't have a display name
        if (depth > 0) {
            printDepth();
            System.out.println(folder.getDisplayName());
        }

        // go through the folders...
        if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(childFolder);
            }
        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            depth++;
            PSTMessage email = (PSTMessage)folder.getNextChild();
            while (email != null) {
                printDepth();
                System.out.println("Email: "+email.getSubject());
                email = (PSTMessage)folder.getNextChild();
            }
            depth--;
        }
        depth--;
    }

    public void printDepth() {
        for (int x = 0; x < depth-1; x++) {
            System.out.print(" | ");
        }
        System.out.print(" |- ");
    }
}
```

Attachments can be read through PSTAttachment.getFileInputStream like so:

```java
int numberOfAttachments = email.getNumberOfAttachments();
for (int x = 0; x < numberOfAttachments; x++) {
    PSTAttachment attach = email.getAttachment(x);
    InputStream attachmentStream = attach.getFileInputStream();
    // both long and short filenames can be used for attachments
    String filename = attach.getLongFilename();
    if (filename.isEmpty()) {
        filename = attach.getFilename();
    }
    FileOutputStream out = new FileOutputStream(filename);
    // 8176 is the block size used internally and should give the best performance
    int bufferSize = 8176;
    byte[] buffer = new byte[bufferSize];
    int count = attachmentStream.read(buffer);
    while (count == bufferSize) {
        out.write(buffer);
        count = attachmentStream.read(buffer);
    }
    byte[] endBuffer = new byte[count];
    System.arraycopy(buffer, 0, endBuffer, 0, count);
    out.write(endBuffer);
    out.close();
    attachmentStream.close();
}
```

Each object in the PST has a unique identifier called a descriptor node id.  This can be useful for retrieving known objects quickly from the PST:

```java
long id = email.getDescriptorNodeId();
pstObject = PSTObject.detectAndLoadPSTObject(pstFile, id);
```

