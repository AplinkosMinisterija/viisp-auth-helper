package lt.biip.auth.generators;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;

@SuppressWarnings("restriction")
public abstract class BaseAuthRequestGenerator {
    public static final String SIGNED_NODE_ID = "uniqueNodeId";
    private static final char[] PASSWORD = System.getenv("KEYSTORE_PASSWORD").toCharArray();
    private static final XMLSignatureFactory XML_SIGNATURE_FACTORY = XMLSignatureFactory.getInstance("DOM");

    protected PrivateKey privateKey = null;
    protected PublicKey publicKey = null;

    public BaseAuthRequestGenerator() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(Base64.getDecoder().decode(System.getenv("KEYSTORE_BASE64"))), PASSWORD);

            for (Enumeration<String> e = keyStore.aliases(); e.hasMoreElements(); ) {
                String alias = e.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    privateKey = (PrivateKey) keyStore.getKey(alias, PASSWORD);
                    X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                    publicKey = cert.getPublicKey();
                    break;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Node marshal(Object data) throws JAXBException {
        String packageName = data.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);

        DOMResult result = new DOMResult();
        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(data, result);
        return result.getNode();
    }

    public String getSignedXml(Node node, String referenceUri) throws Exception {
        signNode(node, referenceUri);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        trans.transform(new DOMSource(node), new StreamResult(output));

        return output.toString(StandardCharsets.UTF_8);
    }

    public void signNode(Node node, String uri) throws Exception {
        DOMSignContext dsc = new DOMSignContext(privateKey, node);
        XMLSignatureFactory fac = XML_SIGNATURE_FACTORY;

        List<String> prefixList = new ArrayList<>();
        prefixList.add(node.getPrefix());
        C14NMethodParameterSpec spec = new ExcC14NParameterSpec(prefixList);
        List<Transform> transforms = new ArrayList<>();
        transforms.add(fac.newTransform(CanonicalizationMethod.ENVELOPED, (TransformParameterSpec) null));
        transforms.add(fac.newTransform(CanonicalizationMethod.EXCLUSIVE, spec));

        Reference ref = fac.newReference(uri, fac.newDigestMethod(DigestMethod.SHA1, null), transforms, null, null);
        SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, spec), fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref));

        KeyInfoFactory kif = fac.getKeyInfoFactory();

        KeyValue kv = kif.newKeyValue(publicKey);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(dsc);
    }

    /**
     * Sets ID attribute (named "id") for given node.
     * <p>
     * Necessary for XML signing/validation when running on JDK 7
     *
     * @param node node to set ID attribute for
     * @see http://stackoverflow.com/questions/17331187/xml-dig-sig-error-after-upgrade-to-java7u25
     */
    public void setIdAttribute(Node node) {
        Node idAttribute = node.getAttributes().getNamedItem("id");
        if (idAttribute != null) {
            ((Element) node).setIdAttribute("id", true);
        }
    }

    public String generateSoap(String data) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" \n" +
                "xmlns:aut=\"http://www.epaslaugos.lt/services/authentication\" \n" +
                "xmlns:xd=\"http://www.w3.org/2000/09/xmldsig#\"> \n" +
                " <soapenv:Header/> \n" +
                " <soapenv:Body>\n" +
                StringUtils.substringAfter(data, "?>") + "\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

}
