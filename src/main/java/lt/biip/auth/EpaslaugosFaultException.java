package lt.biip.auth;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPConstants;
import java.io.IOException;
import java.io.InputStream;

final class EpaslaugosFaultException extends Exception {
    private final int httpStatus;
    private final String faultCode;
    private final String faultString;

    EpaslaugosFaultException(int httpStatus, String faultCode, String faultString) {
        super("VIISP returned HTTP " + httpStatus);
        this.httpStatus = httpStatus;
        this.faultCode = faultCode;
        this.faultString = faultString;
    }

    int getHttpStatus() {
        return httpStatus;
    }

    String getFaultCode() {
        return faultCode;
    }

    String getFaultString() {
        return faultString;
    }

    static EpaslaugosFaultException fromResponse(int httpStatus, InputStream errorStream) {
        if (errorStream != null) {
            try (InputStream stream = errorStream) {
                var factory = DocumentBuilderFactory.newDefaultNSInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                var builder = factory.newDocumentBuilder();
                builder.setErrorHandler(new DefaultHandler());
                var document = builder.parse(stream);

                var fault = document.getElementsByTagNameNS(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Fault").item(0);
                if (fault != null) {
                    return new EpaslaugosFaultException(
                            httpStatus, text(child(fault, "faultcode")), text(child(fault, "faultstring")));
                }

                fault = document.getElementsByTagNameNS(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "Fault").item(0);
                if (fault != null) {
                    return new EpaslaugosFaultException(
                            httpStatus, text(child(child(fault, "Code"), "Value")),
                            text(child(child(fault, "Reason"), "Text")));
                }
            } catch (IOException | ParserConfigurationException | SAXException ignored) {
                // HTTP errors can have missing or non-SOAP bodies.
            }
        }
        return new EpaslaugosFaultException(httpStatus, null, null);
    }

    private static Element child(Node parent, String name) {
        for (Node node = parent == null ? null : parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && name.equals(element.getLocalName())) {
                return element;
            }
        }
        return null;
    }

    private static String text(Element element) {
        if (element == null) {
            return null;
        }
        String text = element.getTextContent().trim();
        return text.isEmpty() ? null : text;
    }
}
