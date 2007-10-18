package com.substanceofcode.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * <p>This class encompasses all the information needed to 
 * recover a 'File' blob from the underlying RMS. </p>
 * 
 * i.e. Which RecordStores and which records the file blob is in.<br>
 * It also has some additional anciliary information, such as file name and mime type.
 * 
 * It also contains methods to recover and store a FileLocator to a DataInputStream and DataOutputStream respectively
 * @author Barry
 *
 */
public class FileLocator {

	/**
	 * The size of the file this locator points to. (in bytes)
	 */
	public final int size;
	
	public final String[] recordStores;
	public final int[] recordNumbers;
	
	public final String title;
	public final String mimeType;
	
	
	public FileLocator(String title, String mimeType, int size, String[] stores, int[] numbers){
		/*
		 * Make sure that neither stores, nor numbers are null, and that they are the same length.
		 */
		if(stores == null || numbers == null){
			throw new IllegalArgumentException("Both stores and numbers must NOT be null.");
		}
		if( stores.length != numbers.length ){
			throw new IllegalArgumentException("The stores and numbers must have the same number of elements. (" + stores.length + ") _ (" + numbers.length + ")");
		}
		
		
		this.title = title;
		this.mimeType = mimeType;
		this.size = size;
		
		this.recordStores = new String[stores.length];
		for(int i = 0; i < stores.length; i++){
			recordStores[i] = stores[i];
		}
		this.recordNumbers = new int[numbers.length];
		for(int i = 0; i < numbers.length; i++){
			recordNumbers[i] = numbers[i];
		}
	}
	
	public FileLocator(DataInputStream dis) throws IOException{
		this.title = dis.readUTF();
		this.mimeType = dis.readUTF();
		this.size = dis.readInt();
		
		int arraySize = dis.readInt();
		this.recordStores = new String[arraySize];
		for(int i = 0; i < arraySize; i++){
			recordStores[i] = dis.readUTF();
		}
		this.recordNumbers = new int[arraySize];
		for(int i = 0; i < arraySize; i++){
			recordNumbers[i] = dis.readInt();
		}
	}
	
	public void writeFileLocator(DataOutputStream dos) throws IOException{
		dos.writeUTF(title);
		dos.writeUTF(mimeType);
		dos.writeInt(size);
		
		dos.writeInt(recordStores.length);
		for(int i = 0; i < recordStores.length; i++){
			dos.writeUTF(recordStores[i]);
		}
		for(int i = 0; i < recordNumbers.length; i++){
			dos.writeInt(recordNumbers[i]);
		}
	}
	
}
