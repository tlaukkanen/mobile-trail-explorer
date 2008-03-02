package com.substanceofcode.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import com.substanceofcode.tracker.view.Logger;



/**
 * <p>A file-system overlay for the RMS</p>
 * 
 * This class allows 'files' to be written to the RMS, and retrieved via a 'filename'.<br>
 * A 'file' can be any class which implements the Serializable interface.<br>
 * This class deals with all the issues of space, and spliting up files into smaller chunks,
 * making sure there is room in the RMS etc.<br>
 * <br>
 * Information stored in the FILE INDEX will be as follows:<br>
 * <ul>
 *   <li>Number of files (int)
 *   <li>Current RecordStore Number.
 *   <li>Number of records stored in current RecordStore.
 *   <li>File information[S]
 *   <ul>
 *     <li>File name (UTF)
 *     <li>File location (Serialized File locator)
 *   </ul>
 * </ul>
 * 
 * @author Barry
 */

public class FileSystem{

	private static final int RMS_RECORD_SIZE = 50000;
	private static final int RMS_NUMBER_RECORDS = 150000;
	private static final int RMS_NUMBER_STORES = 2;
	
	
	private static final String RMS_FILE_INDEX ="rms_files_index";
	
	private static FileSystem fileSystem = null;
	
	/**
         * A hashtable from 'String' (filename) to 'Filelocator'. 
	 */
	private Hashtable fileTable;
	private int currentRecordStoreNumber;
	private int recordsInCurrentRecordStore;
	
	
	
	private FileSystem(){
		try {
			RecordStore indexRS = RecordStore.openRecordStore(RMS_FILE_INDEX, false);
			// Records exist already.
			RecordEnumeration re = indexRS.enumerateRecords(null, null, false);
			if(re.hasNextElement()){
			byte[] data = re.nextRecord();
			DataInputStream dos = new DataInputStream(new ByteArrayInputStream(data));
			int numberOfFiles = dos.readInt();
			currentRecordStoreNumber = dos.readInt();
			recordsInCurrentRecordStore = dos.readInt();
			fileTable = new Hashtable(numberOfFiles);
			String key;
			FileLocator fl;
			for(int i = 0; i < numberOfFiles; i++){
				key = dos.readUTF();
				fl = new FileLocator(dos);
				fileTable.put(key, fl);
			}
			indexRS.closeRecordStore();
			dos.close();
		}
		} catch (RecordStoreNotFoundException e) {
			// Records DO NOT exist already
			currentRecordStoreNumber = 1;
			recordsInCurrentRecordStore = 0;
			fileTable = new Hashtable();
			//e.printStackTrace();
		} catch (RecordStoreException e) {
		    //either of these exceptions will mean the fileTable
		    //is not initialized, causing problems later
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
		    e.printStackTrace();
		}
	}
	
	/*
	 * PUBLIC METHODS
	 */
	
	public static FileSystem getFileSystem(){
		if(fileSystem == null){
			fileSystem = new FileSystem();
		}
		return fileSystem;
	}

