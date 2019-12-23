package rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActAttachment {

    public enum ActAttachmentType{SCREENSHOT, NMAP_LOG}

    private Long id;
    private ActAttachmentType type;
    private String value;
}
