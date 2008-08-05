/*
 * KeySettingsList.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.substanceofcode.tracker.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;

import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.controller.Controller;

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
        super(LocaleManager.getMessage("key_settings_list_title"), List.IMPLICIT);
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