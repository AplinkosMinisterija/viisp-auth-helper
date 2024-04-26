package lt.biip.auth;

import lt.biip.auth.generators.AuthRequestGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authRequest")
public class AuthRequestController {
    @GetMapping
    public ResponseEntity<String> generateSoap(@RequestParam String customData) {
        try {
            AuthRequestGenerator dataRequest = new AuthRequestGenerator();
            String body = dataRequest.generateSoap(dataRequest.generateRequest(customData));
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}

