/**
 * 
 */
package com.pff;

/**
 * Simple exception for PST File related errors
 * @author Richard Johnson
 */
public class PSTException extends Exception
{
	/**
	 * eclipse generated serial UID
	 */
	private static final long serialVersionUID = 4284698344354718143L;
	
	PSTException(String error) {
		super(error);
	}
	PSTException(String error, Exception orig) {
		super(error, orig);
	}
}
