/**
 * Copyright 2010 Lukasz Szmit <devmail@szmit.eu>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/
package iAndroid.what2wear.google;

import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class ContactsAtomParser {

	final private static String	ID		= "ID";
	final private static String ENTRY = "ENTRY";
	final private static String	EMAIL	= "email";
	final private static String	FEED	= "FEED";

	public String parse(String dataString) {
		final XmlPullParser parser = Xml.newPullParser();
		String row = null;
		try {
			parser.setInput(new StringReader(dataString));
			int eventType = parser.getEventType();
			boolean done = false;

			

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String tag = null;
				String value = null;

				switch (eventType) {
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						if (tag.equalsIgnoreCase(ID) ) {
							value = parser.nextText();
							row = value.substring(value.lastIndexOf("/") + 1);
							return row;
						}
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return row;
	}
	
	public ArrayList<String> parseContacts(String dataString) {
		ArrayList<String> arr = new ArrayList<String>();
		final XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(new StringReader(dataString));
			int eventType = parser.getEventType();
			boolean done = false;

			boolean b = false;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String tag = null;

				switch (eventType) {
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						if (tag.equalsIgnoreCase(ENTRY))
							b = true;
						else if (tag.equalsIgnoreCase(EMAIL) && b)
							arr.add(parser.getAttributeValue(1));
						break;
						
					case XmlPullParser.END_TAG:
						tag = parser.getName();
						if (tag.equalsIgnoreCase(ENTRY) && b) {
							b = false;
						} else if (tag.equalsIgnoreCase(FEED))
							done = true;
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return arr;
	}
}
