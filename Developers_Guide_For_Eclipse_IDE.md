# Building Mobile Trail Explorer(MTE) in Eclipse using ANT. #

You must have the following Installed before you can build MTE in Eclipse:
  * [Eclipse](http://www.eclipse.org/) (naturally)
  * Suns 'Java Development Kit'
  * The SUN Wireless Toolkit(WTK) installed to "C:\WTK"  (see #1 bellow)
  * [EclipseME](http://eclipseme.org/) (a plug in for eclipse for developing JavaME applications - [see the download page](http://eclipseme.org/docs/installEclipseME.html))
  * Some Subversion access application, either [Subclipse](http://subclipse.tigris.org/) or   [TortoiseSVN](http://tortoisesvn.tigris.org/)

  * [Subclipse](http://subclipse.tigris.org/) (handy if using eclipse, but hard to configure if you're behind a proxy - [see the download page](http://subclipse.tigris.org/install.html))
  * [TortoiseSVN](http://tortoisesvn.tigris.org/) (more complicated compared to Subclipse, but works well behind proxys)

## #1 ##
If you have the SonyEricsson WTK installed, or the SUN one installed to some other folder, you will have to edit the build-eclipse.xml file. Edit the line:
'

&lt;property name="install.api.root" value="C:/WTK" /&gt;

'
, and replace the 'value' with the destination folder of your WTK installation, this is one folder above the \bin and \lib folders, for SonyEricsson, this is "C:/SonyEricsson/JavaME\_SDK\_CLDC/PC\_Emulation/WTK2" (I think...).
Also if you're using the SonyEricsson WTK instead of the SUN one, change the line:
'

&lt;target name="Default Build" depends="Default\_Color\_Phone" /&gt;

'
to
'

&lt;target name="Default Build" depends="SonyEricsson\_K750\_Emu" /&gt;

'

## Configure Eclipse to use the JDK (as opposed to the JRE ) ##
In eclipse, go:
Window -> Preferences
Java -> Installed JREs.

Here you should see one line, saying something like jre1.6.0, with a tick in the box on that line.

Now click "Add..."
Then click "Browse..."

And find where you installed the JDK (The default location (for 1.6) is "C:\Program Files\Java\jdk1.6.0")
In "JRE name" put in something descriptive, like "jdk1.6.0" (which might actually be the default anyway), now click OK.

You should now have a second line, with the JDK you've just found.
Make sure you have the tick beside the JDK (as opposed to the JRE).
DONE.

## Next... ##

Open Eclipse and download MTE into a new Project. (If you haven't done that already!)

Make sure you have an ANT window open by going:
Window -> Show/View -> ANT
Drag the build-eclipse.xml file across into that window

## Building for Development\Debugging ##
Simply double click on "Mobile Trail" in the ANT window, and it will compile and run MTE in an emulator for you. The compiled JAD and JAR files will be put in the \bin directory also, to be transfered to your phone/PDA etc. at your leisure.

## Building for Deployment (Harder to debug, but smaller JAR file, and better performance) ##
Expand the "Mobile Trail" item in the ANT window, and run (Double click on) any of the DXX-xxxxxxxxxxxxxxxxx itmes, e.g. D03-SonyEricsson\_K750\_Emu currently there is no difference between the JAR files created by any of these DXX scripts (so feel free to choose any). This should also open up an emulator for you, and run the compiled JAR, but don't worry if it doesn't, you probably ran one you don't have the emulator for!

The JAR file created by this script will also be put in \bin folder, it should be smaller, and (although you may not notice it much in MTE) a bit faster running on your phone.

## Huh? ##

Anything here not make sense? Not able to follow the instructions? e-mail barryred (at) gmail (dot) com for help using the "Eclipse" build tool, preferably with an explanation of what you didn't understand/what went wrong. And I'll try and make this page clearer.












