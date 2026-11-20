package lt.biip.auth;

import lt.biip.auth.dto.GenerateTicketResponse;
import lt.biip.auth.dto.UserDataResponse;
import lt.biip.auth.generators.AuthDataRequestGenerator;
import lt.biip.auth.generators.AuthDataResponseGenerator;
import lt.biip.auth.generators.AuthRequestGenerator;
import lt.biip.auth.generators.AuthResponseGenerator;
import lt.biip.auth.generators.SignatureAlgorithm;
import lt.biip.auth.generators.UnknownAuthProviderException;
import lt.epaslaugos.authentication.client.AuthenticationDataResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // VIISP production by default. VIISP test environment: VIISP_AUTH_SERVICE_URL=
    // https://test.epaslaugos.lt/services/services/auth, VIISP_LOGIN_URL=
    // https://test.epaslaugos.lt/portal/external/services/authentication/v2/
    private static final String AUTH_SERVICE_URL = viispUrl("VIISP_AUTH_SERVICE_URL",
            System.getenv("VIISP_AUTH_SERVICE_URL"),
            "https://www.epaslaugos.lt/portal/authenticationServices/auth");
    private static final String LOGIN_URL = viispUrl("VIISP_LOGIN_URL",
            System.getenv("VIISP_LOGIN_URL"),
            "https://www.epaslaugos.lt/portal/external/services/authentication/v2/");

    static {
        // Fail at startup, not on the first login, when the setting is wrong.
        SignatureAlgorithm.fromEnvironment();
    }

    /**
     * Returns the configured VIISP address or the production default. The address must be an absolute https URL
     * without a query or fragment: the ticket is appended to the login URL as {@code ?ticket=...}.
     */
    static String viispUrl(String name, String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String url = value.trim();
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(name + " is not a valid URL: " + url, e);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalStateException(name + " must be an https URL without a query or fragment: " + url);
        }
        return url;
    }

    private static String _sendSoapToEpaslaugos(String soap) throws Exception {
        URL url = new URL(AUTH_SERVICE_URL);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "text/xml");


        byte[] out = soap.getBytes(StandardCharsets.UTF_8);
        http.setFixedLengthStreamingMode(out.length);

        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        http.disconnect();

        return content.toString();
    }

    @PostMapping(path = "/sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenerateTicketResponse generateTicket(@RequestBody String customData,
                                                 @RequestParam(required = false) List<String> providers)
            throws Exception {
        AuthRequestGenerator dataRequest = new AuthRequestGenerator();
        String body = dataRequest.generateSoap(dataRequest.generateRequest(customData, providers));
        String soap = _sendSoapToEpaslaugos(body);

        AuthResponseGenerator dataResponse = new AuthResponseGenerator();
        String ticket = dataResponse.generateResponse(soap);
        String epaslaugosHost = LOGIN_URL;
        var url = String.format("%s?ticket=%s", epaslaugosHost, ticket);

        return new GenerateTicketResponse(ticket, epaslaugosHost, url);
    }

    @GetMapping(path = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDataResponse getAuthData(@RequestParam String ticket) throws Exception {
        AuthDataRequestGenerator dataRequest = new AuthDataRequestGenerator();
        String body = dataRequest.generateSoap(dataRequest.generateRequest(ticket));
        String soap = _sendSoapToEpaslaugos(body);

        AuthDataResponseGenerator dataResponse = new AuthDataResponseGenerator();

        return dataResponse.generateResponse(soap);
    }

    @ExceptionHandler(UnknownAuthProviderException.class)
    public ResponseEntity<String> handleUnknownProvider(UnknownAuthProviderException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

