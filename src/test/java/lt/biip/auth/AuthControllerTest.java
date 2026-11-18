package lt.biip.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private static final String TICKET = "32ad7f72-3048-4d4a-8fca-c16257b69593";

    @Test
    void readsSuccessfulResponseFromTheInputStream() throws Exception {
        var connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream("<response/>".getBytes(StandardCharsets.UTF_8)));

        assertEquals("<response/>", AuthController.readSoapResponse(connection));
    }

    @Test
    void readsSoap11FaultFromTheErrorStream() throws Exception {
        var fault = readFault(500, """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <soap:Fault>
                      <faultcode>soap:Client</faultcode>
                      <faultstring>Ticket has already been used</faultstring>
                    </soap:Fault>
                  </soap:Body>
                </soap:Envelope>
                """);

        assertEquals(500, fault.getHttpStatus());
        assertEquals("soap:Client", fault.getFaultCode());
        assertEquals("Ticket has already been used", fault.getFaultString());
    }

    @Test
    void readsSoap12FaultFromTheErrorStream() throws Exception {
        var fault = readFault(400, """
                <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
                  <env:Body>
                    <env:Fault>
                      <env:Code><env:Value>env:Sender</env:Value></env:Code>
                      <env:Reason><env:Text xml:lang="en">Ticket has expired</env:Text></env:Reason>
                    </env:Fault>
                  </env:Body>
                </env:Envelope>
                """);

        assertEquals(400, fault.getHttpStatus());
        assertEquals("env:Sender", fault.getFaultCode());
        assertEquals("Ticket has expired", fault.getFaultString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "Service unavailable")
    void stillThrowsWhenNoFaultCanBeParsed(String body) throws Exception {
        var fault = readFault(503, body);

        assertEquals(503, fault.getHttpStatus());
        assertNull(fault.getFaultCode());
        assertNull(fault.getFaultString());
    }

    @Test
    void mapsDataFaultToRejectedTicketAndRedactsEchoedTicket() {
        var request = new MockHttpServletRequest("GET", "/auth/data");
        request.addParameter("ticket", TICKET);
        var fault = new EpaslaugosFaultException(500, "soap:Client", "Expired ticket " + TICKET);

        var response = new AuthController().handleEpaslaugosFault(fault, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VIISP_TICKET_REJECTED", response.getBody().error());
        assertEquals("soap:Client", response.getBody().faultCode());
        assertEquals("Expired ticket [redacted]", response.getBody().message());
    }

    @Test
    void returnsGenericMessageWhenDataFaultHasNoText() {
        var request = new MockHttpServletRequest("GET", "/auth/data");
        var fault = new EpaslaugosFaultException(500, null, null);

        var response = new AuthController().handleEpaslaugosFault(fault, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VIISP_TICKET_REJECTED", response.getBody().error());
        assertNull(response.getBody().faultCode());
        assertEquals("VIISP ticket was rejected", response.getBody().message());
    }

    @Test
    void mapsSignFaultToBadGateway() {
        var request = new MockHttpServletRequest("POST", "/auth/sign");
        var fault = new EpaslaugosFaultException(500, "soap:Server", "Signing service failed");

        var response = new AuthController().handleEpaslaugosFault(fault, request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("VIISP_UNAVAILABLE", response.getBody().error());
        assertEquals("VIISP authentication service is unavailable", response.getBody().message());
    }

    private EpaslaugosFaultException readFault(int status, String body) throws Exception {
        var connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(status);
        when(connection.getErrorStream()).thenReturn(
                body == null ? null : new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

        return assertThrows(EpaslaugosFaultException.class, () -> AuthController.readSoapResponse(connection));
    }
}
