package lt.biip.auth.generators;

import lt.epaslaugos.authentication.client.AuthenticationAttribute;
import lt.epaslaugos.authentication.client.AuthenticationProvider;
import lt.epaslaugos.authentication.client.AuthenticationRequest;
import lt.epaslaugos.authentication.client.UserInformation;
import org.w3c.dom.Document;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * Generates example authenticationRequest XML element.
 */
public class AuthRequestGenerator extends BaseAuthRequestGenerator {
    public static final String PID = System.getenv("VIISP_PID");
    public static final String POSTBACK_URL = System.getenv("VIISP_POSTBACK_URL");

    public AuthRequestGenerator() {
        super();
    }

    AuthRequestGenerator(PrivateKey privateKey, PublicKey publicKey, SignatureAlgorithm signatureAlgorithm) {
        super(privateKey, publicKey, signatureAlgorithm);
    }

    public static void main(String[] args) throws Exception {
    }

    public String generateRequest(String customData) throws Exception {
        return generateRequest(customData, null);
    }

    public String generateRequest(String customData, List<String> providers) throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setId(SIGNED_NODE_ID);

        request.setPid(PID);
        request.setCustomData(customData);
        request.setPostbackUrl(POSTBACK_URL);
        request.getAuthenticationAttribute().add(AuthenticationAttribute.LT_PERSONAL_CODE);
        request.getAuthenticationAttribute().add(AuthenticationAttribute.LT_COMPANY_CODE);
        request.getAuthenticationAttribute().add(AuthenticationAttribute.EIDAS_EID);

        for (AuthenticationProvider provider : AuthProviders.resolve(providers)) {
            request.getAuthenticationProvider().add(provider);
        }

        // request.setServiceTarget(ServiceTargetXml.SERVICE_TARGET_BUSINESS);
        request.getUserInformation().add(UserInformation.FIRST_NAME);
        request.getUserInformation().add(UserInformation.LAST_NAME);
        request.getUserInformation().add(UserInformation.EMAIL);
        request.getUserInformation().add(UserInformation.PHONE_NUMBER);
        request.getUserInformation().add(UserInformation.COMPANY_NAME);

        Document doc = (Document) marshal(request);
        setIdAttribute(doc.getChildNodes().item(0));

        return getSignedXml(doc.getFirstChild(), "#" + request.getId());
    }
}
