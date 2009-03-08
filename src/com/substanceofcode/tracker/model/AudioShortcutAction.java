/*
 * AudioShortcutAction.java
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
package com.substanceofcode.tracker.model;

import java.io.IOException;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.gps.GpsPosition;
import com.substanceofcode.util.FileUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class AudioShortcutAction implements ShortcutAction {

    /** Do Recording */
    private static RecordControl rc;
    private static Player p;
    private static boolean bRecStarted = false;

    public void execute() {
        if (!bRecStarted) {
            // Create new marker
            String dateStamp = DateTimeUtil.getCurrentDateStamp();
            Controller controller = Controller.getController();
            GpsPosition pos = controller.getPosition();

            //get encoding settings
            RecorderSettings settings = controller.getSettings();
            String audioEncoding = settings.getAudioEncoding();
            String audioSuffix = settings.getAudioSuffix();

            Track track = controller.getTrack();
            String audioFile = LocaleManager.getMessage("audio_shortcut_action_file_prefix")
                    + dateStamp + "." + audioSuffix;
            Marker audioMarker =
                    new Marker(pos,
                               LocaleManager.getMessage("audio_shortcut_action_marker_name"),
                               audioFile);
            track.addMarker(audioMarker);

            if ((audioEncoding != null) && !audioEncoding.equals("")) {
                audioEncoding = "?" + audioEncoding;
            }
            
            try {
                p = Manager.createPlayer("capture://audio" + audioEncoding);
                p.realize();
                rc = (RecordControl) p.getControl("RecordControl");
                
                String exportFolder = controller.getSettings().getExportFolder();
                
                String path = exportFolder + audioFile;
                FileUtil.createFile( path );
                
                rc.setRecordLocation("file:///" + path);
                rc.startRecord();
                p.start();
                bRecStarted = true;
                controller.setAudioRecOn(bRecStarted);
            } catch (IOException ex) {
                controller.setError(LocaleManager.getMessage("audio_shortcut_ioexception")
                        + ": " + ex.getMessage());
                ex.printStackTrace();
                controller.setAudioRecOn(false);
            } catch (MediaException ex) {
                controller.setError(LocaleManager.getMessage("audio_shortcut_mediaexception")
                        + ": " + ex.getMessage());
                ex.printStackTrace();
                controller.setAudioRecOn(false);
            }
        } else {
            Controller controller = Controller.getController();
            try {
                // Stop recording
                rc.commit();
                p.close();
                bRecStarted = false;
                controller.setAudioRecOn(bRecStarted);
            } catch (IOException ex) {
                controller.setError(LocaleManager.getMessage("audio_shortcut_ioexception")
                        + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}