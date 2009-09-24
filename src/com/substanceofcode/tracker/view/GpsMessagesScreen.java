/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.substanceofcode.tracker.view;

import com.substanceofcode.bluetooth.BluetoothGPSDeviceImpl;
import com.substanceofcode.bluetooth.Device;
import com.substanceofcode.data.FileSystem;
import com.substanceofcode.data.Serializable;
import com.substanceofcode.gpsdevice.Jsr179Device;
import com.substanceofcode.tracker.controller.Controller;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Screen;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author gareth
 */
public class GpsMessagesScreen extends List implements CommandListener {

    public static final String EDITMODE="EDIT";
    public static final String COPYMODE="COPY";
    public static final String NEWMODE="NEW";
    private Screen previous;
    private final Command backCommand;
    private final Command sendCommand;
    private final Command addCommand;
    private final Command editCommand;
    private final Command copyCommand;
    private final Command deleteCommand;
    private final Command helpCommand;
    final StringBuffer addMessage = new StringBuffer();
    GPSMessages gpsMessages;

    public GpsMessagesScreen() {
        super("Gps Messages", List.MULTIPLE);
        addCommand(backCommand = new Command("Back", Command.BACK, 1));
        addCommand(addCommand = new Command("Add", Command.ITEM, 1));
        addCommand(editCommand = new Command("Edit", Command.ITEM, 1));
        addCommand(copyCommand = new Command("Copy", Command.ITEM, 1));
        addCommand(deleteCommand = new Command("Delete", Command.ITEM, 1));
        addCommand(sendCommand = new Command("Send", Command.SCREEN, 1));
        addCommand(helpCommand = new Command("Help", Command.SCREEN, 1));
        setSelectCommand(sendCommand);
        setCommandListener(this);

        initialize();
    }

    //Read the messages from the rms and append to the list
    public void initialize() {
   		Logger.debug("Initializing GpsMessages");
        try{
        deleteAll();
        gpsMessages = new GPSMessages();
        unserializeMessages(gpsMessages);
        Enumeration keys = gpsMessages.keys();
        while (keys.hasMoreElements()) {
            append((String) keys.nextElement(), null);
        }
        }catch(Exception e){
            Logger.error("GpsMessages Init: "+e.getMessage());
        }
    }

    public void setPreviousScreen(Screen screen) {
        previous = screen;
    }

    public void commandAction(Command c, Displayable d) {
        Controller controller = Controller.getController();

        gpsMessages = new GPSMessages();
        unserializeMessages(gpsMessages);

        boolean selected[] = new boolean[size()];
        getSelectedFlags(selected);


        if (c == backCommand) {
            controller.showDisplayable(previous);
        }
        if (c == addCommand) {
            handleMessage();
        }
        if (c == editCommand) {//think we can only do one at a time....
            for (int i = 0; i < size(); i++) {
                if (selected[i]) {
                    handleMessage(getString(i),GpsMessagesScreen.EDITMODE);
                }
            }
        }
        if (c == copyCommand) {//think we can only do one at a time....
            for (int i = 0; i < size(); i++) {
                if (selected[i]) {
                    handleMessage(getString(i),GpsMessagesScreen.COPYMODE);
                }
            }

        }
        if (c == deleteCommand) {
            //1 so we don't delete the add option ...
            for (int i = 0; i < size(); i++) {
                if (selected[i]) {
                    gpsMessages.remove(getString(i));
                }
            }
            serializeMessages(gpsMessages);
            initialize();
        }

        if (c == sendCommand) {
                for (int i = 0; i < size(); i++) {
                    if (selected[i]) {
                        Send(createNMEAMessage((String) gpsMessages.get(getString(i))));
                        //Set back to unselected
                        setSelectedIndex(i, false);
                    }
                }
         //       outputStrem.flush();
                initialize();
        }
         if (c == helpCommand) {
            showHelp();
        }
	}


    private void showHelp(){
        GpsMessagesHelp gpsMessagesHelp=new GpsMessagesHelp(this);
        Controller.getController().getDisplay().setCurrent(gpsMessagesHelp);
    }


    private void Send(String msg) {
        OutputStream outputStream;
        Logger.debug("Sending Message");
        Device dev = Controller.getController().getGpsDevice();
        if (dev == null ||  dev instanceof Jsr179Device) {
			Logger.debug("GPSMessages: No external device detected");
            return;
        }
        outputStream = ((BluetoothGPSDeviceImpl) dev).outputStream;
        if( outputStream == null){
            Logger.debug("GPSMessages: No output stream detected");
            return;
        }
        Logger.debug("outputstream is "+outputStream);
        Logger.debug("Sending this [" + msg + "] to this " + outputStream.toString());
        try {
            outputStream.write(msg.getBytes());
            outputStream.flush(); //does nothing according to javadoc
        } catch (IOException ioe) {
            Logger.debug("IOE: " + ioe.getMessage());
        }
    }

    static void serializeMessages(GPSMessages gpsMessages) {
        //If all records have been deleted, remove the rms file
        //If a zero length file is saved to RMS, it errors when reloading later.
        try {
            if(gpsMessages.size()==0){
                FileSystem.getFileSystem().deleteFile("messages");
            }else{
                FileSystem.getFileSystem().saveFile("messages", "Message", gpsMessages, true);
            }
        } catch (Exception ex) {
            Logger.error("File error: " + ex.getMessage());
        }
    }

    static void unserializeMessages(GPSMessages gpsMessages) {
        try {
            if (FileSystem.getFileSystem().containsFile("messages")) {
                gpsMessages.unserialize(FileSystem.getFileSystem().getFile("messages"));
            }
        } catch (IOException ioe) {
            Logger.error("IOE:" + ioe.getMessage());
        }
    }

