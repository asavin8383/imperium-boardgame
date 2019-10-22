package proxychains;

import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ProxychainsConfigurator implements Closeable{

    private static final String CONFIG_DIR_PREFIX= "proxychains_";

    private String protocol;
    private String host;
    private String port;

    @Getter
    private Path configFile;

    public ProxychainsConfigurator(String protocol, String host, String port) throws IOException {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.configFile = Files.createTempFile(CONFIG_DIR_PREFIX, ".conf");
        configure(this.configFile);
    }

    private void configure(Path configFile) throws IOException {
        try(InputStreamReader isReader = new InputStreamReader(
                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("proxychains.conf")))){

            try(BufferedReader br = new BufferedReader(isReader)){
                try(PrintWriter writer = new PrintWriter(configFile.toFile())){
                    br.lines().forEach(writer::println);
                    writer.println(this.protocol+" "+this.host+" "+this.port);
                }
            }
        }
    }

    @Override
    public void close() {
        if(configFile != null && configFile.toFile().exists())
            configFile.toFile().delete();
    }
}
