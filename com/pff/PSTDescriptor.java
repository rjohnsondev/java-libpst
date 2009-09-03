/**
 * 
 */
package com.pff;

import java.io.*;
import java.util.*;

/**
 * PST Descriptor handles the processing of the descriptors associated with a PST Item
 * Not to be confused with the DescriptorsIndexNodes which point to an item, this rather clumsy
 * terminology came from the PFF file format specification I was working off.  This actually handles
 * data which describes an item.
 * @author Richard Johnson
 */
class PSTDescriptor {
	
	private RandomAccessFile in;
	private int encryptionType;
	private HashMap<Integer, PSTDescriptorItem> children = new HashMap<Integer, PSTDescriptorItem>();
	
	PSTDescriptor(PSTFile theFile, long localDescriptorsOffsetIndexIdentifier)
		throws IOException, PSTException
	{
		// make sure we have a valid index identifier...
		if (localDescriptorsOffsetIndexIdentifier == 0) {
			throw new PSTException("unable to create PSTDescriptor, invalid descriptors offset passed!");
		}

		in = theFile.getFileHandle();
		encryptionType = theFile.getEncryptionType();
		
		// we need to get out the local descriptor which will give us descriptor node lookups for the file location of larger blobs of data
		OffsetIndexItem localDescriptorOffset = PSTObject.getOffsetIndexNode(in, localDescriptorsOffsetIndexIdentifier);
		
		byte[] data = new byte[localDescriptorOffset.size];
		in.seek(localDescriptorOffset.fileOffset);
		in.read(data);
		this.children = processDescriptor(data);
	}
	
	HashMap<Integer, PSTDescriptorItem> getChildren() {
		return this.children;
	}
	
	private HashMap<Integer, PSTDescriptorItem> processDescriptor(byte[] data)
		throws PSTException, IOException
	{
		
		// make sure the signature is correct
		if (data[0] != 0x2) {
			throw new PSTException("Unable to process descriptor node, bad signature: "+data[0]);
		}
		
		HashMap<Integer, PSTDescriptorItem> output = new HashMap<Integer, PSTDescriptorItem>();
		
		int numberOfItems = (int)PSTObject.convertLittleEndianBytesToLong(data, 2, 4);
		int offset = 8;
		
		for (int x = 0; x < numberOfItems; x++) {
			
			PSTDescriptorItem item = new PSTDescriptorItem();
			
			item.descriptorIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset, offset+4);
			item.offsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+8, offset+16);
			item.subNodeOffsetIndexIdentifier =  (int)PSTObject.convertLittleEndianBytesToLong(data, offset+16, offset+24);
			
			item.offsetIndexIdentifier = (item.offsetIndexIdentifier & 0xfffffffe);
			
			OffsetIndexItem itemOffsetIndex = PSTObject.getOffsetIndexNode(in, item.offsetIndexIdentifier);
			item.data = new byte[itemOffsetIndex.size];
			in.seek(itemOffsetIndex.fileOffset);
			in.read(item.data);
			
			// is the data an array?
			if (PSTObject.isPSTArray(item.data))
			{
				item.data = PSTObject.processArray(in, item.data);
			}
			if (encryptionType == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE) {
				item.data = PSTObject.decode(item.data);
			}
			if (item.subNodeOffsetIndexIdentifier != 0) {

				item.subNodeOffsetIndexIdentifier = (item.subNodeOffsetIndexIdentifier & 0xfffffffe);
				OffsetIndexItem subNodeLocalDescriptorOffset = PSTObject.getOffsetIndexNode(in, item.subNodeOffsetIndexIdentifier);
			
				byte[] subNodeData = new byte[subNodeLocalDescriptorOffset.size];
				in.seek(subNodeLocalDescriptorOffset.fileOffset);
				in.read(subNodeData);

				// recurse baby
				item.subNodeDescriptorItems = processDescriptor(subNodeData);
				output.putAll(item.subNodeDescriptorItems);
			}
			
			output.put(item.descriptorIdentifier, item);
			
			offset += 24;
		}
		
		return output;
	}
	
}
