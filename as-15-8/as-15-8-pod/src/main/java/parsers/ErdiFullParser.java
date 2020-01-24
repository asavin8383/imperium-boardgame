package parsers;

import exceptions.ExceptionErdiParser;
import model.rest.ContentDelete;
import model.rest.ContentFull;
import model.rest.ContentRest;
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


public class ErdiFullParser {

    private int flushSize = 10000;
    private long maxContentSize = -1;

    public ErdiFullParser(){}

    public void parse(InputStream is, BiFunction<RegisterRest, List<ContentRest>, Boolean> action) throws ExceptionErdiParser {
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

    public int getFlushSize() {
        return flushSize;
    }

    public void setFlushSize(int flushSize) {
        this.flushSize = flushSize;
    }

    public long getMaxContentSize() {
        return maxContentSize;
    }

    public void setMaxContentSize(long maxContentSize) {
        this.maxContentSize = maxContentSize;
    }

    private void readDocument(XMLStreamReader reader,
                              BiFunction<RegisterRest, List<ContentRest>, Boolean> action) throws XMLStreamException, JAXBException {
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
                              BiFunction<RegisterRest, List<ContentRest>, Boolean> action) throws XMLStreamException, JAXBException {

        RegisterRest register = new RegisterRest(
                reader.getAttributeValue(null, "updateTime"),
                reader.getAttributeValue(null, "updateTimeUrgently"),
                reader.getAttributeValue(null, "formatVersion"),
                reader.getNamespaceURI("reg"),
                reader.getNamespaceURI("tns")
        );

        readRegisterContent(reader, register, action);
    }

    private void readRegisterContent(XMLStreamReader reader,
                                     RegisterRest register,
                                     BiFunction<RegisterRest, List<ContentRest>, Boolean> action) throws XMLStreamException, JAXBException {

        List<ContentRest> contents = new ArrayList<>();
        Boolean needNext = true;

        long contentCounter = 0;
        while (needNext && reader.hasNext() && (maxContentSize < 0 || contentCounter < maxContentSize)) {
            int eType = reader.getEventType();
            if (eType == XMLStreamReader.START_ELEMENT) {
                String elementName = reader.getLocalName();
                try {
                    if (elementName.equals("content")) {
                        ContentFull content = readContentFull(reader);
                        contents.add(content);
                        contentCounter++;
                        needNext = flush(false, register, contents, action);
                        continue;
                    }
                    else if (elementName.equals("delete")) {
                        ContentDelete content = readContentDelete(reader);
                        contents.add(content);
                        contentCounter++;
                        needNext = flush(false, register, contents, action);
                        continue;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing content: " + elementName);
                    // e.printStackTrace();
                    // ignore
                    e.printStackTrace();
                    throw e;
                }
            }
            reader.next();
        }

        if (contents.size() > 0) {      // слив остатка
            needNext = flush(true, register, contents, action);
        }
        if (needNext){                              // завершаем - пустым массивом
            flush(true, register, new ArrayList<>(), action);
        }
    }

    private Boolean flush(Boolean force,
                          RegisterRest register,
                          List<ContentRest> list,
                          BiFunction<RegisterRest, List<ContentRest>, Boolean> action)
    {
        if (list.size() >= this.flushSize || force){
            Boolean result = action.apply(register, list);
            list.clear();
            return result;
        }
        return true;
    }

    private ContentFull readContentFull(XMLStreamReader reader) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ContentFull.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ContentFull) unmarshaller.unmarshal(reader);
    }

    private ContentDelete readContentDelete(XMLStreamReader reader) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ContentDelete.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ContentDelete) unmarshaller.unmarshal(reader);
    }
}
