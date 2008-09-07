/*
 * GpxStream.java
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

import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.util.DateTimeUtil;
import com.substanceofcode.localization.LocaleManager;

/**
 *
 * @author steinerp
 */
public class GpxStream {

    private final Controller controller;

    private boolean fileCreated;

    public GpxStream(Controller controller) {
        this.controller = controller;

        fileCreated = false;

        createStreamFile();
    }
    
    public synchronized void createStreamFile() {
        // do IO operations in another thread to prevent UI freezing.
        new Thread(new Runnable() {
            public void run() {
                try {
                    String folder = controller.getSettings().getExportFolder();
                    folder += (folder.endsWith("/") ? "" : "/");
                    String timeStamp = DateTimeUtil.getCurrentDateStamp();
                    String fullPath = "file:///" + folder + "stream_"
                            + timeStamp + ".gpx";
                    Track streamTrack = new Track(fullPath, true);
                    controller.loadTrack(streamTrack);
                    
                    // ---------------------------------------------------------
                    // Store details in our settings file to allow us to
                    // recover from crashes
                    // ---------------------------------------------------------
                    controller.getSettings().setStreamingStarted(fullPath);
                    fileCreated = true;
                } catch (Exception e) {
                    controller.showError(LocaleManager.getMessage("trails_list_error") +
                            ": " + e.toString());
                }
            }
        }).start();
    }

    public synchronized boolean streamIsWritten() {
        return fileCreated;
    }
}