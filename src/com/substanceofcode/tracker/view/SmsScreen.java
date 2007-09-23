package com.substanceofcode.tracker.view;

import java.util.NoSuchElementException;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import com.substanceofcode.bluetooth.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Waypoint;

/**
 * <p>The SmsScreen is the form used to send an SMS with GPS information in it.</p>
 * 
 * 
 * 
 * @author Barry Redmond
 */
public class SmsScreen extends Form implements CommandListener, ItemStateListener, ItemCommandListener {

    private static final String DEFAULT_MESSAGE = "This is where I am now!";
    
    private static final int NO_PORT = -1;

    private static final int CURRENT_POSITION = 0;
    private static final int END_OF_TRAIL = 1;
    private static final int EXISTING_WAYPOINT = 2;
    private static final int NEW_WAYPOINT = 3;
    
    private static final int TEXT_MESSAGE = 0;
    private static final int MTE_MESSAGE = 1;

    private final ChoiceGroup positionType;
    private final StringItem waypointNameText;
    private final TextField latField;
    private final TextField lonField;
    private final TextField altField;
    private final TextField privateMessageField;
    private final ChoiceGroup typeChoice;
    private final TextField recipientField;
    private final StringItem finalMessageText;

    private final Command sendCommand;
    private final Command cancelCommand;
    private final Command nextWaypointCommand;
    private final Command previousWaypointCommand;
    
    private int currentWaypointIndex;

    public SmsScreen() {
        super("SMS");
        
        positionType = new ChoiceGroup("Position", ChoiceGroup.EXCLUSIVE);
        positionType.append("Current Position", null);
        positionType.append("End Of Current Trail", null);
        positionType.append("Existing Waypoint", null);
        positionType.append("New Waypoint", null);
        positionType.setSelectedIndex(0, true);
        
        waypointNameText = new StringItem("Waypoint", "");
        waypointNameText.addCommand(nextWaypointCommand = new Command("Next Waypoint", Command.OK, 2));
        waypointNameText.addCommand(previousWaypointCommand = new Command("Previous Waypoint", Command.ITEM, 3));
        waypointNameText.setDefaultCommand(nextWaypointCommand);
        waypointNameText.setItemCommandListener(this);
        
        currentWaypointIndex = 0;
        
        latField = new TextField("Lat", "0.0", 15, TextField.DECIMAL);
        lonField = new TextField("Long", "0.0", 15, TextField.DECIMAL);
        altField = new TextField("Altitude", "0.0", 15, TextField.DECIMAL);

        privateMessageField = new TextField("Your Message", DEFAULT_MESSAGE,
                90, TextField.ANY);

        typeChoice = new ChoiceGroup("SMS Type", ChoiceGroup.EXCLUSIVE);
        typeChoice.append("Text Message", null);
        // FIXME: implement&reinstate: typeChoice.append("MTE Program Message",
        // null);
        typeChoice.setSelectedIndex(TEXT_MESSAGE, true);

        recipientField = new TextField("Recipient", "", 20,
                TextField.PHONENUMBER);

        finalMessageText = new StringItem("Message To Be Sent", "");

        this.setItemStateListener(this);

        this.refresh();
        
        this.addCommand(sendCommand = new Command("Send", Command.OK, 0));
        this.addCommand(cancelCommand = new Command("Cancel", Command.BACK, 1));
        this.setCommandListener(this);

    }
    
    private void refresh(){
        this.refreshItems();
        this.refreshMessage();
    }

    private void refreshItems() {
        this.deleteAll();
        // Don't need this until more than one choice implemented
        // this.append(typeChoice);
        this.append(positionType);
        final int position = positionType.getSelectedIndex();
        if(position == EXISTING_WAYPOINT){
            refreshWaypointName();
            this.append(waypointNameText);
        }else if(position == NEW_WAYPOINT){
            this.append(latField);
            this.append(lonField);
            this.append(altField);
        }
        switch(typeChoice.getSelectedIndex()){
            case(TEXT_MESSAGE):
                this.privateMessageField.setLabel("Private Message");
                this.privateMessageField.setMaxSize(90);
                break;
            case(MTE_MESSAGE):
                this.privateMessageField.setLabel("Waypoint Name");
                this.privateMessageField.setMaxSize(50);
                break;
        }
        this.append(privateMessageField);
        this.append(recipientField);
        if (this.typeChoice.getSelectedIndex() == TEXT_MESSAGE) {
            this.append(finalMessageText);
        }
    }
    
    private void refreshWaypointName(){
        String waypointName;
        try{
            waypointName = ((Waypoint)Controller.getController().getWaypoints().elementAt(this.currentWaypointIndex)).getName();
        }catch(IndexOutOfBoundsException e){
            waypointName = "No Waypoints Found";
        }
        this.waypointNameText.setText(waypointName);
    }

