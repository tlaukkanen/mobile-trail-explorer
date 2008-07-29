/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.view;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author tommi
 */
class ShortcutsList extends List implements CommandListener {

    Command selectCommand;
    short key;

    public static final short KEY_ASTERISK = 0;
    public static final short KEY_CROSSHATCH = 1;

    public ShortcutsList() {
        super("Actions", List.EXCLUSIVE);
        append("Add audio-mark", null);
        append("Add placemark", null);
        selectCommand = new Command("Select", Command.OK, 1);
        addCommand(selectCommand);
        setSelectCommand(selectCommand);
        setCommandListener(this);
    }

    public void setKey(short key) {
        this.key = key;
    }
    
    public void commandAction(Command cmd, Displayable disp) {
        if(cmd==selectCommand) {
            int selectedIndex = getSelectedIndex();
            RecorderSettings settings = Controller.getController().getSettings();
            if(key==KEY_ASTERISK) {
                settings.setStarShortcut(selectedIndex);
            } else {
                settings.setPoundShortcut(selectedIndex);
            }
            Controller.getController().showKeySettings();
        }
    }

}
