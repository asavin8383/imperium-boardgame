package parsers;

import exceptions.ExceptionErdiParser;
import model.rest.ContentFull;
import model.rest.Register;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;


public class ErdiFullParser {

    private int flushSize = 10000;

    public ErdiFullParser(){}

    public ErdiFullParser(int flushSize){
        this.flushSize = flushSize;
    }

    public void parse(InputStream is, BiFunction<Register, List<ContentFull>, Boolean> action) throws ExceptionErdiParser {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;

        try {
            reader = inputFactory.createXMLStreamReader(is);
            readDocument(reader, action);
        } catch (JAXBException | XMLStreamException e) {
            throw new ExceptionErdiParser(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void readDocument(XMLStreamReader reader,
                              BiFunction<Register, List<ContentFull>, Boolean> action) throws XMLStreamException, JAXBException {
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    String elementName = reader.getLocalName();
                    if (elementName.equals("register"))
                        readRegister(reader, action);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    break;
            }
        }
    }

    private void readRegister(XMLStreamReader reader,
                              BiFunction<Register, List<ContentFull>, Boolean> action) throws XMLStreamException, JAXBException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        Register register = new Register(
                reader.getAttributeValue(null, "updateTime"),
                reader.getAttributeValue(null, "updateTimeUrgently"),
                reader.getAttributeValue(null, "formatVersion"),
                reader.getNamespaceURI("reg"),
                reader.getNamespaceURI("tns")
        );

        readRegisterContent(reader, register, action);
    }

    private void readRegisterContent(XMLStreamReader reader,
                                     Register register,
                                     BiFunction<Register, List<ContentFull>, Boolean> action) throws XMLStreamException, JAXBException {

        List<ContentFull> contents = new ArrayList<>();
        Boolean needNext = true;

        while (needNext && reader.hasNext()) {
            int eType = reader.getEventType();
            if (eType == XMLStreamReader.START_ELEMENT){
                String elementName = reader.getLocalName();
                if (elementName.equals("content")){
                    ContentFull content = readContent(reader);
                    contents.add(content);
                    needNext = flush(false, register, contents, action);
                    continue;
                }
            }
            reader.next();
        }

        if (needNext){
            flush(true, register, contents, action);
        }
    }

    private Boolean flush(Boolean force, Register register, List<ContentFull> list, BiFunction<Register, List<ContentFull>, Boolean> action){
        if (list.size() > 0 && (list.size() >= this.flushSize || force)){
            Boolean result = action.apply(register, list);
            list.clear();
            return result;
        }
        return true;
    }

    private ContentFull readContent(XMLStreamReader reader) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ContentFull.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ContentFull) unmarshaller.unmarshal(reader);
    }
}
