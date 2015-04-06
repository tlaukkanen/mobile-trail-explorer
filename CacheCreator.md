# Introduction #

Several people have asked about whether it is possible to download map images to a pc and transfer them to the phone, so as to save on the cost of mobile data charges.

I've written a small j2se app that creates the necessary cache file for MTE and, used in conjunction with some other software it is now possible to create maps 'offline'.

What follows is a guide on how to create the map cache file.

# Overview #

Mobile Trail Explorer (MTE) has a feature that enables map images to be displayed in the background behind the trail. This feature is enabled within MTE by clicking Settings->Display->Scroll to 'Map Display' and select one of the 'Draw  Maps' options.

With the option enabled MTE will access one of several map provider services and download the map images (tiles) around the area of the current GPS position, and display them on the screen. The map images are also stored in a cache file so that they do not get downloaded again unnecessarily. The cache file is called 'MTEFileCache' and is created in the Export folder (where trail kml/gpx files are stored).

## The Problem(s) ##
  * Mobile data plans vary in cost across the world from being free to being very expensive indeed.
  * You may wish to use maps in an area where there is no mobile coverage at all.

## The Solution ##
> Why not download the required map images on your pc before you set off, convert them into the format required by MTE, then transfer them to the phone?

# Cache Creator #

Enter Cache Creator, a desktop utility that converts map images into a cache file usable by MTE.

## What you need ##

  * A pc with Java installed, 1.5+ will be ok
  * The ability to copy files to a specific directory on your phone. If you can connect  your phone to your pc via usb and use it as a flash disk, you can already do this.
  * [CacheCreator](http://mobile-trail-explorer.googlecode.com/files/CacheCreator_v0.02.zip). The compiled jar file is in the dist directory.
  * Some application that can download map images from the internet. I suggest  [JTileDownloader](http://wiki.openstreetmap.org/index.php/JTileDownloader) for 2 reasons: it works and it's the only one I've tested this application with.

## How to Use ##

  * First you need to get some map images onto your pc. Use JTileDownloader to download a bunch of tiles around the area of interest.
> > Don't download too many tiles as currently MTE has to parse all of them on startup, and your phone may run out of memory.

  * Run Cache Creator from a command prompt like this:
> > `java -jar CacheCreator.jar osmmaps c:\tiles\ `


> Where 'c:\tiles' is the directory where JTileDownloader put the downloaded tiles.

> and 'osmmaps' is a key corresponding to the map type supported by MTE. Currently you can use 'osmmaps' or 'tahmaps' here. These strings map to the 'Display Map' options in the MTE settings, so If you use 'osmmaps' here make sure you also select 'Draw OSM maps' to see the map tiles.

  * Assuming all goes well, this will create a MTEFileCache file in the c:\tiles directory.

  * Copy this file to the export directory specified in MTE Settings->Export Folder

  * Restart MTE; the cache file will be parsed on startup and any displayable tiles will be shown.

## Note ##

If a cache file already exists, Cache Creator will add any new tiles to it.

## Limitations ##

Only png images are supported.
The images must be 256\*256 pixels.
Cache Creator **requires** the folder structure is of the format zoomlevel/latitude/longitude.png. (JTileDownloader outputs images like this.)

## To do ##

Integrate this into the MTE svn repository. (Not sure if this is possible, netBeans complained when I tried)


