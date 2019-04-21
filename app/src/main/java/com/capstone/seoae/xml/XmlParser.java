package com.capstone.seoae.xml;

import android.content.res.AssetManager;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;


/**
 * Reads an XML file and stores all the data in {@link XmlNode} objects,
 * allowing for easy access to the data contained in the XML file.
 *
 * @author Karl
 *
 */
public class XmlParser {

    public static XmlNode parse(InputStream in)  {
        try {
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xpp.setInput(in, null);
            int eventType = xpp.getEventType();
            if (eventType == XmlPullParser.START_DOCUMENT) {
                XmlNode parent = new XmlNode("xml");
                loadNode(xpp, parent);
                return parent.getChild("COLLADA");
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static void loadNode(XmlPullParser xpp, XmlNode parentNode) throws XmlPullParserException, IOException {
        int eventType = xpp.next();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                XmlNode childNode = new XmlNode(xpp.getName());
                for (int i=0; i<xpp.getAttributeCount(); i++){
                    childNode.addAttribute(xpp.getAttributeName(i), xpp.getAttributeValue(i));
                }
                parentNode.addChild(childNode);
                loadNode(xpp, childNode);
            } else if (eventType == XmlPullParser.END_TAG) {
                return;
            } else if (eventType == XmlPullParser.TEXT) {
                parentNode.setData(xpp.getText());
            }
            eventType = xpp.next();
        }
    }
}