    public void commandAction(Command command, Displayable disp) {
        if (command == sendCommand) {
            // Put the IO operations inside a thread, so that the UI doesn't freeze.
            new Thread(){
                public void run(){
                    try{
                        if (typeChoice.getSelectedIndex() == TEXT_MESSAGE) {
                            refreshMessage();
                            sendTextMessage(recipientField.getString(), finalMessageText.getText());
                        } else if (typeChoice.getSelectedIndex() == MTE_MESSAGE) {
    
                        } else {
                            // Should never reach here!
                            Logger.getLogger().log(
                                    "Neither of the only 2 choices are selected. Crazy! class="
                                            + this.getClass().getName(), Logger.ERROR);
                            Controller.getController().showError(
                                            "Message not sent because of some ERROR!, See log for details");
                        }
                    }catch(Throwable t){
                        Logger.getLogger().log("Error trying to do SmsScreen.commandAction(..., sendCommand)", Logger.ERROR);
                    }
                }
            }.start();
        } else if (command == cancelCommand) {
            this.goBack();
        } 
    }

    private void goBack() {
        Controller.getController().showSettings();
    }

    /* Send text message */
    public void sendTextMessage(String phoneNumber, String message) {
        sendTextMessage(phoneNumber, NO_PORT, message);
    }

    public void sendTextMessage(final String phoneNumber, final int port,
            String message) {
        final String URI = "sms://" + phoneNumber
                + (port == NO_PORT ? "" : ":" + port);
        try {
            // creates a new TextMessage
            MessageConnection messageConnection = (MessageConnection) Connector
                    .open(URI);
            TextMessage textMessage = (TextMessage) messageConnection
                    .newMessage(MessageConnection.TEXT_MESSAGE, URI);
            textMessage.setPayloadText(message);
            messageConnection.send(textMessage);
            Logger.getLogger().log("Message sent", Logger.DEBUG);
            this.goBack();
        } catch (Exception e) {
            Logger.getLogger()
                    .log("Error sending Text Message: " + e.toString(),
                            Logger.ERROR);
        }
    }
    
    public void itemStateChanged(Item item) {
        if (item == this.positionType) {
            this.refresh();
        } else if (item == this.privateMessageField) {
            this.refreshMessage();
        } else if (item == this.typeChoice) {
            this.refresh();
        } else if (item == this.latField || item == this.lonField || item == this.altField){
            this.refreshMessage();
        }
    }

    private void refreshMessage() {
        GpsPosition pos;
        double lat, lon, alt;
        final int type = positionType.getSelectedIndex();
        switch (type) {
            case (CURRENT_POSITION):
                pos = Controller.getController().getPosition();
                break;
            case (END_OF_TRAIL):
                try{
                    pos = Controller.getController().getTrack().getEndPosition();
                }catch(NoSuchElementException e){
                    pos = null;
                }
                break;
            case (EXISTING_WAYPOINT):
                Waypoint waypoint;
                try{
                    waypoint = (Waypoint)Controller.getController().getWaypoints().elementAt(this.currentWaypointIndex);
                }catch(IndexOutOfBoundsException e){
                    waypoint = null;
                }
                if(waypoint != null){
                    lat = waypoint.getLatitude();
                    lon = waypoint.getLongitude();
                    alt = 0.0;
                    pos = new GpsPosition((short)0, lon, lat, 0, alt, null);
                }else{
                    pos = null;
                }
                break;
            case (NEW_WAYPOINT):
                lat = 0.0;
                lon = 0.0;
                alt = 0.0;
                try{
                    lat = Double.parseDouble(latField.getString());
                }catch(NumberFormatException e){}
                try{
                    lon = Double.parseDouble(lonField.getString());
                }catch(NumberFormatException e){}
                try{
                    alt = Double.parseDouble(altField.getString());
                }catch(NumberFormatException e){}
                pos = new GpsPosition((short)0, lon, lat, 0, alt, null);
                break;
            default:
                // Sould never reach here
                pos = null;
                break;
        }
        lat = 0;
        lon = 0;
        alt = 0;
        if (pos != null) {
            // Lat & Long at 5 decimal places is accurate to better than
            // 2metres.
            lat = (int) (pos.latitude * 100000);
            lat = lat / 100000;
            lon = (int) (pos.longitude * 100000);
            lon = lon  / 100000;
            // Altitude is inaccurate anyway, so we'll only go to 1 decimal
            // Place.
            alt = (int) (pos.altitude * 10);
            alt = alt / 10;
        }
        final String messageText = privateMessageField.getString() + "\n\n"
                + "GPS-Details:\n" + "Lat:" + lat + "\n" + "Long:" + lon + "\n"
                + "Alt:" + alt + "m";
        this.finalMessageText.setText(messageText);
    }

    public void commandAction(Command command, Item item) {
        if(item == this.waypointNameText){
            if(command == nextWaypointCommand){
                this.currentWaypointIndex++;
                if(currentWaypointIndex >= Controller.getController().getWaypoints().size()){
                    currentWaypointIndex = 0;
                }
            }else if(command == previousWaypointCommand){
                this.currentWaypointIndex--;
                if(currentWaypointIndex < 0){
                    currentWaypointIndex = Controller.getController().getWaypoints().size()-1;
                }
                if(currentWaypointIndex < 0){
                    currentWaypointIndex = 0;
                }
            }
            refreshWaypointName();
            this.refreshMessage();
        }
    }

}
