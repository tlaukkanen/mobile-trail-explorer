package com.substanceofcode.tracker.view;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Alert;
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

public class SmsScreen extends Form implements CommandListener, ItemStateListener {

    private static final int NO_PORT = -1;

    private static final int CURRENT_POSITION = 0;
    private static final int END_OF_TRAIL = 1;
    
    private static final int TEXT_MESSAGE = 0;
    private static final int MTE_MESSAGE = 1;

    private final ChoiceGroup positionType;
    private final TextField privateMessageField;
    private final ChoiceGroup typeChoice;
    private final TextField recipientField;
    private final StringItem finalMessageText;

    private final Command sendCommand;
    private final Command cancelCommand;

    public SmsScreen() {
        super("SMS");

        positionType = new ChoiceGroup("Position", ChoiceGroup.EXCLUSIVE);
        positionType.append("Current Position", null);
        positionType.append("End Of Current Trail", null);
        positionType.setSelectedIndex(0, true);

        privateMessageField = new TextField("Your Message", "This is where I am now!",
                90, TextField.ANY);

        typeChoice = new ChoiceGroup("SMS Type", ChoiceGroup.EXCLUSIVE);
        typeChoice.append("Text Message", null);
        // FIXME: implement&reinstate: typeChoice.append("MTE Program Message",
        // null);
        typeChoice.setSelectedIndex(TEXT_MESSAGE, true);

        recipientField = new TextField("Recipient", "", 20,
                TextField.PHONENUMBER);

        finalMessageText = new StringItem("Message To Be Sent", "");
        this.refreshMessage();

        this.addCommand(sendCommand = new Command("Send", Command.OK, 0));
        this.addCommand(cancelCommand = new Command("Cancel", Command.BACK, 1));
        this.setCommandListener(this);
        
        this.setItemStateListener(this);

        this.refreshItems();

    }

    private void refreshItems() {
        this.deleteAll();
        this.append(positionType);
        if (this.typeChoice.getSelectedIndex() == TEXT_MESSAGE) {
            this.privateMessageField.setLabel("Private Message");
            this.privateMessageField.setMaxSize(90);
        }else{
            this.privateMessageField.setLabel("Waypoint Name");
            this.privateMessageField.setMaxSize(50);
        }
        this.append(privateMessageField);
        this.append(typeChoice);
        this.append(recipientField);
        if (this.typeChoice.getSelectedIndex() == TEXT_MESSAGE) {
            this.append(finalMessageText);
        }
    }

    public void commandAction(Command command, Displayable disp) {
        if (command == sendCommand) {
            if (typeChoice.getSelectedIndex() == TEXT_MESSAGE) {
                this.refreshMessage();
                sendTextMessage(recipientField.getString(), this.finalMessageText.getText());
            } else if (typeChoice.getSelectedIndex() == MTE_MESSAGE) {

            } else {
                // Should never reach here!
                Logger.getLogger().log(
                        "Neither of the only 2 choices are selected. Crazy! class="
                                + this.getClass().getName(), Logger.ERROR);
                Controller.getController().showError(
                                "Message not sent because of some ERROR!, See log for details",
                                Alert.FOREVER, Controller.getController().getCurrentScreen());
            }

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
            this.refreshItems();
            this.refreshMessage();
        } else if (item == this.privateMessageField) {
            this.refreshMessage();
        } else if (item == this.typeChoice) {
            this.refreshItems();
        }
    }

    private void refreshMessage() {
        GpsPosition pos = null;
        final int type = positionType.getSelectedIndex();
        switch (type) {
            case (CURRENT_POSITION):
                pos = Controller.getController().getPosition();
                break;
            case (END_OF_TRAIL):
                pos = Controller.getController().getTrack().getEndPosition();
                // FIXME: what if no end position
                break;
            default:
                // Sould never reach here
                break;
        }
        double lat = 0;
        double lon = 0;
        double alt = 0;
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

}
