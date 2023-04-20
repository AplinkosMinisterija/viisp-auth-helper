package lt.epaslaugos.test.auth;

import lt.atea.vaiisis.authentication.model.xml.AuthenticationResponseXml;

/**
 * Extracts ticket info from XML.
 */
public class AuthResponseGenerator extends BaseAuthResponseGenerator {

    public String generateResponse(String data) throws Exception {
        AuthenticationResponseXml dataResponse = new AuthenticationResponseXml();

        AuthenticationResponseXml response = (AuthenticationResponseXml) unmarshal(dataResponse, decodeSoap(data));

        return response.getTicket();

    }
}
