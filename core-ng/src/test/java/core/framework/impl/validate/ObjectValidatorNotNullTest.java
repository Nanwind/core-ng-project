package core.framework.impl.validate;

import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ObjectValidatorNotNullTest {
    private ObjectValidatorBuilder builder;
    private ObjectValidator validator;

    @BeforeEach
    void createObjectValidator() {
        builder = new ObjectValidatorBuilder(Bean.class, Field::getName);
        validator = builder.build().get();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("validator-test/validator-notnull.java"), sourceCode);
    }

    @Test
    void validate() {
        Bean bean = new Bean();
        bean.child = new ChildBean();
        bean.children = Lists.newArrayList(bean.child);
        bean.childMap = Maps.newHashMap("child1", bean.child);

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(5, errors.errors.size());
        assertThat(errors.errors.get("stringField"), containsString("stringField"));
        assertThat(errors.errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.errors.get("child.intField"), containsString("intField"));
        assertThat(errors.errors.get("children.intField"), containsString("intField"));
        assertThat(errors.errors.get("childMap.intField"), containsString("intField"));
    }

    @Test
    void partialValidate() {
        Bean bean = new Bean();

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertFalse(errors.hasError());
    }

    static class Bean {
        @NotNull(message = "stringField must not be null")
        public String stringField;
        public String nullStringField;
        @NotNull(message = "booleanField must not be null")
        public Boolean booleanField;
        @NotNull
        public ChildBean child;
        public List<ChildBean> children;
        @NotNull
        public Map<String, ChildBean> childMap;
    }

    static class ChildBean {
        @NotNull(message = "intField must not be null")
        public Integer intField;
    }
}
