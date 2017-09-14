package core.framework.api.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class HTMLTemplateEngineTest {
    private HTMLTemplateEngine engine;

    @BeforeEach
    void createHTMLTemplateEngine() {
        engine = new HTMLTemplateEngine();
    }

    @Test
    void process() {
        engine.add("test", "<html><img c:src=\"imageURL\"></html>", TestModel.class);
        TestModel model = new TestModel();
        model.imageURL = "http://domain/image.png";
        String html = engine.process("test", model);
        assertEquals("<html><img src=http://domain/image.png></html>", html);
    }

    public static class TestModel {
        public String imageURL;
    }
}
