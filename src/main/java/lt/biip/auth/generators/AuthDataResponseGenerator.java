package lt.biip.auth.generators;

import lt.biip.auth.dto.UserDataResponse;
import lt.epaslaugos.authentication.client.*;

import java.util.List;

/**
 * Extracts user info from XML.
 */
public class AuthDataResponseGenerator extends BaseAuthResponseGenerator {
    public UserDataResponse generateResponse(String data) throws Exception {
        AuthenticationDataResponse dataResponse = new AuthenticationDataResponse();
        AuthenticationDataResponse response = (AuthenticationDataResponse) unmarshal(dataResponse, decodeSoap(data));

        var userInformation = response.getUserInformation();
        var authenticationAttributes = response.getAuthenticationAttribute();

        return new UserDataResponse(
                getUserInformationValue(userInformation, UserInformation.FIRST_NAME),
                getUserInformationValue(userInformation, UserInformation.LAST_NAME),
                getAuthenticationAttribute(authenticationAttributes, AuthenticationAttribute.LT_PERSONAL_CODE),
                getUserInformationValue(userInformation, UserInformation.EMAIL),
                getUserInformationValue(userInformation, UserInformation.PHONE_NUMBER),
                getAuthenticationAttribute(authenticationAttributes, AuthenticationAttribute.LT_COMPANY_CODE),
                getUserInformationValue(userInformation, UserInformation.COMPANY_NAME)
        );
    }

    private String getUserInformationValue(List<UserInformationPair> information, UserInformation value) {
        return information.stream()
                .filter(v -> v.getInformation() == value)
                .map(v -> v.getValue().getStringValue()).findFirst()
                .orElse(null);
    }

    private String getAuthenticationAttribute(List<AuthenticationAttributePair> attributes, AuthenticationAttribute attribute) {
        return attributes.stream()
                .filter(v -> v.getAttribute() == attribute)
                .map(AuthenticationAttributePair::getValue)
                .findFirst()
                .orElse(null);
    }

}
