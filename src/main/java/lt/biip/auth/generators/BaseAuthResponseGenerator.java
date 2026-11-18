package lt.biip.auth.generators;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

abstract class BaseAuthResponseGenerator {
    private static final String SOAP_BODY_START = "<soap:Body>";
    private static final String SOAP_BODY_END = "</soap:Body>";

    public Object unmarshal(Object data, String file) throws JAXBException {
        String packageName = data.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        // No DTDs and no external entities: the response comes from outside.
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(file));
            return unmarshaller.unmarshal(reader);
        } catch (XMLStreamException e) {
            throw new JAXBException(e);
        }
    }

    public String decodeSoap(String data) {
        // Same result as the former greedy "<soap:Body>(.*)</soap:Body>" match on
        // the single-line response, without a backtracking regular expression.
        int start = data.indexOf(SOAP_BODY_START);
        int end = data.lastIndexOf(SOAP_BODY_END);
        if (start < 0 || end < start + SOAP_BODY_START.length()) {
            return "";
        }
        return data.substring(start + SOAP_BODY_START.length(), end);
    }

}
