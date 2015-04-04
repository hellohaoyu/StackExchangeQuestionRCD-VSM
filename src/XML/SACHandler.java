package XML;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


public class SACHandler extends DefaultHandler {
	// Override method from default handler
//	public void startDocument(){
//		//System.out.println("Begin parsing document... ");
//	}
//	public void endDocument(){
//		//System.out.print("\n end parsing document... ");
//	}

	
	public void startElement(String nameSpaceURI, String localName, String qName, Attributes atts){
		if(atts.getValue("Title") != null){
			System.out.println(atts.getValue("Id") + ", " + atts.getValue("AcceptedAnswerId") + ", " + atts.getValue("Title"));
		}	
	}
	
	public void endElement(String namespaceURI, String localName, String qName){
		//System.out.print("</" + qName +">");
		//System.out.println();
	}
}
