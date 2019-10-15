package parsers;

import exceptions.ExceptionErdiParser;
import model.rest.ContentAddons;
import model.rest.RegisterRest;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiFunction;

abstract public class ErdiParser {

    protected int flushSize = 10000;

    abstract public void parse(InputStream is, BiFunction<RegisterRest, List<ContentAddons>, Boolean> action) throws ExceptionErdiParser;



}
