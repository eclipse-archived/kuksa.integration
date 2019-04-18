/*********************************************************************
 * Copyright (c)  2019 Expleo Germany GmbH [and others].
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Expleo Germany GmbH
 **********************************************************************/

package org.eclipse.kuksa.testing;

import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.client.TestApiRunner;
import org.eclipse.kuksa.testing.model.TestCase;
import org.eclipse.kuksa.testing.model.TestSuite;
import org.eclipse.kuksa.testing.model.YamlConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
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
    protected static final String PROTOCOL_TCP = "tcp://";

    private static final String TEST_RESULT_PATH = "src/test/resources/result/";

    public static final Logger LOGGER = LogManager.getLogger();

    private static TestApiRunner runner = new TestApiRunner();

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
    public final void initialize() throws Exception {
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
        System.out.println("cleaning UP");
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
    protected static String buildUrl(String baseUrl, String path) {
        return new StringBuilder().append(baseUrl).append(path).toString();
    }

    protected static String staticBuildUrl(String baseUrl, String path) {
        return new StringBuilder().append(baseUrl).append(path).toString();
    }

    protected static HttpHeaders getBaseRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentLanguage(Locale.US);

        return headers;
    }

    protected static HttpHeaders staticGetBaseRequestHeaders() {
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

    protected MqttMessage setMqttMessage(String value) {
        byte[] payload = value.getBytes();
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(0);
        msg.setRetained(false);
        return msg;
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

    protected static ResponseEntity<String> executeApiCall(Request request) {
        return runner.executeApiCall(request);
    }

    protected static ResponseEntity<String> staticExecuteApiCall(Request request) {
        return runner.executeApiCall(request);
    }

}
