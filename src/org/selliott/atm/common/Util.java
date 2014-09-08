package org.selliott.atm.common;

import java.io.InputStream;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Miscellaneous utilities
 */
public class Util {
    private static DocumentBuilderFactory docBuilderFact;
    private static DocumentBuilder docFact;
    private static Transformer trans;

    /**
     * Create a new XML document using the DOM API. This assumes that the
     * default factory and builder are acceptable.
     * 
     * @return New DOM document
     * @throws ATMException
     *             Error in the DOM XML API.
     */
    public static synchronized Document newXmlDoc() throws ATMException {
        try {
            xmlInit();
            return docFact.newDocument();
        } catch (ParserConfigurationException e) {
            throw new ATMException("Could not create a new XML document.", e);
        }
    }

    /**
     * Read a XML document using the DOM API. This assumes that the default
     * factory and builder are acceptable.
     * 
     * @param in
     *            InputStream to read XML from.
     * @return New DOM document
     * @throws ATMException
     *             Error in the DOM XML API.
     */
    public static synchronized Document xmlDocParse(InputStream in)
            throws ATMException {
        try {
            xmlInit();
            return docFact.parse(in);
        } catch (Exception e) {
            throw new ATMException("Could not parse XML document.", e);
        }
    }

    /**
     * Format a DOM XML document and write the result to the specified writer.
     * 
     * @param doc
     *            DOM XML document.
     * @param writer
     *            Writer to write to.
     * @throws ATMException
     *             Tranformer exception.
     */
    public static synchronized void xmlFormat(Document doc, Writer writer)
            throws ATMException {
        // TODO: Assume all transformations will be the same for now.
        try {
            if (trans == null) {
                trans = TransformerFactory.newInstance().newTransformer();
                trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
                trans.setOutputProperty(
                        "{http://xml.apache.org/xslt}indent-amount", "2");
            }
            trans.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            // TODO: Could this log sensitive data?
            throw new ATMException("Unable to tranform XML document \"" + doc
                    + "\".", e);
        }
    }

    /**
     * Initialize the factory and builder which is then used for all DOM
     * activity.
     * 
     * @throws ParserConfigurationException
     *             Failed to create the factory and or builder.
     */
    private static void xmlInit() throws ParserConfigurationException {
        if (docBuilderFact == null) {
            docBuilderFact = DocumentBuilderFactory.newInstance();
            // TODO: Some sort of validation, such as with XML Schema, might
            // be nice in the future.
            docBuilderFact.setValidating(false);
            docFact = docBuilderFact.newDocumentBuilder();
        }
    }
}
