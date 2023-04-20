package lt.biip.auth;

import lt.epaslaugos.test.auth.AuthRequestGenerator;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping ("/authRequest")
public class AuthRequestController {
    @GetMapping
    public ResponseEntity<String> generateSoap(@RequestParam String customData){
        try {
            AuthRequestGenerator dataRequest = new AuthRequestGenerator();
            String body = dataRequest.generateSoap(dataRequest.generateRequest(customData));
            return new ResponseEntity<>(body,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}

