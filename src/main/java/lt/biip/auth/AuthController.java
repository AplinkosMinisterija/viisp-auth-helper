package lt.biip.auth;

import lt.biip.auth.dto.GenerateTicketResponse;
import lt.biip.auth.dto.UserDataResponse;
import lt.biip.auth.generators.AuthDataRequestGenerator;
import lt.biip.auth.generators.AuthDataResponseGenerator;
import lt.biip.auth.generators.AuthRequestGenerator;
import lt.biip.auth.generators.AuthResponseGenerator;
import lt.epaslaugos.authentication.client.AuthenticationDataResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static String _sendSoapToEpaslaugos(String soap) throws Exception {
        URL url = new URL("https://www.epaslaugos.lt/portal/authenticationServices/auth");
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
    public GenerateTicketResponse generateTicket(@RequestBody String customData) throws Exception {
        AuthRequestGenerator dataRequest = new AuthRequestGenerator();
        String body = dataRequest.generateSoap(dataRequest.generateRequest(customData));
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
}

