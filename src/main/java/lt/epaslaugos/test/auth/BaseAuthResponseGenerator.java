package lt.epaslaugos.test.auth;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.dsig.*;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@SuppressWarnings("restriction")
public abstract class BaseAuthResponseGenerator {

    public Object unmarshal(Object data, String file) throws JAXBException {
        String packageName = data.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        Unmarshaller u = jc.createUnmarshaller();
        StringBuffer xmlStr = new StringBuffer( file );
        return unmarshaller.unmarshal(new StreamSource( new StringReader( xmlStr.toString() ) ));
    }

    public String decodeSoap(String data) throws Exception {
        Pattern pattern = Pattern.compile("<soap:Body>(.*)</soap:Body>");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

}
