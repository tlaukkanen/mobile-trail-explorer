/*
 * TrailRecorderVisualMidlet.java
 *
 * Created on 7. elokuuta 2006, 16:54
 */

package com.substanceofcode.tracker.view;

import com.substanceofcode.bluetooth.BluetoothDevice;
import com.substanceofcode.tracker.controller.Controller;
import java.util.Vector;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 *
 * @author Tommi
 */
public class TrailRecorderVisualMidlet extends MIDlet implements CommandListener {
    
    /** Controller */
    private Controller m_controller;
    
    /** Creates a new instance of TrailRecorderVisualMidlet */
    public TrailRecorderVisualMidlet() {
        m_controller = new Controller(this);
        initialize();
    }
    
    private org.netbeans.microedition.lcdui.SplashScreen splashScreen;//GEN-BEGIN:MVDFields
    private com.substanceofcode.tracker.view.TrailCanvas trailCanvas;
    private Command aboutBackCommand;
    private Command aboutCommand;
    private Command deviceListCommand;
    private Command deviceOkCommand;
    private Command deviceCancelCommand;
    private Command exitCommand1;
    private Command startCommand;
    private Image splashImage;
    private Image image1;
    private Form aboutForm;
    private Command backCommand1;
    private com.substanceofcode.tracker.view.DeviceList deviceList;
    private Command okCommand1;
    private Command cancelCommand1;//GEN-END:MVDFields
    
//GEN-LINE:MVDMethods

    /** Called by the system to indicate that a command has been invoked on a particular displayable.//GEN-BEGIN:MVDCABegin
     * @param command the Command that ws invoked
     * @param displayable the Displayable on which the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:MVDCABegin
    // Insert global pre-action code here
        if (displayable == trailCanvas) {//GEN-BEGIN:MVDCABody
            if (command == exitCommand1) {//GEN-END:MVDCABody
                // Insert pre-action code here
                exitMIDlet();//GEN-LINE:MVDCAAction19
                // Insert post-action code here
            } else if (command == aboutCommand) {//GEN-LINE:MVDCACase19
                // Insert pre-action code here
                getDisplay().setCurrent(get_aboutForm());//GEN-LINE:MVDCAAction11
                // Insert post-action code here
            } else if (command == startCommand) {//GEN-LINE:MVDCACase11
                // Insert pre-action code here
                m_controller.startStop();
               
                getDisplay().setCurrent(get_trailCanvas());//GEN-LINE:MVDCAAction21
                
                
                // Insert post-action code here
            } else if (command == deviceListCommand) {//GEN-LINE:MVDCACase21
                // Insert pre-action code here
                getDisplay().setCurrent(get_deviceList());//GEN-LINE:MVDCAAction13
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase13
        } else if (displayable == aboutForm) {
            if (command == backCommand1) {//GEN-END:MVDCACase13
                // Insert pre-action code here
                getDisplay().setCurrent(get_trailCanvas());//GEN-LINE:MVDCAAction26
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase26
        } else if (displayable == deviceList) {
            if (command == okCommand1) {//GEN-END:MVDCACase26
                // Insert pre-action code here
                
                // Set selected device as GPS
                int selectedIndex = deviceList.getSelectedIndex();
                String selectedDeviceAlias = deviceList.getString( selectedIndex );
                
                Vector devices = m_controller.getDevices();
                int deviceCount = devices.size();
                int deviceIndex;
                for(deviceIndex=0; deviceIndex<deviceCount; deviceIndex++) {
                    BluetoothDevice dev = (BluetoothDevice)devices.elementAt(deviceIndex);
                    String devAlias = dev.getAlias();
                    if(selectedDeviceAlias.equals(devAlias)==true) {
                        // We found the selected device
                        // Set device as GPS device
                        m_controller.setGpsDevice( dev );
                    }
                }
                
                getDisplay().setCurrent(get_trailCanvas());//GEN-LINE:MVDCAAction32
                // Insert post-action code here
            } else if (command == cancelCommand1) {//GEN-LINE:MVDCACase32
                // Insert pre-action code here
                getDisplay().setCurrent(get_trailCanvas());//GEN-LINE:MVDCAAction34
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase34
        }//GEN-END:MVDCACase34
    // Insert global post-action code here
}//GEN-LINE:MVDCAEnd

    /** This method initializes UI of the application.//GEN-BEGIN:MVDInitBegin
     */
    private void initialize() {//GEN-END:MVDInitBegin
        // Insert pre-init code here
        getDisplay().setCurrent(get_splashScreen());//GEN-LINE:MVDInitInit
        // Insert post-init code here
    }//GEN-LINE:MVDInitEnd
    
    /**
     * This method should return an instance of the display.
     */
    public Display getDisplay() {//GEN-FIRST:MVDGetDisplay
        return Display.getDisplay(this);
    }//GEN-LAST:MVDGetDisplay
    
