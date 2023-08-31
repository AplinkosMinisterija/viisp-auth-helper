package lt.epaslaugos.test.auth;

import lt.atea.vaiisis.authentication.model.xml.AuthenticationAttribute;
import lt.atea.vaiisis.authentication.model.xml.AuthenticationProviderXml;
import lt.atea.vaiisis.authentication.model.xml.AuthenticationRequestXml;
import lt.atea.vaiisis.authentication.model.xml.UserInformation;
import org.w3c.dom.Document;

/**
 * Generates example authenticationRequest XML element.
 */
public class AuthRequestGenerator extends BaseAuthRequestGenerator {
    public static final String PID = System.getenv("VIISP_PID");
    public static final String POSTBACK_URL = System.getenv("VIISP_POSTBACK_URL");

    public static void main(String[] args) throws Exception {}

    public String generateRequest(String customData) throws Exception {
        AuthenticationRequestXml request = new AuthenticationRequestXml();
        request.setId(SIGNED_NODE_ID);

        request.setPid(PID);
        request.setCustomData(customData);
        request.setPostbackUrl(POSTBACK_URL);
        request.getAuthenticationAttribute().add(AuthenticationAttribute.LT_PERSONAL_CODE);
        request.getAuthenticationAttribute().add(AuthenticationAttribute.LT_COMPANY_CODE);
        request.getAuthenticationAttribute().add(AuthenticationAttribute.EIDAS_EID);

        request.getAuthenticationProvider().add(AuthenticationProviderXml.AUTH_PROVIDER_LT_IDENTITY_CARD);
        request.getAuthenticationProvider().add(AuthenticationProviderXml.AUTH_PROVIDER_LT_BANK);
        request.getAuthenticationProvider().add(AuthenticationProviderXml.AUTH_PROVIDER_SIGNATURE);
        request.getAuthenticationProvider().add(AuthenticationProviderXml.AUTH_PROVIDER_LT_EMPLOYEE_CARD);
        request.getAuthenticationProvider().add(AuthenticationProviderXml.AUTH_PROVIDER_STORK);
        request.getAuthenticationProvider().add(AuthenticationProviderXml.AUTH_EIDAS);

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
