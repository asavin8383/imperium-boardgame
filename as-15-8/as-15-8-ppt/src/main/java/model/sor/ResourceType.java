package model.sor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Immutable
@Table(schema = "sor", name = "resource_type")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResourceType implements Serializable {

    public static final Long serialVersionUID = 1L;

    public enum Description {
        // DOMAIN, IP, IPV6, IPSUBNET, IPV6SUBNET, URL
        domain, ip, ipv6, ipsubnet, ipv6subnet, url
    }

    // @Converter(autoApply = true)
    public static class DescriptionConverter implements AttributeConverter<Description, String> {

        @Override
        public String convertToDatabaseColumn(Description type) {
            return type.toString().toLowerCase();
        }

        @Override
        public Description convertToEntityAttribute(String s) {
            return Description.valueOf(s.toUpperCase());
        }
    }

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "dsc")
    @Enumerated(EnumType.STRING)
    // @Convert(converter = DescriptionConverter.class)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Description description;

    @OneToMany(mappedBy = "resourceType")
    @JsonIgnore
    private List<ContentResource> contentResources;

}
