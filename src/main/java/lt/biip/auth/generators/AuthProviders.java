package lt.biip.auth.generators;

import lt.epaslaugos.authentication.client.AuthenticationProvider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AuthProviders {
    public static final List<AuthenticationProvider> ALL = List.of(
            AuthenticationProvider.AUTH_LT_BANK,
            AuthenticationProvider.AUTH_SIGNATURE_PROVIDER,
            AuthenticationProvider.AUTH_LT_IDENTITY_CARD,
            AuthenticationProvider.AUTH_LT_GOVERNMENT_EMPLOYEE_CARD,
            AuthenticationProvider.AUTH_LOGIN_PASS,
            AuthenticationProvider.AUTH_EIDAS);

    private AuthProviders() {
    }

    public static List<AuthenticationProvider> resolve(List<String> names) {
        if (names == null || names.isEmpty()) {
            return ALL;
        }

        Set<AuthenticationProvider> providers = new LinkedHashSet<>();
        for (String name : names) {
            providers.add(byName(name));
        }

        return List.copyOf(providers);
    }

    private static AuthenticationProvider byName(String name) {
        AuthenticationProvider provider;
        try {
            provider = AuthenticationProvider.valueOf(name.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown authentication provider: " + name);
        }

        if (!ALL.contains(provider)) {
            throw new IllegalArgumentException("Unsupported authentication provider: " + name);
        }

        return provider;
    }
}
