package lt.biip.auth;

import lt.epaslaugos.test.auth.AuthDataRequestGenerator;
import lt.epaslaugos.test.auth.AuthDataResponseGenerator;
import lt.epaslaugos.test.auth.AuthRequestGenerator;
import lt.epaslaugos.test.auth.AuthResponseGenerator;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<String> generateTicket(@RequestBody String customData) {
        try {
            AuthRequestGenerator dataRequest = new AuthRequestGenerator();
            String body = dataRequest.generateSoap(dataRequest.generateRequest(customData));
            String soap = _sendSoapToEpaslaugos(body);

            AuthResponseGenerator dataResponse = new AuthResponseGenerator();
            String ticket = dataResponse.generateResponse(soap);
            String epaslaugosHost = "https://www.epaslaugos.lt/portal/external/services/authentication/v2/";

            Map<String, String> result = new HashMap<>();
            result.put("ticket", ticket);
            result.put("host", epaslaugosHost);
            result.put("url", String.format("%s?ticket=%s", epaslaugosHost, ticket));
            JSONObject json = new JSONObject(result);

            return new ResponseEntity<>(json.toString(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping(path = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAuthData(@RequestParam String ticket) {
        try {
            AuthDataRequestGenerator dataRequest = new AuthDataRequestGenerator();
            String body = dataRequest.generateSoap(dataRequest.generateRequest(ticket));
            String soap = _sendSoapToEpaslaugos(body);

            AuthDataResponseGenerator dataResponse = new AuthDataResponseGenerator();
            Map<String, String> userData = dataResponse.generateResponse(soap);

            JSONObject json = new JSONObject(userData);

            return new ResponseEntity<>(json.toString(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}

