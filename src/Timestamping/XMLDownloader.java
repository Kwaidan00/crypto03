package zad6;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLDownloader {
	private URL url;
	private String seedValueString;
	private String timeStampString;
	
	XMLDownloader(String url) throws MalformedURLException {
		this.url = new URL(url);
	}

	public String getSeedValueString() {
		return seedValueString;
	}

	public void setSeedValueString(String seedValueString) {
		this.seedValueString = seedValueString;
	}

	public String getTimeStampString() {
		return timeStampString;
	}

	public void setTimeStampString(String timeStampString) {
		this.timeStampString = timeStampString;
	}
	
	public void downloadXML() throws IOException, ParserConfigurationException, SAXException {
		InputStream urlStream = url.openStream();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(urlStream);
		
		doc.getDocumentElement().normalize();
		this.seedValueString = doc.getElementsByTagName("seedValue").item(0).getTextContent();
		this.timeStampString = doc.getElementsByTagName("timeStamp").item(0).getTextContent();
		//System.out.println("seedValue: " + seedValueString);
		//System.out.println("timeStamp: " + timeStampString);
	}
	
	
}
