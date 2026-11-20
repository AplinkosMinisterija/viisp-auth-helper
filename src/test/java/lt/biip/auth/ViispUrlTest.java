package lt.biip.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViispUrlTest {
    private static final String DEFAULT = "https://www.epaslaugos.lt/portal/external/services/authentication/v2/";

    @Test
    void usesTheProductionAddressWhenNothingIsSet() {
        assertEquals(DEFAULT, AuthController.viispUrl("VIISP_LOGIN_URL", null, DEFAULT));
        assertEquals(DEFAULT, AuthController.viispUrl("VIISP_LOGIN_URL", "  ", DEFAULT));
    }

    @Test
    void takesTheTestEnvironmentAddress() {
        assertEquals("https://test.epaslaugos.lt/portal/external/services/authentication/v2/",
                AuthController.viispUrl("VIISP_LOGIN_URL",
                        " https://test.epaslaugos.lt/portal/external/services/authentication/v2/ ", DEFAULT));
    }

    @Test
    void refusesAnAddressTheTicketCannotBeAppendedTo() {
        assertThrows(IllegalStateException.class, () -> AuthController.viispUrl("VIISP_LOGIN_URL",
                "https://test.epaslaugos.lt/portal/external/services/authentication/v2/?language=lt", DEFAULT));
        assertThrows(IllegalStateException.class, () -> AuthController.viispUrl("VIISP_LOGIN_URL",
                "https://test.epaslaugos.lt/v2/#top", DEFAULT));
    }

    @Test
    void refusesAnAddressWithoutTls() {
        assertThrows(IllegalStateException.class, () -> AuthController.viispUrl("VIISP_AUTH_SERVICE_URL",
                "http://test.epaslaugos.lt/services/services/auth", DEFAULT));
        assertThrows(IllegalStateException.class, () -> AuthController.viispUrl("VIISP_AUTH_SERVICE_URL",
                "test.epaslaugos.lt/services/services/auth", DEFAULT));
        assertThrows(IllegalStateException.class, () -> AuthController.viispUrl("VIISP_AUTH_SERVICE_URL",
                "https://exa mple.lt/", DEFAULT));
    }
}