    /**
     * This method should exit the midlet.
     */
    public void exitMIDlet() {//GEN-FIRST:MVDExitMidlet
        getDisplay().setCurrent(null);
        destroyApp(true);
        notifyDestroyed();
    }//GEN-LAST:MVDExitMidlet

    /** This method returns instance for splashScreen component and should be called instead of accessing splashScreen field directly.//GEN-BEGIN:MVDGetBegin2
     * @return Instance for splashScreen component
     */
    public org.netbeans.microedition.lcdui.SplashScreen get_splashScreen() {
        if (splashScreen == null) {//GEN-END:MVDGetBegin2
            // Insert pre-init code here
            splashScreen = new org.netbeans.microedition.lcdui.SplashScreen(getDisplay());//GEN-BEGIN:MVDGetInit2
            splashScreen.setTitle("TrackRecorder");
            splashScreen.setText("");
            splashScreen.setImage(get_splashImage());
            splashScreen.setNextDisplayable(get_trailCanvas());//GEN-END:MVDGetInit2
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd2
        return splashScreen;
    }//GEN-END:MVDGetEnd2

    /** This method returns instance for trailCanvas component and should be called instead of accessing trailCanvas field directly.//GEN-BEGIN:MVDGetBegin4
     * @return Instance for trailCanvas component
     */
    public com.substanceofcode.tracker.view.TrailCanvas get_trailCanvas() {
        if (trailCanvas == null) {//GEN-END:MVDGetBegin4
            // Insert pre-init code here
            trailCanvas = new com.substanceofcode.tracker.view.TrailCanvas(m_controller);//GEN-BEGIN:MVDGetInit4
            trailCanvas.addCommand(get_aboutCommand());
            trailCanvas.addCommand(get_deviceListCommand());
            trailCanvas.addCommand(get_exitCommand1());
            trailCanvas.addCommand(get_startCommand());
            trailCanvas.setCommandListener(this);//GEN-END:MVDGetInit4
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd4
        return trailCanvas;
    }//GEN-END:MVDGetEnd4

    /** This method returns instance for aboutBackCommand component and should be called instead of accessing aboutBackCommand field directly.//GEN-BEGIN:MVDGetBegin8
     * @return Instance for aboutBackCommand component
     */
    public Command get_aboutBackCommand() {
        if (aboutBackCommand == null) {//GEN-END:MVDGetBegin8
            // Insert pre-init code here
            aboutBackCommand = new Command("Back", Command.BACK, 1);//GEN-LINE:MVDGetInit8
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd8
        return aboutBackCommand;
    }//GEN-END:MVDGetEnd8

    /** This method returns instance for aboutCommand component and should be called instead of accessing aboutCommand field directly.//GEN-BEGIN:MVDGetBegin10
     * @return Instance for aboutCommand component
     */
    public Command get_aboutCommand() {
        if (aboutCommand == null) {//GEN-END:MVDGetBegin10
            // Insert pre-init code here
            aboutCommand = new Command("About", Command.SCREEN, 1);//GEN-LINE:MVDGetInit10
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd10
        return aboutCommand;
    }//GEN-END:MVDGetEnd10

    /** This method returns instance for deviceListCommand component and should be called instead of accessing deviceListCommand field directly.//GEN-BEGIN:MVDGetBegin12
     * @return Instance for deviceListCommand component
     */
    public Command get_deviceListCommand() {
        if (deviceListCommand == null) {//GEN-END:MVDGetBegin12
            // Insert pre-init code here
            deviceListCommand = new Command("Connect to GPS", Command.SCREEN, 1);//GEN-LINE:MVDGetInit12
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd12
        return deviceListCommand;
    }//GEN-END:MVDGetEnd12

    /** This method returns instance for deviceOkCommand component and should be called instead of accessing deviceOkCommand field directly.//GEN-BEGIN:MVDGetBegin14
     * @return Instance for deviceOkCommand component
     */
    public Command get_deviceOkCommand() {
        if (deviceOkCommand == null) {//GEN-END:MVDGetBegin14
            // Insert pre-init code here
            deviceOkCommand = new Command("Ok", Command.OK, 1);//GEN-LINE:MVDGetInit14
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd14
        return deviceOkCommand;
    }//GEN-END:MVDGetEnd14

    /** This method returns instance for deviceCancelCommand component and should be called instead of accessing deviceCancelCommand field directly.//GEN-BEGIN:MVDGetBegin16
     * @return Instance for deviceCancelCommand component
     */
    public Command get_deviceCancelCommand() {
        if (deviceCancelCommand == null) {//GEN-END:MVDGetBegin16
            // Insert pre-init code here
            deviceCancelCommand = new Command("Cancel", Command.CANCEL, 1);//GEN-LINE:MVDGetInit16
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd16
        return deviceCancelCommand;
    }//GEN-END:MVDGetEnd16

    /** This method returns instance for exitCommand1 component and should be called instead of accessing exitCommand1 field directly.//GEN-BEGIN:MVDGetBegin18
     * @return Instance for exitCommand1 component
     */
    public Command get_exitCommand1() {
        if (exitCommand1 == null) {//GEN-END:MVDGetBegin18
            // Insert pre-init code here
            exitCommand1 = new Command("Exit", Command.EXIT, 1);//GEN-LINE:MVDGetInit18
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd18
        return exitCommand1;
    }//GEN-END:MVDGetEnd18

    /** This method returns instance for startCommand component and should be called instead of accessing startCommand field directly.//GEN-BEGIN:MVDGetBegin20
     * @return Instance for startCommand component
     */
    public Command get_startCommand() {
        if (startCommand == null) {//GEN-END:MVDGetBegin20
            // Insert pre-init code here
            startCommand = new Command("Start recording", Command.SCREEN, 1);//GEN-LINE:MVDGetInit20
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd20
        return startCommand;
    }//GEN-END:MVDGetEnd20

    /** This method returns instance for splashImage component and should be called instead of accessing splashImage field directly.//GEN-BEGIN:MVDGetBegin22
     * @return Instance for splashImage component
     */
    public Image get_splashImage() {
        if (splashImage == null) {//GEN-END:MVDGetBegin22
            // Insert pre-init code here
            try {//GEN-BEGIN:MVDGetInit22
                splashImage = Image.createImage("/images/logo.png");
            } catch (java.io.IOException exception) {
            }//GEN-END:MVDGetInit22
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd22
        return splashImage;
    }//GEN-END:MVDGetEnd22

    /** This method returns instance for image1 component and should be called instead of accessing image1 field directly.//GEN-BEGIN:MVDGetBegin23
     * @return Instance for image1 component
     */
    public Image get_image1() {
        if (image1 == null) {//GEN-END:MVDGetBegin23
            // Insert pre-init code here
            try {//GEN-BEGIN:MVDGetInit23
                image1 = Image.createImage("<No Image>");
            } catch (java.io.IOException exception) {
            }//GEN-END:MVDGetInit23
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd23
        return image1;
    }//GEN-END:MVDGetEnd23

    /** This method returns instance for aboutForm component and should be called instead of accessing aboutForm field directly.//GEN-BEGIN:MVDGetBegin24
     * @return Instance for aboutForm component
     */
    public Form get_aboutForm() {
        if (aboutForm == null) {//GEN-END:MVDGetBegin24
            // Insert pre-init code here
            aboutForm = new Form("About", new Item[0]);//GEN-BEGIN:MVDGetInit24
            aboutForm.addCommand(get_backCommand1());
            aboutForm.setCommandListener(this);//GEN-END:MVDGetInit24
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd24
        return aboutForm;
    }//GEN-END:MVDGetEnd24

    /** This method returns instance for backCommand1 component and should be called instead of accessing backCommand1 field directly.//GEN-BEGIN:MVDGetBegin25
     * @return Instance for backCommand1 component
     */
    public Command get_backCommand1() {
        if (backCommand1 == null) {//GEN-END:MVDGetBegin25
            // Insert pre-init code here
            backCommand1 = new Command("Back", Command.BACK, 1);//GEN-LINE:MVDGetInit25
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd25
        return backCommand1;
    }//GEN-END:MVDGetEnd25

    /** This method returns instance for deviceList component and should be called instead of accessing deviceList field directly.//GEN-BEGIN:MVDGetBegin29
     * @return Instance for deviceList component
     */
    public com.substanceofcode.tracker.view.DeviceList get_deviceList() {
        if (deviceList == null) {//GEN-END:MVDGetBegin29
            // Insert pre-init code here
            deviceList = new com.substanceofcode.tracker.view.DeviceList(m_controller, "GPS Devices");//GEN-BEGIN:MVDGetInit29
            deviceList.addCommand(get_okCommand1());
            deviceList.addCommand(get_cancelCommand1());
            deviceList.setCommandListener(this);//GEN-END:MVDGetInit29
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd29
        return deviceList;
    }//GEN-END:MVDGetEnd29

    /** This method returns instance for okCommand1 component and should be called instead of accessing okCommand1 field directly.//GEN-BEGIN:MVDGetBegin31
     * @return Instance for okCommand1 component
     */
    public Command get_okCommand1() {
        if (okCommand1 == null) {//GEN-END:MVDGetBegin31
            // Insert pre-init code here
            okCommand1 = new Command("Ok", Command.OK, 1);//GEN-LINE:MVDGetInit31
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd31
        return okCommand1;
    }//GEN-END:MVDGetEnd31

    /** This method returns instance for cancelCommand1 component and should be called instead of accessing cancelCommand1 field directly.//GEN-BEGIN:MVDGetBegin33
     * @return Instance for cancelCommand1 component
     */
    public Command get_cancelCommand1() {
        if (cancelCommand1 == null) {//GEN-END:MVDGetBegin33
            // Insert pre-init code here
            cancelCommand1 = new Command("Cancel", Command.CANCEL, 1);//GEN-LINE:MVDGetInit33
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd33
        return cancelCommand1;
    }//GEN-END:MVDGetEnd33
    
    public void startApp() {
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
    
}
