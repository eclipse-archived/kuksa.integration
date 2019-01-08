package org.eclipse.kuksa.testing;

import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.client.TestApiClient;
import org.eclipse.kuksa.testing.model.TestCase;
import org.eclipse.kuksa.testing.model.TestSuite;
import org.eclipse.kuksa.testing.model.YamlConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Locale;

import static org.junit.Assert.fail;

/**
 * Parent class for all test cases which executes API-Calls. The
 * application.properties file will be used for test configuration. If necessary
 * these configuration can be overridden by an extra local.properties file in
 * the same directory.
 *
 * @author cnguyen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource({"classpath:application.properties", "classpath:local.properties"})
public abstract class AbstractTestCase {

    protected static final String PROTOCOL_HTTP = "http://";

    private static final String TEST_RESULT_PATH = "src/test/resources/result/";

    public static final Logger LOGGER = LogManager.getLogger();

    private TestApiClient client = new TestApiClient();

    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher testWatcher = new GlobalTestWatcher();

    private TestSuite testSuite;

    // current executing test
    protected TestCase testCase;

    // TEST ENVIRONMENT
    /*
     * PLEASE DO NOT OVERRIDE initialize() AND finish() IN SUB-CLASS -> only for
     * logging
     */
    @Before
    public final void initialze() throws Exception {
        LOGGER.debug("SETUP START");
        if (testSuite == null) {
            String file = TEST_RESULT_PATH + getTestFile();

            testSuite = YamlConverter.convert(file);
            if (testSuite == null) {
                fail();
            }
        }

        String currentTest = testName.getMethodName();
        testCase = testSuite.getTestCases().get(currentTest);

        // individual test setup
        testSetup();

        LOGGER.debug("SETUP END");
    }

    @After
    public final void cleanup() throws Exception {
        LOGGER.debug("CLEANUP START");

        // individual test setup
        testCleanup();

        LOGGER.debug("CELANUP END");
    }

    protected void testSetup() throws Exception {
        // override if necessary
    }

    protected void testCleanup() throws Exception {
        // override if necessary
    }

    /**
     * Returns the location to a file which contains test results.
     *
     * @return file location
     */
    protected abstract String getTestFile();

    // UTIL
    protected String buildUrl(String protocol, String baseUrl, String path) {
        return new StringBuilder().append(protocol).append(baseUrl).append(path).toString();
    }

    protected HttpHeaders getBaseRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentLanguage(Locale.US);

        return headers;
    }

    protected JSONObject getBodyAsJson(ResponseEntity<String> response) {
        return getBodyAsJson(response.getBody());
    }

    private JSONObject getBodyAsJson(String body) {
        try {
            return new JSONObject(body);
        } catch (JSONException e) {
            LOGGER.error("Failed to transform body to json format.", e);
            fail();
        }
        return null;
    }

    protected String getJsonValue(JSONObject json, String property) {
        try {
            return json.getString(property);
        } catch (Exception e) {
            LOGGER.error("Property " + property + "does not exist.", e);
            fail();
        }
        return null;
    }

    protected JSONObject getJsonObject(JSONObject json, String property) {
        try {
            return json.getJSONObject(property);
        } catch (Exception e) {
            LOGGER.error("Property " + property + "does not exist.", e);
            fail();
        }
        return null;
    }

    protected ResponseEntity<String> executeApiCall(Request request) {
        return client.executeApiCall(request);
    }

}
