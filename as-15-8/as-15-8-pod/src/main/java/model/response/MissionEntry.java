package model.response;

import lombok.Data;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;


@Data
public class MissionEntry
{
    public String id;
    public String docNum;
    public Integer typeCheck;
    @ToString.Exclude
    public String docFileData;
    public String dateApproved;

    public Date getDateApprovedDate() throws ParseException {
        if (StringUtils.isEmpty(dateApproved)){
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return dateFormat.parse(dateApproved);
    }

    public byte[] getDocFileDataBytes() throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(docFileData)){
            return new byte[]{};
        }
        byte[] decodedString = Base64.getDecoder().decode(docFileData.getBytes(StandardCharsets.UTF_8));
        return decodedString;
    }
}
