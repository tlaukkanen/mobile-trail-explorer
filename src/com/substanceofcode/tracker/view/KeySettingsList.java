/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.tracker.view;

import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.controller.Controller;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;

/**
 *
 * @author tommi
 */
public class KeySettingsList extends List implements CommandListener {

    private Command backCommand;
    private Command changeCommand;

    private static final short ASTERISK = 0;
    private static final short CROSSHATCH = 1;

    public KeySettingsList() {
        super("Keys", List.IMPLICIT);
        backCommand = new Command(LocaleManager.getMessage("menu_back"), Command.BACK, 3);
        addCommand(backCommand);
        changeCommand = new Command( LocaleManager.getMessage("menu_change"), Command.SCREEN, 1 );
        addCommand(changeCommand);
        setSelectCommand(changeCommand);
        setCommandListener(this);
        append( LocaleManager.getMessage("keys_star"), null);
        append( LocaleManager.getMessage("keys_pound"), null);
    }

    public void commandAction(Command cmd, Displayable disp) {
        Controller controller = Controller.getController();
        if(cmd==backCommand) {
            controller.showSettings();
        }
        if(cmd==changeCommand) {
            int selected = getSelectedIndex();
            ShortcutsList shortcuts = new ShortcutsList();
            int shortcut = 0;
            if(selected==ASTERISK) {
                shortcut = controller.getSettings().getStarShortcut();
            } else if(selected==CROSSHATCH) {
                shortcut = controller.getSettings().getPoundShortcut();
            }
            shortcuts.setKey( (short)selected );
            shortcuts.setSelectedIndex( shortcut, true );
            controller.showDisplayable( shortcuts );
        }
    }

}
