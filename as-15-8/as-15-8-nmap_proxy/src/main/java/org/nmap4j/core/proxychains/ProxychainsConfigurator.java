package org.nmap4j.core.proxychains;

import java.io.*;

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
        try(InputStreamReader isReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("proxychains.conf"))){
            try(BufferedReader br = new BufferedReader(isReader)){
                try(PrintWriter writer = new PrintWriter(new File(CONFIG_FILE_PATH))){
                    br.lines().forEach(line -> writer.println(line));
                    writer.println(this.protocol+" "+this.host+" "+this.port);
                }
            }
        } catch (Exception ex){
            throw ex;
        }
    }
}
