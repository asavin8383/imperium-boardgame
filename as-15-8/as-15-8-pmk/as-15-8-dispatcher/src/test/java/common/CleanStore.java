package common;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class CleanStore {

    @Test
    public void clean() throws IOException {
        try {
            FileUtils.deleteDirectory(new File("/tmp/kafka-streams/"));
            FileUtils.forceMkdir(new File("/tmp/kafka-streams/"));
        } catch(Exception ex){
            throw ex;
        }
    }

}
