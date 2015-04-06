# Developers Guide for Netbeans IDE #

# Introduction #

This is a guide for developers who wish to use Netbeans IDE in Mobile Trail Explorer development.

# Requirements #

You will need to have [Netbeans IDE](http://www.netbeans.org/products/ide/) and [Mobility Pack](http://www.netbeans.org/products/mobility/) installed. You can download the installer for both from [Netbeans website](http://www.netbeans.info/downloads/index.php).

# Steps to build the application #

  1. Install IDE + Modules
    1. download the installers for both from [Netbeans website](http://www.netbeans.info/downloads/index.php)
    1. Start Netbeans IDE
    1. Install [Subversion module](http://subversion.netbeans.org/) from update center (Tools/Update Center)
  1. Import source
    1. Select from menu: Subversion/Checkout...
    1. Set repository url to http://mobile-trail-explorer.googlecode.com/svn/trunk/
    1. Set repository folder to **trunk**
    1. Set local folder to point to root of your project folder (e.g. c:\projects\trailexplorer\)
  1. Build and run
    1. Click **Run Main Project** or press F6
    1. Finished! Now you should be running MTE in emulator