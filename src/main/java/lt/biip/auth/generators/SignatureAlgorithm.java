package lt.biip.auth.generators;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import java.util.Locale;

/**
 * Algorithms used to sign requests sent to VIISP.
 * <p>
 * VIISP stops accepting RSA-SHA1 / SHA-1 signatures in its test environment on 2027-02-01 and in production on
 * 2027-05-01. SHA-256 is the default; SHA-1 stays available through {@code VIISP_SIGNATURE_ALGORITHM=sha1} for an
 * environment that has to keep the old algorithm until then.
 */
public enum SignatureAlgorithm {
    SHA1(SignatureMethod.RSA_SHA1, DigestMethod.SHA1),
    SHA256(SignatureMethod.RSA_SHA256, DigestMethod.SHA256);

    public static final String ENV_VARIABLE = "VIISP_SIGNATURE_ALGORITHM";

    private final String signatureMethod;
    private final String digestMethod;

    SignatureAlgorithm(String signatureMethod, String digestMethod) {
        this.signatureMethod = signatureMethod;
        this.digestMethod = digestMethod;
    }

    public String signatureMethod() {
        return signatureMethod;
    }

    public String digestMethod() {
        return digestMethod;
    }

    public static SignatureAlgorithm fromEnvironment() {
        return parse(System.getenv(ENV_VARIABLE));
    }

    static SignatureAlgorithm parse(String value) {
        if (value == null || value.isBlank()) {
            return SHA256;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "sha1" -> SHA1;
            case "sha256" -> SHA256;
            default -> throw new IllegalStateException(
                    ENV_VARIABLE + " must be \"sha1\" or \"sha256\", got \"" + value + "\"");
        };
    }
}
