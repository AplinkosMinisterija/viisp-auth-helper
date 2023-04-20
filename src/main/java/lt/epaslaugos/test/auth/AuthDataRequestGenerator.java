package lt.epaslaugos.test.auth;

import lt.atea.vaiisis.authentication.model.xml.AuthenticationDataRequestXml;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

/**
 * Generates example authenticationDataRequest XML element.
 */
public class AuthDataRequestGenerator extends BaseAuthRequestGenerator {
    public String generateRequest(String ticket) throws Exception {
        AuthenticationDataRequestXml dataRequest = new AuthenticationDataRequestXml();
        dataRequest.setId(SIGNED_NODE_ID);
        dataRequest.setPid(AuthRequestGenerator.PID);
        dataRequest.setIncludeSourceData(true);
        dataRequest.setTicket(ticket);

        Document doc = (Document) marshal(dataRequest);
        setIdAttribute(doc.getChildNodes().item(0));

        String xml = getSignedXml(doc.getFirstChild(), "#" + dataRequest.getId());

        System.out.println("Auth data request XML, which goes directly into SOAP body (Note: whitespace is important!):\n");
        System.out.println(StringUtils.substringAfter(xml, "?>"));
        return xml;
    }
}
