package lt.biip.auth;

import lt.epaslaugos.test.auth.AuthDataRequestGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticketInfo")
public class TicketInfoControler {
    @GetMapping
    public ResponseEntity<String> generateSoap(@RequestParam String ticketId) {

        String request = null;
        try {
            AuthDataRequestGenerator dataRequest = new AuthDataRequestGenerator();
            String body = dataRequest.generateSoap(dataRequest.generateRequest(ticketId));

            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}

