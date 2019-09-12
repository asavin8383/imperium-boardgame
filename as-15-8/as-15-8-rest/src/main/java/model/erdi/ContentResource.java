package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import model.Views;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(schema = "sor", name = "content_resources")
@Data
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentResource implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id")
    @JsonIgnore
    private FormalErdi content;

    @NotNull
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String value;

    @JsonIgnore
    @ToString.Include
    private LocalDateTime ts;

    @Enumerated(EnumType.STRING)
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Type resourceType;

    @JsonIgnore
    @ToString.Include
    private Long contentVersionId;

}
