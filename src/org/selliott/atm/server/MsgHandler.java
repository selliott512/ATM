package org.selliott.atm.server;

import java.io.Reader;
import java.io.Writer;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.selliott.atm.common.ATMError;
import org.selliott.atm.common.ATMException;
import org.selliott.atm.common.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handle messages received from the client, namely "atmRequest" elements, by
 * reading the from a reader and then write a response, namely a "atmResponse"
 * element, to a writer.
 */
public class MsgHandler extends DefaultHandler {
    private static final Logger log = Logger.getLogger(MsgHandler.class);
    private Element atmResponseEl;
    private Command command;
    Reader reader;
    private Document response;
    private boolean saXerror;
    Writer writer;
    private XMLReader xmlReader;

    /**
     * Create a new message handler.
     * 
     * @param reader
     *            The reader.
     * @param writer
     *            The writer.
     * @throws ATMException
     *             Application level error occured.
     */
    public MsgHandler(Reader reader, Writer writer) throws ATMException {
        try {
            log.info("Begin init.");
            this.reader = reader;
            this.writer = writer;

            // Parse the request message using SAX since it may be large.
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser parser = spf.newSAXParser();
            xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(this);
        } catch (Exception e) {
            throw new ATMException("Unable to initialize handler", e);
        } finally {
            log.info("End init.");
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            Util.xmlFormat(response, writer);
        } catch (ATMException e) {
            log.error("Transform failed", e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        log.info("endElement  : uri=" + uri + " localName=" + localName
                + " qName=" + qName);
        if (qName.equals("command")) {
            if (saXerror) {
                log.error("Ignoring command \"" + command.name
                        + "\" due to a SAX error.");
                return;
            }
            log.info("Processing command \"" + command.name + "\"");
            boolean resultAppended = false;
            try {
                Result result = command.process();
                Element resultEl = response.createElement("result");
                resultEl.setAttribute("name", command.name);
                resultEl.setAttribute("error", "" + command.error);
                atmResponseEl.appendChild(resultEl);
                resultAppended = true;

                // result may be null in the case of an invalid PIN.
                if (result != null) {
                    for (Entry<String, String> entry : result.fieldMap
                            .entrySet()) {
                        String name = entry.getKey();
                        String value = entry.getValue();
                        Element feildEl = response.createElement("field");
                        feildEl.setAttribute("name", name);
                        feildEl.setAttribute("value", value);
                        resultEl.appendChild(feildEl);
                    }
                }
            } catch (ATMException e) {
                // Send back an error result.
                // TODO: More information for the caller, if it can be done
                // securely.
                if (!resultAppended) {
                    Element resultEl = response.createElement("result");
                    resultEl.setAttribute("name", command.name);
                    // TODO: It may not just be an invalid PIN.
                    resultEl.setAttribute("error", "" + ATMError.INVALID_PIN);
                    atmResponseEl.appendChild(resultEl);
                }
                log.error(
                        "Could not process command \"" + command.name + "\".",
                        e);
            }
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        log.error("Error processing a message", e);
        saXerror = true;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        log.error("Fatal error processing a message", e);
        saXerror = true;
    }

    /**
     * Process input messages in output output messages.
     * 
     * @return True if the message was successfully processed.
     */
    public boolean process() {
        log.info("Begin run.");
        boolean success = true;
        try {
            xmlReader.parse(new InputSource(reader));
        } catch (Exception e) {
            success = false;
            log.error("Unable to parse read XML message.", e);
        }
        log.info("End run.");
        return success;
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            // Use DOM since the response is likely to be small.
            response = Util.newXmlDoc();
            atmResponseEl = response.createElement("atmResponse");
            response.appendChild(atmResponseEl);
        } catch (ATMException e) {
            // TODO: Does this stop the processing?
            throw new SAXException(
                    "Unable to create a new response XML document.", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        log.info("startElement: uri=" + uri + " localName=" + localName
                + " qName=" + qName);
        saXerror = false;
        if (qName.equals("command")) {
            String name = attributes.getValue("name");
            log.info("New command \"" + name + "\"");
            command = new Command(name);
        } else if (qName.equals("field")) {
            command.setField(attributes.getValue("name"),
                    attributes.getValue("value"));
        }
    }
}
