package com.substanceofcode.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {

    /** The MIME type for this class, can/should be used when storing to the file system */
    public String getMimeType();
    
    /**
     * This method should serialize the given file into a sequence of bytes so
     * that it can be easily stored in the RMS.
     * 
     * @param dos The OutputStream to write to.
     */
    public void serialize(DataOutputStream dos) throws IOException;

    /**
     * This method should do the exact opposite of the
     * {@link Serializable#serialize(DataOutputStream)} method. i.e. it should
     * take the byte array (data) and be able to construct the Object from that.
     * 
     * @param dis the InputStream to read the data from.
     */
    public void unserialize(DataInputStream dis) throws IOException;

}
