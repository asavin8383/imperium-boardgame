package model.catalog;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "search_system_parameters")
@Data
@EqualsAndHashCode(callSuper=false)
public class SearchSystemParameters extends AccessToolParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    private String searchSystemUrl;
    private String resultPageType;
    private String inputFieldXpath;
    private String captchaXpath;
    private String nextPageXpath;
    private String itemLinkXpath;
    private int inputDelay;
}
