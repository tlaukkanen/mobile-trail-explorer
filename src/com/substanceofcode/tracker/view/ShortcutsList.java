/*
 * ShortcutsList.java
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
import javax.microedition.lcdui.List;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.localization.LocaleManager;

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
        super(LocaleManager.getMessage("shortcuts_list_title"), List.EXCLUSIVE);
        append(LocaleManager.getMessage("shortcuts_list_add_audio_mark"), null);
        append(LocaleManager.getMessage("shortcuts_list_add_placemark"), null);
        append(LocaleManager.getMessage("shortcuts_list_none"), null);
        append(LocaleManager.getMessage("shortcuts_list_surveyor"), null);
        selectCommand = new Command(LocaleManager.getMessage("menu_select"), Command.OK, 1);
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