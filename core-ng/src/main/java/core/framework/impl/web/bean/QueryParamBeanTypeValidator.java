package core.framework.impl.web.bean;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.api.web.service.QueryParam;
import core.framework.impl.reflect.Fields;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.JSONTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author neo
 */
final class QueryParamBeanTypeValidator implements TypeVisitor {
    private final DataTypeValidator validator;
    private final Set<String> visitedQueryParams = Sets.newHashSet();

    QueryParamBeanTypeValidator(Type beanType) {
        validator = new DataTypeValidator(beanType);
        validator.allowedValueClass = this::allowedValueClass;
        validator.visitor = this;
    }

    public void validate() {
        validator.validate();
    }

    private boolean allowedValueClass(Class<?> valueClass) {
        return String.class.equals(valueClass)
                || Integer.class.equals(valueClass)
                || Boolean.class.equals(valueClass)
                || Long.class.equals(valueClass)
                || Double.class.equals(valueClass)
                || BigDecimal.class.equals(valueClass)
                || LocalDate.class.equals(valueClass)
                || LocalDateTime.class.equals(valueClass)
                || ZonedDateTime.class.equals(valueClass)
                || valueClass.isEnum();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
    }

    @Override
    public void visitField(Field field, String parentPath) {
        QueryParam queryParam = field.getDeclaredAnnotation(QueryParam.class);
        if (queryParam == null)
            throw Exceptions.error("field must have @QueryParam, field={}", Fields.path(field));

        String name = queryParam.name();

        boolean added = visitedQueryParams.add(queryParam.name());
        if (!added) {
            throw Exceptions.error("found duplicate query param, field={}, name={}", Fields.path(field), name);
        }

        Class<?> fieldClass = field.getType();
        if (fieldClass.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) fieldClass;
            JSONTypeValidator.validateEnumClass(enumClass);
        }
    }
}
