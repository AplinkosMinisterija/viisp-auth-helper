package lt.biip.auth.generators;

import lt.epaslaugos.authentication.client.AuthenticationDataRequest;
import org.w3c.dom.Document;

/**
 * Generates example authenticationDataRequest XML element.
 */
public class AuthDataRequestGenerator extends BaseAuthRequestGenerator {
    public String generateRequest(String ticket) throws Exception {
        AuthenticationDataRequest dataRequest = new AuthenticationDataRequest();
        dataRequest.setId(SIGNED_NODE_ID);
        dataRequest.setPid(AuthRequestGenerator.PID);
        dataRequest.setIncludeSourceData(true);
        dataRequest.setTicket(ticket);

        Document doc = (Document) marshal(dataRequest);
        setIdAttribute(doc.getChildNodes().item(0));

        String xml = getSignedXml(doc.getFirstChild(), "#" + dataRequest.getId());

        return xml;
    }
}
