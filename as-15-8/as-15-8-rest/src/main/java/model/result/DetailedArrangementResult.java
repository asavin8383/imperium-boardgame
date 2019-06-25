package model.result;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Creation date: 31.05.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "detail_results")
@Immutable
@Data
public class DetailedArrangementResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String responseErrorCode;
    private boolean responseError;
    private Integer httpStatus;
    private Integer httpStatusEtalon;
    private String httpHeaders;
    private String httpHeadersEtalon;
    private Integer pageSize;
    private Integer pageSizeEtalon;
    private Integer keyWordsCount;
    private Integer linkCount;
    private Integer domainNameCount;
    private String pageUrlFinal;
    private Integer similarityOriginPercent;
    private String stubScoreInfo;
    private Boolean redirectionDetected;
}
