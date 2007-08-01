/*
 * XML Parsing using kxml2
 * Author : Naveen Balani
 */

//KXML Apis
import org.kxml2.io.*;
import org.xmlpull.v1.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;

import java.io.*;
import java.util.Vector;

public class XMLJ2MEService extends MIDlet implements CommandListener {

	//Form Name
	Form mainForm = new Form("SampleJ2MEXML");

	//Location of xml file
	static final String URL = "http://localhost:8080/examples/book.xml";

	Vector bookVector = new Vector();

	StringItem resultItem = new StringItem("", "");

	private final static Command xmlCommand = new Command("Get XML Data",
			Command.OK, 1);

	class ReadXML extends Thread {

		public void run() {
			try {
				//Open http connection
				HttpConnection httpConnection = (HttpConnection) Connector
						.open(URL);

				//Initilialize XML parser
				KXmlParser parser = new KXmlParser();

				parser.setInput(new InputStreamReader(httpConnection
						.openInputStream()));

				parser.nextTag();

				parser.require(XmlPullParser.START_TAG, null, "catalog");

				//Iterate through our XML file
				while (parser.nextTag() != XmlPullParser.END_TAG)
					readXMLData(parser);

				parser.require(XmlPullParser.END_TAG, null, "catalog");
				parser.next();

				parser.require(XmlPullParser.END_DOCUMENT, null, null);

			} catch (Exception e) {
				e.printStackTrace();
				resultItem.setLabel("Error:");
				resultItem.setText(e.toString());

			}
		}
	}

	public XMLJ2MEService() {
		mainForm.append(resultItem);
		mainForm.addCommand(xmlCommand);
		mainForm.setCommandListener(this);

	}

	public void startApp() {
		Display.getDisplay(this).setCurrent(mainForm);
		new ReadXML().start();
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	public void commandAction(Command c, Displayable d) {

		StringBuffer sb = new StringBuffer();

		if (c == xmlCommand) {

			//Display parsed  XML file
			for (int i = 0; i < bookVector.size(); i++) {
				Book book = (Book) bookVector.elementAt(i);
				sb.append("\n");
				sb.append("Name : ");
				sb.append(book.getName());
				sb.append("\n");
				sb.append("Descrition : ");
				sb.append(book.getDescription());
				sb.append("\n");

			}
			resultItem.setLabel("Book Information");
			resultItem.setText(sb.toString());
		}

	}

	private void readXMLData(KXmlParser parser) throws IOException,
			XmlPullParserException {

		//Parse our XML file
		parser.require(XmlPullParser.START_TAG, null, "title");

		Book book = new Book();

		while (parser.nextTag() != XmlPullParser.END_TAG) {

			parser.require(XmlPullParser.START_TAG, null, null);
			String name = parser.getName();

			String text = parser.nextText();

			System.out.println("<" + name + ">" + text);

			if (name.equals("name"))
				book.setName(text);
			else if (name.equals("description"))
				book.setDescription(text);
			else if (name.equals("author"))
				book.setAuthor(text);
			else if (name.equals("rating"))
				book.setRating(text);
			else if (name.equals("available"))
				book.setAvailable(text);

			parser.require(XmlPullParser.END_TAG, null, name);
		}

		bookVector.addElement(book);

		parser.require(XmlPullParser.END_TAG, null, "title");

	}
	
	private class Book{
		private String name, description, author, rating, available;

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getAvailable() {
			return available;
		}

		public void setAvailable(String available) {
			this.available = available;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRating() {
			return rating;
		}

		public void setRating(String rating) {
			this.rating = rating;
		}
	}
	
}
