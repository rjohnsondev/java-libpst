/**
 * 
 */
package com.pff;

import java.io.*;
import java.util.*;


/**
 * @author toweruser
 *
 */
public class PSTEmail extends PSTMessage {
	
	PSTEmail(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
			throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}

	PSTEmail(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems){
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	
}
