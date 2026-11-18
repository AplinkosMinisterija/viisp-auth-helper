package lt.biip.auth;

import jakarta.servlet.http.HttpServletRequest;
import lt.biip.auth.dto.GenerateTicketResponse;
import lt.biip.auth.dto.UserDataResponse;
import lt.biip.auth.generators.AuthDataRequestGenerator;
import lt.biip.auth.generators.AuthDataResponseGenerator;
import lt.biip.auth.generators.AuthRequestGenerator;
import lt.biip.auth.generators.AuthResponseGenerator;
import lt.biip.auth.generators.UnknownAuthProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static String _sendSoapToEpaslaugos(String soap) throws Exception {
        URL url = new URL("https://www.epaslaugos.lt/portal/authenticationServices/auth");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "text/xml");


        byte[] out = soap.getBytes(StandardCharsets.UTF_8);
        http.setFixedLengthStreamingMode(out.length);

        try {
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            return readSoapResponse(http);
        } finally {
            http.disconnect();
        }
    }

    static String readSoapResponse(HttpURLConnection http) throws IOException, EpaslaugosFaultException {
        int status = http.getResponseCode();
        if (status >= 400) {
            throw EpaslaugosFaultException.fromResponse(status, http.getErrorStream());
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
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
        String epaslaugosHost = "https://www.epaslaugos.lt/portal/external/services/authentication/v2/";
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

    @ExceptionHandler(EpaslaugosFaultException.class)
    public ResponseEntity<EpaslaugosErrorResponse> handleEpaslaugosFault(
            EpaslaugosFaultException fault, HttpServletRequest request) {
        if (HttpMethod.POST.matches(request.getMethod())) {
            logger.error("VIISP signing failed (HTTP {}, fault {}): {}",
                    fault.getHttpStatus(), fault.getFaultCode(), fault.getFaultString());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new EpaslaugosErrorResponse(
                    "VIISP_UNAVAILABLE", fault.getFaultCode(), "VIISP authentication service is unavailable"));
        }

        String ticket = request.getParameter("ticket");
        String faultCode = redactTicket(fault.getFaultCode(), ticket);
        String faultString = redactTicket(fault.getFaultString(), ticket);
        logger.warn("VIISP ticket rejected (HTTP {}, fault {}): {}", fault.getHttpStatus(), faultCode, faultString);
        return ResponseEntity.badRequest().body(new EpaslaugosErrorResponse(
                "VIISP_TICKET_REJECTED", faultCode, faultString == null ? "VIISP ticket was rejected" : faultString));
    }

    private static String redactTicket(String text, String ticket) {
        return text == null || ticket == null || ticket.isEmpty() ? text : text.replace(ticket, "[redacted]");
    }

    public record EpaslaugosErrorResponse(String error, String faultCode, String message) {
    }
}

