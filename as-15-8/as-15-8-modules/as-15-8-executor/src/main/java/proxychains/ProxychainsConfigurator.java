package proxychains;

import java.io.*;
import java.util.Objects;

public class ProxychainsConfigurator {

    private static final String CONFIG_FILE_PATH = "/etc/proxychains/proxychains.conf";

    private String protocol;
    private String host;
    private String port;

    public ProxychainsConfigurator(String protocol, String host, String port){
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public void configure() throws IOException {
        try(InputStreamReader isReader = new InputStreamReader(
                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("proxychains.conf")))){

            File configFile = new File(CONFIG_FILE_PATH);
            assert !configFile.exists() || configFile.delete();

            try(BufferedReader br = new BufferedReader(isReader)){
                try(PrintWriter writer = new PrintWriter(configFile)){
                    br.lines().forEach(writer::println);
                    writer.println(this.protocol+" "+this.host+" "+this.port);
                }
            }
        }
    }
}
