package lt.biip.auth.generators;

import lt.epaslaugos.authentication.client.AuthenticationProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthProvidersTest {
    @Test
    void offersEveryProviderWhenNoneIsRequested() {
        assertEquals(AuthProviders.ALL, AuthProviders.resolve(null));
        assertEquals(AuthProviders.ALL, AuthProviders.resolve(List.of()));
    }

    @Test
    void keepsOnlyTheRequestedProvider() {
        assertEquals(
                List.of(AuthenticationProvider.AUTH_LT_GOVERNMENT_EMPLOYEE_CARD),
                AuthProviders.resolve(List.of("AUTH_LT_GOVERNMENT_EMPLOYEE_CARD")));
    }

    @Test
    void keepsTheRequestedOrderAndDropsRepeats() {
        assertEquals(
                List.of(AuthenticationProvider.AUTH_LOGIN_PASS, AuthenticationProvider.AUTH_LT_BANK),
                AuthProviders.resolve(List.of("AUTH_LOGIN_PASS", "AUTH_LT_BANK", "AUTH_LOGIN_PASS")));
    }

    @Test
    void refusesAProviderThatIsNotOffered() {
        assertThrows(
                UnknownAuthProviderException.class,
                () -> AuthProviders.resolve(List.of("AUTH_STORK")));
    }

    @Test
    void refusesANameThatIsNotAProvider() {
        assertThrows(UnknownAuthProviderException.class, () -> AuthProviders.resolve(List.of("bank")));
    }
}
