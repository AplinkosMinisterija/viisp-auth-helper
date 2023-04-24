package lt.epaslaugos.test.auth;

import lt.atea.vaiisis.authentication.model.xml.AuthenticationDataResponseXml;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts user info from XML.
 */
public class AuthDataResponseGenerator extends BaseAuthResponseGenerator {
    public Map<String, String> generateResponse(String data) throws Exception {
        AuthenticationDataResponseXml dataResponse = new AuthenticationDataResponseXml();
        AuthenticationDataResponseXml response = (AuthenticationDataResponseXml) unmarshal(dataResponse, decodeSoap(data));

        Map<String, String> result = new HashMap<>();

        response.getUserInformation().forEach((item) -> {
            String key = item.getInformation().value();
            String value = item.getValue().getStringValue();
            result.put(key, value);
        });

        response.getAuthenticationAttribute().forEach((item) -> {
            String key = item.getAttribute().value();
            String value = item.getValue();
            result.put(key, value);
        });

        return result;
    }
}
