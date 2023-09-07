package lt.biip.auth;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ping")
public class PingController {
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPingData() {
        Map<String, Number> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        JSONObject json = new JSONObject(result);
        return new ResponseEntity<>(json.toString(), HttpStatus.OK);
    }
}
