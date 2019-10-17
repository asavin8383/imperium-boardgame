package parsers;

import exceptions.ExceptionErdiParser;
import model.rest.ContentAddons;
import model.rest.RegisterRest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;


public class ErdiAddonsParser {

    private int flushSize = 10000;

    public ErdiAddonsParser(){}

    public ErdiAddonsParser(int flushSize){
        this.flushSize = flushSize;
    }

    public void parse(InputStream is, BiFunction<RegisterRest, List<ContentAddons>, Boolean> action) throws ExceptionErdiParser {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
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
                              BiFunction<RegisterRest, List<ContentAddons>, Boolean> action) throws XMLStreamException, JAXBException {
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    String elementName = reader.getLocalName();
                    if (elementName.equals("reg:register"))
                        readRegister(reader, action);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    break;
            }
        }
    }

    private void readRegister(XMLStreamReader reader,
                              BiFunction<RegisterRest, List<ContentAddons>, Boolean> action) throws XMLStreamException, JAXBException {
        RegisterRest register = new RegisterRest();
        readRegisterContent(reader, register, action);
    }

    private void readRegisterContent(XMLStreamReader reader,
                                     RegisterRest register,
                                     BiFunction<RegisterRest, List<ContentAddons>, Boolean> action) throws XMLStreamException, JAXBException {

        List<ContentAddons> contents = new ArrayList<>();
        Boolean needNext = true;

        while (needNext && reader.hasNext()) {
            int eType = reader.getEventType();
            if (eType == XMLStreamReader.START_ELEMENT){
                String elementName = reader.getLocalName();
                if (elementName.equals("content")){
                    ContentAddons content = readContent(reader);
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

    private Boolean flush(Boolean force, RegisterRest register, List<ContentAddons> list, BiFunction<RegisterRest, List<ContentAddons>, Boolean> action){
        if (list.size() > 0 && (list.size() >= this.flushSize || force)){
            Boolean result = action.apply(register, list);
            list.clear();
            return result;
        }
        return true;
    }

    private ContentAddons readContent(XMLStreamReader reader) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ContentAddons.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ContentAddons) unmarshaller.unmarshal(reader);
    }
}
