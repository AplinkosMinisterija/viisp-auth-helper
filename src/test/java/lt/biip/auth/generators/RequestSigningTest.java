package lt.biip.auth.generators;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestSigningTest {
    private static final String DSIG_NS = XMLSignature.XMLNS;

    private static KeyPair keys;

    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keys = generator.generateKeyPair();
    }

    @Test
    void signsTheAuthenticationRequestWithRsaSha256ByDefault() throws Exception {
        String xml = new AuthRequestGenerator(keys.getPrivate(), keys.getPublic(), SignatureAlgorithm.parse(null))
                .generateRequest("{}", List.of("AUTH_LOGIN_PASS"));

        Document doc = parse(xml);
        assertAlgorithms(doc, SignatureMethod.RSA_SHA256, DigestMethod.SHA256);
        assertTrue(validate(doc, true), "signature must verify with the signer's public key");
    }

    @Test
    void signsTheAuthenticationDataRequestWithRsaSha256() throws Exception {
        String xml = new AuthDataRequestGenerator(keys.getPrivate(), keys.getPublic(), SignatureAlgorithm.SHA256)
                .generateRequest("ticket-1");

        Document doc = parse(xml);
        assertAlgorithms(doc, SignatureMethod.RSA_SHA256, DigestMethod.SHA256);
        assertTrue(validate(doc, true), "signature must verify with the signer's public key");
    }

    @Test
    void keepsRsaSha1WhenAskedTo() throws Exception {
        String xml = new AuthDataRequestGenerator(keys.getPrivate(), keys.getPublic(), SignatureAlgorithm.SHA1)
                .generateRequest("ticket-1");

        Document doc = parse(xml);
        assertAlgorithms(doc, SignatureMethod.RSA_SHA1, DigestMethod.SHA1);
        // The JDK's secure validation refuses SHA-1 signatures, so check this one without it.
        assertTrue(validate(doc, false), "signature must verify with the signer's public key");
    }

    @Test
    void aChangedRequestNoLongerVerifies() throws Exception {
        String xml = new AuthDataRequestGenerator(keys.getPrivate(), keys.getPublic(), SignatureAlgorithm.SHA256)
                .generateRequest("ticket-1");

        Document doc = parse(xml.replace("ticket-1", "ticket-2"));
        assertFalse(validate(doc, true), "a changed ticket must break the signature");
    }

    @Test
    void readsTheAlgorithmFromItsSetting() {
        assertEquals(SignatureAlgorithm.SHA256, SignatureAlgorithm.parse(null));
        assertEquals(SignatureAlgorithm.SHA256, SignatureAlgorithm.parse(" "));
        assertEquals(SignatureAlgorithm.SHA256, SignatureAlgorithm.parse("SHA256"));
        assertEquals(SignatureAlgorithm.SHA1, SignatureAlgorithm.parse(" sha1 "));
        assertThrows(IllegalStateException.class, () -> SignatureAlgorithm.parse("sha512"));
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        doc.getDocumentElement().setIdAttribute("id", true);
        return doc;
    }

    private static void assertAlgorithms(Document doc, String signatureMethod, String digestMethod) {
        assertEquals(signatureMethod, onlyElement(doc, "SignatureMethod").getAttribute("Algorithm"));
        assertEquals(digestMethod, onlyElement(doc, "DigestMethod").getAttribute("Algorithm"));
    }

    private static boolean validate(Document doc, boolean secureValidation) throws Exception {
        DOMValidateContext context = new DOMValidateContext(keys.getPublic(), onlyElement(doc, "Signature"));
        context.setProperty("org.jcp.xml.dsig.secureValidation", secureValidation);
        XMLSignature signature = XMLSignatureFactory.getInstance("DOM").unmarshalXMLSignature(context);
        return signature.validate(context);
    }

    private static Element onlyElement(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS(DSIG_NS, localName);
        assertEquals(1, nodes.getLength(), "expected one ds:" + localName);
        return (Element) nodes.item(0);
    }
}
