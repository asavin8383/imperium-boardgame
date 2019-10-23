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
@Table(schema = "portal", name = "proxy_parameters")
@Data
@EqualsAndHashCode(callSuper=false)
public class ProxyParameters extends AccessToolParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    private String stubUrl;
    private String proxyDnsName;
    private String proxyPort;
    private String proxyUser;
    private String proxyPassword;
}
