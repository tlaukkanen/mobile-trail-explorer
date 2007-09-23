/*
 * TrackExporter.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.io.file.FileConnection;

import org.kxml2.io.KXmlParser;

import com.substanceofcode.tracker.view.Logger;

/**
 * TrackConverter interface is used when track positions are exported to
 * different formats.
 * 
 * @author Tommi Laukkanen
 */
// FIXME: it would make sense for this to extend KXmlParser.( I think anyway)
public abstract class TrackConverter {

  /**
  * Abstract method to allow for differentiated handling of different xml 
  * formats e.g. kml/gpx
  */
	public abstract String convert(Track track, 
			                           Vector waypoints,
			                           boolean includeWaypoints, 
			                           boolean includeMarkers);

	/**
	 * Abstract method to allow for differentiated handling of different xml 
	 * formats e.g. kml/gpx
	 */
	public abstract Track importTrack(KXmlParser trackDescription);

	/**
	 * TrackConverter : Open File using provided FileConnection and construct
	 * a KXmlParser using the resulting InputStreamReader - This is then passed
	 * to abstract method importTrack(...) which will handle the file in an
	 * appropriate way.
	 */
	public Track importTrack(FileConnection connection) {
		Track result = null;
		try {
			/* Make sure file exists and can be read */
			if (!connection.exists()) {
				Logger.getLogger().log("FileConnection does not exist, Track Import " +
						                   "aborted", Logger.WARN);
				return null;
			}
			if (!connection.canRead()) {
				Logger.getLogger().log("FileConnection can not be read exist, " +
						                   "Track Import aborted",Logger.WARN);
				return null;
			}

			InputStream is = connection.openInputStream();
			InputStreamReader isr = new InputStreamReader(is);

			KXmlParser parser = new KXmlParser();
			parser.setInput(isr);

			result = importTrack(parser);

			try {
				isr.close();
			} catch (IOException e) {
			}
			try {
				is.close();
			} catch (IOException e) {
			}
		} catch (Exception e) {
			Logger.getLogger().log("Exception caught trying to importTrack :" + 
					                   e.toString(),Logger.WARN);
			e.printStackTrace();
		}
		return result;
	}

}
