package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Lists;
import core.framework.api.util.Types;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.impl.web.response.BeanBody;
import core.framework.impl.web.response.ResponseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class WebServiceControllerBuilderTest {
    @Mock
    private Request request;
    private TestWebServiceImpl testWebService;

    @BeforeEach
    void createTestWebService() {
        MockitoAnnotations.initMocks(this);

        testWebService = new TestWebServiceImpl();
    }

    @Test
    void get() throws Exception {
        when(request.pathParam("id", Integer.class)).thenReturn(1);

        WebServiceControllerBuilder<TestWebService> builder = new WebServiceControllerBuilder<>(TestWebService.class, testWebService, TestWebService.class.getDeclaredMethod("get", Integer.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("webservice-test/test-webservice-controller-get.java"), sourceCode);

        Response response = controller.execute(request);
        assertEquals(HTTPStatus.OK, response.status());
        assertEquals(2, (int) ((TestWebService.TestResponse) ((BeanBody) ((ResponseImpl) response).body).bean).intField);
    }

    @Test
    void create() throws Exception {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        when(request.pathParam("id", Integer.class)).thenReturn(1);
        when(request.bean(TestWebService.TestRequest.class)).thenReturn(requestBean);

        WebServiceControllerBuilder<TestWebService> builder = new WebServiceControllerBuilder<>(TestWebService.class, testWebService, TestWebService.class.getDeclaredMethod("create", Integer.class, TestWebService.TestRequest.class));
        Controller controller = builder.build();

        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("webservice-test/test-webservice-controller-create.java"), sourceCode);

        Response response = controller.execute(request);
        assertEquals(HTTPStatus.CREATED, response.status());
    }

    @Test
    void batch() throws Exception {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        when(request.bean(Types.list(TestWebService.TestRequest.class))).thenReturn(Lists.newArrayList(requestBean));

        Controller controller = new WebServiceControllerBuilder<>(TestWebService.class, testWebService, TestWebService.class.getDeclaredMethod("batch", List.class)).build();
        Response response = controller.execute(request);
        assertEquals(HTTPStatus.OK, response.status());
    }

    public static class TestWebServiceImpl implements TestWebService {
        @Override
        public TestResponse search(TestSearchRequest request) {
            return null;
        }

        @Override
        public TestResponse get(Integer id) {
            assertEquals(1, (int) id);

            TestResponse response = new TestResponse();
            response.intField = 2;
            return response;
        }

        @Override
        public void create(Integer id, TestRequest request) {
            assertEquals(1, (int) id);
            assertEquals("value", request.stringField);
        }

        @Override
        public void delete(String id) {

        }

        @Override
        public List<TestResponse> batch(List<TestRequest> requests) {
            assertEquals(1, requests.size());
            return Lists.newArrayList();
        }
    }
}
