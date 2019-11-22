package model;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import model.soap.Envelope;

import java.io.*;

/**
 * User: asinjavin
 * Date: 22.11.2019
 * Time: 14:45
 */
public class Run
{
    public static void main(String[] args) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);


        ObjectMapper objectMapper = new ObjectMapper();

        File file = new File("./src/main/webapp/1.xml");
        InputStream stream = new FileInputStream(file);


        Envelope res = xmlMapper.readValue(file, Envelope.class);
        System.out.println("res = " + res);

        objectMapper.writeValue(System.out, res.getBody().getGetUpdatedObjects().getOperation());
    }


    public static String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