        public void saveFile(String filename, Serializable file, boolean overwrite) throws FileIOException{
            this.saveFile(filename, file.getMimeType(), file, overwrite);
        }
	/**
	 * Saves the specified 'file' to the RMS/FileSystem.
	 * 
	 * @param filename The name of the file to save.
	 * @param mimeType The type of file, (can be any string if mimeType is not appropriate for the applicaiton)
	 * @param file The 'file' to write. 
	 * @param overwrite If true, will overwrite any previously saved file with that name, if false will throw a FileIOException if there is a file of that name already in the system.
	 * @throws FileIOException If 'overwrite' is false and a file with the specified 'filename' already exists in the FileSystem. OR if any other error occurs when saving the data.
	 */
	public void saveFile(String filename, String mimeType, Serializable file, boolean overwrite) throws FileIOException{
		if(this.fileTable.containsKey(filename)){
			if(!overwrite){
				throw new FileIOException("File " + filename + " already exists. Specify a different name, or assert 'overwrite'");
			}else{
				this.deleteFile(filename);
			}
		}		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		byte[] data = null;
		try {
			file.serialize(dos);
			data = baos.toByteArray();
			dos.close();
			baos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new FileIOException("Error serializing file:- " + e1.getMessage());
		}
		int bytesWritten = 0;
		int numRecords = data.length /  RMS_RECORD_SIZE + (data.length%RMS_RECORD_SIZE == 0?0:1);
		String[] recordStores = new String[numRecords];
		int[] records = new int[numRecords];
		try {
			RecordStore rs = RecordStore.openRecordStore(Integer.toString(currentRecordStoreNumber), true);
			for(int i = 0; i < numRecords; i++){

				if(this.recordsInCurrentRecordStore >= RMS_NUMBER_RECORDS){
					rs.closeRecordStore();
					this.currentRecordStoreNumber++;
					if(this.currentRecordStoreNumber > RMS_NUMBER_STORES){
						throw new FileIOException("Out of space, unable to save " + filename);
					}
					this.recordsInCurrentRecordStore = 0;
					rs = RecordStore.openRecordStore(Integer.toString(currentRecordStoreNumber), true);
				}

				recordStores[i] = Integer.toString(currentRecordStoreNumber);
				records[i] = rs.addRecord(data, bytesWritten, i >= data.length /  RMS_RECORD_SIZE?data.length%RMS_RECORD_SIZE:RMS_RECORD_SIZE);
				bytesWritten += RMS_RECORD_SIZE;
			}
			rs.closeRecordStore();
			FileLocator fl = new FileLocator(filename, mimeType, data.length, recordStores, records);
			fileTable.put(filename, fl);
			this.writeFileTableToRMS();
		} catch (RecordStoreException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Returns the file (as a DataInputStream) specified by the 'filname'
	 * 
	 * @param filename the file to return.
	 * @return the file specified by the filname, as a byte array.
	 * @throws FileIOException if the filename doesn't exist, or other IO problems occur.
	 */
	public DataInputStream getFile(String filename) throws FileIOException{
		// Make sure the file exists in our table of files.
		if(!fileTable.containsKey(filename)){
			throw new FileIOException("File '" + filename + "' does not exist");
		}
		byte[] resultArray;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FileLocator fl = (FileLocator)(fileTable.get(filename));
			String recordStoreName = fl.recordStores[0];
			RecordStore recordStore = RecordStore.openRecordStore(recordStoreName, false);
			for(int i = 0; i < fl.recordStores.length; i++){
				if(recordStoreName != fl.recordStores[i]){
					recordStoreName = fl.recordStores[i];
					recordStore.closeRecordStore();
					RecordStore.openRecordStore(fl.recordStores[i], false);
				}
				byte[] nextData = recordStore.getRecord(fl.recordNumbers[i]);
				baos.write(nextData);
			}
			recordStore.closeRecordStore();
			resultArray = baos.toByteArray();
			baos.close();
		} catch (RecordStoreException e) {
			throw new FileIOException(e.getMessage());
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		} 
		
		return new DataInputStream(new ByteArrayInputStream(resultArray));
	}
	
	/**
	 * Returns a Vector of Strings corrosponding to the filenames stored in this
	 * FileSystem.
	 * 
	 * @return a Vector of Strings corrosponding to the filenames stored in this FileSystem.
	 */
	public Vector /*String*/ listFiles(){
	    Logger.debug("fileTable is "+fileTable);
		Enumeration keysEnumeration = fileTable.keys();
		Vector keysVector = new Vector();
		while(keysEnumeration.hasMoreElements()){
		    keysVector.addElement(keysEnumeration.nextElement());
		}
		return keysVector;
	}
        
        /**
         * Returns a Vector of Strings corrosponding to the filenames of all 'files' with a 'mimeType' that
         * matches 'fileType'
         * 
         * @param fileType The type of files to get.
         * 
         * @return a Vector of Strings corrosponding to the filenames of all 'files' with a 'mimeType' that
         * matches 'fileType'
         */
        public Vector /*String*/ listFiles(String fileType){
            Enumeration keysEnumeration = fileTable.keys();
            Vector keysVector = new Vector();
            while(keysEnumeration.hasMoreElements()){
                // Don't bother casting to a String as it's not necessary
                final Object key = keysEnumeration.nextElement();
                final FileLocator fl = (FileLocator)fileTable.get(key);
                if(fl.mimeType.equals(fileType)){
                    keysVector.addElement(key);
                }
            }
            return keysVector;
        }
	
	public boolean containsFile(String filename) {
	    if(fileTable !=null){
		return fileTable.containsKey(filename);
	    }else{
	        Logger.error("fileTable was null!!");
	        return false;
	    }
	}
        
        /**
         * Renames the file with the title 'origionalName' to 'newName'
         * @param origionalName the file to rename
         * @param newName the new name for the file.
         * @throws FileIOException if there is a problem writing the revised name to the RMS.
         */
        public void renameFile(String origionalName, String newName) throws FileIOException{
            if(fileTable.containsKey(origionalName)){
                final FileLocator fl = (FileLocator)fileTable.get(origionalName);
                final FileLocator newFl = new FileLocator(newName, fl.mimeType, fl.size, fl.recordStores, fl.recordNumbers);
                fileTable.remove(origionalName);
                fileTable.put(newName, newFl);

                this.writeFileTableToRMS();
            }
        }
	
	/**
	 * <p>Deletes the file with the specified filename from the FileSystem. </p>
	 * Deletes any empty RecordStores this action causes along the way.
	 * 
	 * @param filename the file to delete
	 * @throws FileIOException if the filename doesn't exist in the filesystem, or other IO problems occur.
	 */
	public void deleteFile(String filename) throws FileIOException{
		// Make sure the file exists in our table of files.
		if(!fileTable.containsKey(filename)){
			throw new FileIOException("File '" + filename + "' does not exist");
		}
		try {
			FileLocator fl = (FileLocator)(fileTable.get(filename));
			String recordStoreName = fl.recordStores[0];
			RecordStore recordStore = RecordStore.openRecordStore(recordStoreName, false);
			for(int i = 0; i < fl.recordStores.length; i++){
				if(recordStoreName != fl.recordStores[i]){
					recordStoreName = fl.recordStores[i];
					recordStore.closeRecordStore();
					RecordStore.openRecordStore(fl.recordStores[i], false);
				}
				recordStore.deleteRecord(fl.recordNumbers[i]);
			}
			if(recordStore.getSize() == 0){
				recordStore.closeRecordStore();
				RecordStore.deleteRecordStore(recordStoreName);
			}
			recordStore.closeRecordStore();
			fileTable.remove(filename);
			writeFileTableToRMS();
		} catch (RecordStoreException e) {
			throw new FileIOException(e.getMessage());
		} 
	}

	  /**
         * Delete all the files of the specified type from the 'filesystem'
         * @param type the type of files to delete
         */
        public void deleteFiles(String type){
            Vector deleteList=listFiles(type);
            while(deleteList.size()>0){
                String fileToDelete= (String)deleteList.firstElement();
                deleteList.removeElementAt(0);
                try {
                    deleteFile(fileToDelete);
                } catch (FileIOException e) {
                    Logger.error("Could not delete file "+fileToDelete);
                    e.printStackTrace();
                }
                
            }
        }
      
        
	
	/**
	 * <p>Formats the filesystem, deleting ALL files, and ALL RecordStores along the way.</p>
	 * 
	 * This is a very extreme and will delete ALL information stored in the RMS, use with care.
	 * 
	 * @throws FileIOException if there is a problem deleting any of the RecordStores.
	 */
	public void formatFileSystem() throws FileIOException {
		try{
			String[] stores = RecordStore.listRecordStores();
			for(int i = 0; i < stores.length; i++){
				RecordStore.deleteRecordStore(stores[i]);
			}
			fileTable = new Hashtable();
			currentRecordStoreNumber = 1;
			recordsInCurrentRecordStore = 0;
			this.writeFileTableToRMS();
		} catch (RecordStoreException e) {
			throw new FileIOException(e.getMessage());
		}	
	}
	
	private void writeFileTableToRMS() throws FileIOException{
		try{
			RecordStore indexRS = RecordStore.openRecordStore(RMS_FILE_INDEX, true);
			// Delete all current records, (to be replaced later in this method.
			RecordEnumeration re = indexRS.enumerateRecords(null, null, false);
			while(re.hasNextElement()){
				indexRS.deleteRecord(re.nextRecordId());
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			//int numberOfFiles
			dos.writeInt(fileTable.size());
			//currentRecordStoreNumber
			dos.writeInt(currentRecordStoreNumber);
			//recordsInCurrentRecordStore
			dos.writeInt(recordsInCurrentRecordStore);
	
			Enumeration keys = fileTable.keys();
			for(int i = 0; i < fileTable.size(); i++){
				String currentKey = (String)keys.nextElement();
				dos.writeUTF(currentKey);
				FileLocator fl = (FileLocator)fileTable.get(currentKey);
				fl.writeFileLocator(dos);
			}
			byte[] data = baos.toByteArray();
			indexRS.addRecord(data, 0, data.length);
			indexRS.closeRecordStore();
			dos.close();
			baos.close();
		} catch (RecordStoreException e) {
			e.printStackTrace();
			throw new FileIOException("Unable to write File-Table to RMS.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileIOException("Unable to write File-Table to RMS.");
		}
	}


}
