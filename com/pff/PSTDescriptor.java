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
	
	private PSTFile pstFile;
	private HashMap<Integer, PSTDescriptorItem> children = new HashMap<Integer, PSTDescriptorItem>();
	
	PSTDescriptor(PSTFile theFile, long localDescriptorsOffsetIndexIdentifier)
		throws IOException, PSTException
	{
		// make sure we have a valid index identifier...
		if (localDescriptorsOffsetIndexIdentifier == 0) {
			throw new PSTException("unable to create PSTDescriptor, invalid descriptors offset passed!");
		}

		pstFile = theFile;
		
		// we need to get out the local descriptor which will give us descriptor node lookups for the file location of larger blobs of data
		PSTFile.PSTFileBlock dataBlock = pstFile.readLeaf(localDescriptorsOffsetIndexIdentifier);
		this.children = processDescriptor(dataBlock.data);
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
		int offset;
		if (this.pstFile.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
			offset = 4;
		} else {
			offset = 8;
		}
		
		for (int x = 0; x < numberOfItems; x++) {
			
			PSTDescriptorItem item = new PSTDescriptorItem(data, offset, pstFile);

			byte[] subNodeData = item.getSubNodeData();
			if ( subNodeData != null) {
				if (subNodeData[0] == 0x2) {
					// recurse baby
					item.setSubNodeDescriptorItems(processDescriptor(subNodeData));
					output.putAll(item.getSubNodeDescriptorItems());
				} else {
					// this isn't normal :/
				}
			}

			output.put(item.descriptorIdentifier, item);

			if (this.pstFile.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
				offset += 12;
			} else {
				offset += 24;
			}
		}
		
		return output;
	}
	
}
