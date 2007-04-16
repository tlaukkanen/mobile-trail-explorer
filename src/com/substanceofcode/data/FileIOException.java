package com.substanceofcode.data;

import java.io.IOException;

/**
 * <p>This Exception is thrown whenever an error occurs in writing to the RMS/FileSystem</p>
 * 
 * @author Barry
 */
public class FileIOException extends IOException{

	/**
	 * Creates a new FileIOException with the specified text.
	 * 
	 * @param reason The text for this exception.
	 */
	public FileIOException(String reason){
		super(reason);
	}
}
