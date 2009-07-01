/*
 * MultimediaSettingsFrom.java
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
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

import java.util.*;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ChoiceGroup;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.RecorderSettings;
import com.substanceofcode.localization.LocaleManager;

/**
 *
 * @author pat
 */
public class MultimediaSettingsForm extends Form implements CommandListener {

    private String audio;
    private String[] audioText;
    private int audioIndex;
    private Controller controller;
    private Command okCommand;
    private Command cancelCommand;
    private ChoiceGroup audioChoiceGroup;

    private String[] splitString(String inString, String separator) {
        Vector nodes = new Vector();
        int pos = inString.indexOf(separator);
        while (pos >= 0) {
            if (pos > 0) {
                nodes.addElement(inString.substring(0, pos));
            }
            inString = inString.substring(pos + separator.length());
            pos = inString.indexOf(separator);
        }
        if (inString.length() > 0) {
            nodes.addElement(inString);
        }
        String[] outString = new String[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            outString[i] = (String) nodes.elementAt(i);
        }
        return outString;
    }

    public MultimediaSettingsForm(Controller controller) {
        super(LocaleManager.getMessage("multimedia_form_title"));
        this.controller = controller;

        initializeControls();
        initializeCommands();

        this.setCommandListener(this);

    }

    private void initializeControls() {
        RecorderSettings settings = controller.getSettings();
        audioIndex = settings.getAudioIndex();

        audio = System.getProperty("audio.encodings");
        audioChoiceGroup = new ChoiceGroup(LocaleManager.getMessage("multimedia_form_audio"), ChoiceGroup.EXCLUSIVE);
        if (audio != null) {
            audioText = splitString(audio, " ");
            for (int i = 0; i < audioText.length; i++) {
                String codec = null;
                String[] audioTemp1 = splitString(audioText[i], "&");
                for (int j = 0; j < audioTemp1.length; j++) {
                    String[] audioTemp2 = splitString(audioTemp1[j], "=");
                    if ((audioTemp2.length == 2) && (audioTemp2[0].equalsIgnoreCase("encoding"))) {
                        codec = audioTemp2[1];
                    }
                }
                if ((audioTemp1.length == 1) && (codec != null)) {
                    audioChoiceGroup.append(codec, null);//found codec without options
                } else {
                    audioChoiceGroup.append(audioText[i], null);
                }
            }
        }
        //append item for previous used method (with didn't work
        //with my SE 800i, but may be needed by other users)
        audioChoiceGroup.append(LocaleManager.getMessage("multimedia_form_audio_old_standard"), null);
        audioChoiceGroup.setSelectedIndex(audioIndex, true);
        this.append(audioChoiceGroup);
    }

    private void initializeCommands() {
        okCommand = new Command(LocaleManager.getMessage("menu_ok"), Command.OK, 1);
        this.addCommand(okCommand);
        cancelCommand = new Command(LocaleManager.getMessage("menu_cancel"), Command.CANCEL, 2);
        this.addCommand(cancelCommand);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand) {
            RecorderSettings settings = controller.getSettings();
            /** save audio index */
            audioIndex = audioChoiceGroup.getSelectedIndex();
            settings.setAudioIndex(audioIndex);
            /** save audio encoding and suffix */
            String audioSuffix = "wav"; // Standard audio suffix
            if ((audioText != null) && (audioIndex < audioText.length)) {
                /** get encoding string */
                String audioEncoding = audioText[audioIndex];
                String codec = null;
                String[] audioTemp1 = splitString(audioEncoding, "&");
                for (int j = 0; j < audioTemp1.length; j++) {
                    String[] audioTemp2 = splitString(audioTemp1[j], "=");
                    if ((audioTemp2.length == 2) && (audioTemp2[0].equalsIgnoreCase("encoding"))) {
                        codec = audioTemp2[1];
                    }
                }
                if ((audioTemp1.length == 1) && (codec != null)) {
                    /** codec found */
                    if (codec.equalsIgnoreCase("amr")
                        || codec.equalsIgnoreCase("amr-nb")
                        || codec.equalsIgnoreCase("audio/amr")
                        || codec.equalsIgnoreCase("audio/amr-wb")
                       ) {
                        audioSuffix = "amr";
                    } else if (codec.equalsIgnoreCase("audio/wave")
                               || codec.equalsIgnoreCase("audio/wav")
                               || codec.equalsIgnoreCase("audio/pcm")
                               || codec.equalsIgnoreCase("wave")
                               || codec.equalsIgnoreCase("wav")
                               || codec.equalsIgnoreCase("pcm")
                              ) {
                        audioSuffix = "wav";
                    }
                }
                settings.setAudioEncoding(audioText[audioIndex]);
            } else {
                /** last position is to use former standard method */
                settings.setAudioEncoding("");
                // don't change standard suffix "wav"
            }
            //save audio suffix
            settings.setAudioSuffix(audioSuffix);
        }
        controller.showSettings();
    }
}
