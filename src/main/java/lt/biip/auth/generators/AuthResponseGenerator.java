package lt.biip.auth.generators;

import lt.epaslaugos.authentication.client.AuthenticationResponse;

/**
 * Extracts ticket info from XML.
 */
public class AuthResponseGenerator extends BaseAuthResponseGenerator {

    public String generateResponse(String data) throws Exception {
        AuthenticationResponse dataResponse = new AuthenticationResponse();

        AuthenticationResponse response = (AuthenticationResponse) unmarshal(dataResponse, decodeSoap(data));

        return response.getTicket();
    }
}
