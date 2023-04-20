package lt.epaslaugos.test.auth;

import lt.atea.vaiisis.authentication.model.xml.AuthenticationDataResponseXml;
import org.apache.commons.lang.StringUtils;
import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.json.JSONObject;

/**
 * Extracts user info from XML.
 *
 */
public class AuthDataResponseGenerator extends BaseAuthResponseGenerator {
    public Map generateResponse(String data) throws Exception {
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
