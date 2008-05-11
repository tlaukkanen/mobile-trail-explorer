package com.substanceofcode.tracker.view;

import java.util.NoSuchElementException;

import javax.microedition.io.Connector;
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

import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Place;

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
    private static final int EXISTING_PLACE = 2;
    private static final int NEW_PLACE = 3;
    
    private static final int TEXT_MESSAGE = 0;
    private static final int MTE_MESSAGE = 1;

    private final ChoiceGroup positionType;
    private final StringItem placeNameText;
    private final TextField latField;
    private final TextField lonField;
    private final TextField altField;
    private final TextField privateMessageField;
    private final ChoiceGroup typeChoice;
    private final TextField recipientField;
    private final StringItem finalMessageText;

    private final Command sendCommand;
    private final Command cancelCommand;
    private final Command nextPlaceCommand;
    private final Command previousPlaceCommand;
    
    private int currentPlaceIndex;

    public SmsScreen() {
        super("SMS");
        
        positionType = new ChoiceGroup("Position", ChoiceGroup.EXCLUSIVE);
        positionType.append("Current Position", null);
        positionType.append("End Of Current Trail", null);
        positionType.append("Existing Place", null);
        positionType.append("New Place", null);
        positionType.setSelectedIndex(0, true);
        
        placeNameText = new StringItem("Place", "");
        placeNameText.addCommand(nextPlaceCommand = new Command("Next Place", Command.OK, 2));
        placeNameText.addCommand(previousPlaceCommand = new Command("Previous Place", Command.ITEM, 3));
        placeNameText.setDefaultCommand(nextPlaceCommand);
        placeNameText.setItemCommandListener(this);
        
        currentPlaceIndex = 0;
        
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
        if(position == EXISTING_PLACE){
            refreshPlaceName();
            this.append(placeNameText);
        }else if(position == NEW_PLACE){
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
    
    private void refreshPlaceName(){
        String waypointName;
        try{
            waypointName = ((Place)Controller.getController().getPlaces().elementAt(this.currentPlaceIndex)).getName();
        }catch(IndexOutOfBoundsException e){
            waypointName = "No Waypoints Found";
        }
        this.placeNameText.setText(waypointName);
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
                            Logger.error(
                                    "Neither of the only 2 choices are selected. Crazy! class="
                                            + this.getClass().getName());
                            Controller.getController().showError(
                                            "Message not sent because of some ERROR!, See log for details");
                        }
                    }catch(Throwable t){
                        Logger.error("Error trying to do SmsScreen.commandAction(..., sendCommand)");
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
            Logger.debug("Message sent");
            this.goBack();
        } catch (Exception e) {
            Logger.error("Error sending Text Message: " + e.toString());
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
                Logger.debug("SmsScreen getPosition called");
                pos = Controller.getController().getPosition();
                break;
            case (END_OF_TRAIL):
                try{
                    pos = Controller.getController().getTrack().getEndPosition();
                }catch(NoSuchElementException e){
                    pos = null;
                }
                break;
            case (EXISTING_PLACE):
                Place waypoint;
                try{
                    waypoint = (Place)Controller.getController().getPlaces().elementAt(this.currentPlaceIndex);
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
            case (NEW_PLACE):
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
        if(item == this.placeNameText){
            if(command == nextPlaceCommand){
                this.currentPlaceIndex++;
                if(currentPlaceIndex >= Controller.getController().getPlaces().size()){
                    currentPlaceIndex = 0;
                }
            }else if(command == previousPlaceCommand){
                this.currentPlaceIndex--;
                if(currentPlaceIndex < 0){
                    currentPlaceIndex = Controller.getController().getPlaces().size()-1;
                }
                if(currentPlaceIndex < 0){
                    currentPlaceIndex = 0;
                }
            }
            refreshPlaceName();
            this.refreshMessage();
        }
    }

}