    /**
     * Creates a valid NMEA sentence given the payload.
     * IE Will output a string like: '$'+payload+checksum+crlf
     * @param sentence the payload to turn into an nmea message
     * @return String suitable for sending to a gps receiver
     */
    public static String createNMEAMessage(String sentence) {
        // Calculate the checksum for the sentence.
        // A NMEA checksum is calculated as the XOR of the bytes.
        byte[] input = sentence.getBytes();
        int checksum = 0;

        for (int i = 0; i < input.length; i++) {
            checksum ^= input[i];
        }

        String hexChecksum = Integer.toHexString(checksum);
        hexChecksum = hexChecksum.toUpperCase();  // Turn a-f into A-F

        // Package the sentence.
        StringBuffer buffer = new StringBuffer();
        buffer.append('$');
        buffer.append(sentence);
        buffer.append('*');
        buffer.append(hexChecksum);
        buffer.append("\r\n");
        String packagedSentence = buffer.toString();

        // Send the sentence.
        //Logger.debug("Sending this [" + packagedSentence + "]");
        return packagedSentence;
    }

    //Create a form with an entry field to allow user to enter whatever they like.
    //Then add a checksum and send the message
    private void handleMessage() {
        handleMessage("", NEWMODE);
    }

    private void handleMessage(String message,String mode) {
        Form f = new MessageEditor(message, this,mode);
        Controller.getController().getDisplay().setCurrent(f);
    }
}


class MessageEditor extends Form implements CommandListener {

    String message;
    GpsMessagesScreen previous;
    Command backCommand;
    Command okCommand;
    TextField messageNameField;
    TextField messageField;
    boolean editMode = false;
    boolean copyMode = false;
    GPSMessages gpsMessages = new GPSMessages();

    public MessageEditor(String message, Object previous,final String mode) {
        super("Other Message");
        this.message = message;
        this.previous = (GpsMessagesScreen) previous;
        addCommand(backCommand = new Command("Back", Command.BACK, 1));
        addCommand(okCommand = new Command("Ok", Command.OK, 1));
        //Recreate the messages from the RMS
        GpsMessagesScreen.unserializeMessages(gpsMessages);

        messageNameField = new TextField(
                "Enter a name for this message",
                "",
                80,
                TextField.ANY & TextField.NON_PREDICTIVE);

        messageField = new TextField(
                "Enter a gps string without the $, checksum, or CRLF",
                "",
                80,
                TextField.ANY & TextField.NON_PREDICTIVE);
        messageField.setInitialInputMode("MIDP_UPPERCASE_LATIN");

        //If message is not empty we are editing, so delete the current entry
        if(!"".equals(message)) {
            messageNameField.setString(message);
            messageField.setString((String) gpsMessages.get(message));
        }

        if (GpsMessagesScreen.EDITMODE.equals(mode) ){
            editMode = true;
        }
        if (GpsMessagesScreen.COPYMODE.equals(mode) ){
            copyMode = true;
        }

        append(messageNameField);
        append(messageField);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == okCommand) {
			String name=messageNameField.getString();
			String value= messageField.getString().toUpperCase();
			if(!"".equals(name) && !"".equals(value))
                gpsMessages.put(name, value);
            //if editing and the key (name) has changed, remove the old version now.
            if (editMode && !messageNameField.getString().equals(message)) {
                gpsMessages.remove(message);
            }
            GpsMessagesScreen.serializeMessages(gpsMessages);
            previous.initialize();

        }
        Controller.getController().getDisplay().setCurrent(previous);
    }

}
/**
 * GPSMessages is a serializable hashmap corresponding to user defined
 * gps messages.
 * @author gareth
 */
class GPSMessages extends Hashtable implements Serializable {

    public String getMimeType() {
        return "";//throw new UnsupportedOperationException("Not supported yet.");
    }

    public void serialize(DataOutputStream dos) throws IOException {
        String key = "";
        String value = "";
        Enumeration keys = keys();
        while (keys.hasMoreElements()) {
            key = ((String) keys.nextElement());
            value = (String) get(key);

            //writeUTF doesn't work...
            byte[] keyBytes = key.getBytes();
            dos.writeShort(keyBytes.length);
            dos.write(keyBytes);

            byte[] valueBytes = value.getBytes();
            dos.writeShort(valueBytes.length);
            dos.write(valueBytes);
        }
    }

    public void unserialize(DataInputStream dis) throws IOException {
        String key = "";
        String value = "";

        while (dis.available() > 0) {
            short len = dis.readShort();
            byte[] bytes = new byte[len];
            dis.read(bytes, 0, len);
            key = new String(bytes);

            len = dis.readShort();
            bytes = new byte[len];
            dis.read(bytes, 0, len);
            value = new String(bytes);
            put(key, value);
        }

    }
}

class GpsMessagesHelp extends Form implements CommandListener{

    private Command okCommand;
    private final Displayable previous;
    public GpsMessagesHelp(Displayable previous){
        super("GPS Messages Help");
        okCommand = new Command("Ok", Command.OK, 1);
        append("Use this page to send commands to the GPS receiver.\n" +
                "Only use this if you know what you are doing as it is " +
                "possible to break the device if you make a mistake!\n" +
                "Commands are saved to the RMS and can be edited,copied and " +
                "deleted.\nSeveral commands can be selected and sent to the " +
                "Gps device at once.\nNote: editing and copying can only be performed " +
                "on one record at a time.");
        addCommand(okCommand);
        this.previous=previous;
        setCommandListener(this);
    }
        public void commandAction(Command c, Displayable d) {
          if(c==okCommand)
               Controller.getController().setCurrentScreen(previous);
    }

}


