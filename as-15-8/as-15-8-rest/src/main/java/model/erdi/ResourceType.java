package model.erdi;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Immutable
@Table(schema = "sor", name = "resource_type")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResourceType implements Serializable {

    public static final Long serialVersionUID = 1L;

    private enum Type {
        DOMAIN, IP, IPV6, IPSUBNET, IPV6SUBNET, URL
    }

    @Converter(autoApply = true)
    private static class TypeConverter implements AttributeConverter<Type, String> {

        @Override
        public String convertToDatabaseColumn(Type type) {
            return type.toString().toLowerCase();
        }

        @Override
        public Type convertToEntityAttribute(String s) {
            return Type.valueOf(s.toUpperCase());
        }
    }

    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "dsc")
    @Enumerated(EnumType.STRING)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Type description;

}
