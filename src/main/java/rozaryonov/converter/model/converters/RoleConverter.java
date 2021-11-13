package rozaryonov.converter.model.converters;

import rozaryonov.converter.model.Role;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

//@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) return null;
        return role.toString();
    }

    @Override
    public Role convertToEntityAttribute(String id) {
        if (id == null) return null;

        return Role.valueOf(id);
    }
}
