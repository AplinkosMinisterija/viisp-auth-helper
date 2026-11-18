package lt.biip.auth.generators;

import jakarta.xml.bind.JAXBException;
import lt.epaslaugos.authentication.client.AuthenticationResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseAuthResponseGeneratorTest {
    private final BaseAuthResponseGenerator generator = new BaseAuthResponseGenerator() {
    };

    @Test
    void decodesTheSoapBody() {
        assertEquals("<a/>", generator.decodeSoap(
                "<soap:Envelope><soap:Body><a/></soap:Body></soap:Envelope>"));
    }

    @Test
    void returnsEmptyWithoutABody() {
        assertEquals("", generator.decodeSoap("<soap:Envelope/>"));
        assertEquals("", generator.decodeSoap("</soap:Body><soap:Body>"));
    }

    @Test
    void unmarshalsAnAuthenticationResponse() throws Exception {
        String soap = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                + "<authentication:authenticationResponse xmlns:authentication=\"http://www.epaslaugos.lt/services/authentication\">"
                + "<authentication:ticket>5b9f2a4e-0000-4000-8000-000000000000</authentication:ticket>"
                + "</authentication:authenticationResponse></soap:Body></soap:Envelope>";

        assertEquals("5b9f2a4e-0000-4000-8000-000000000000", new AuthResponseGenerator().generateResponse(soap));
    }

    @Test
    void refusesADoctype() {
        String xml = "<!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><x>&e;</x>";

        assertThrows(JAXBException.class, () -> generator.unmarshal(new AuthenticationResponse(), xml));
    }
}
