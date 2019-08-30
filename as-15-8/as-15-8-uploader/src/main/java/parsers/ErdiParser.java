package parsers;

import exceptions.ExceptionErdiParser;
import model.rest.ContentAddons;
import model.rest.Register;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiFunction;

abstract public class ErdiParser {

    protected int flushSize = 10000;

    abstract public void parse(InputStream is, BiFunction<Register, List<ContentAddons>, Boolean> action) throws ExceptionErdiParser;



}
