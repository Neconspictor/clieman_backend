package de.necon.dateman_backend.network;

import javax.validation.ValidationException;

public class Validator {

    /**
     * Checks if a dto subclass is valid.
     * A dto is valid if it is not null and all its fields are not null, too.
     * @param dto
     * @param <T>
     * @throws ValidationException
     */
    public static <T> void validate(T dto) throws ValidationException {

        if (dto == null) throw new ValidationException("dto is null");

        var clazz = dto.getClass();

        while(clazz != null && clazz != Object.class) {
            var fields = clazz.getDeclaredFields();

            for (var field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    if (value == null) throw new ValidationException("Field value is null: " + field.getName());
                } catch (NullPointerException  e) {
                    throw new ValidationException("Argument is null (should not happen!): " + dto);
                } catch (IllegalAccessException e) {
                    throw new ValidationException("Field not accessible (should not happen!): " + field.getName());
                }
            }

            clazz = clazz.getSuperclass();
        }
    }
}