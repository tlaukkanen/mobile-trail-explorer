package com.substanceofcode.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {
	
    public static final String MIMETYPE_SERIALIZED_CLASS = "serialized/class";
    
	/**
	 * This method should serialize the given file into a 
	 * sequence of bytes so that it can be easily stored in 
	 * the RMS.
	 * @param dos TODO
	 */
	public abstract void serialize(DataOutputStream dos) throws IOException;
	
	/**
	 * This method should do the exact opposite of the {@link Serializable#serialize(DataOutputStream)} method.
	 * i.e. it should take the byte array (data) and be able to construct the Object from that.
	 * 
	 * @param dis the data in serialized form.
	 */
	public abstract void unserialize(DataInputStream dis) throws IOException;
	
}
