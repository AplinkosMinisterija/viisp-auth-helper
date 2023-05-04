package lt.epaslaugos.test.auth;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("restriction")
public abstract class BaseAuthResponseGenerator {

    public Object unmarshal(Object data, String file) throws JAXBException {
        String packageName = data.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        return unmarshaller.unmarshal(new StreamSource(new StringReader(file)));
    }

    public String decodeSoap(String data) {
        Pattern pattern = Pattern.compile("<soap:Body>(.*)</soap:Body>");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

}
