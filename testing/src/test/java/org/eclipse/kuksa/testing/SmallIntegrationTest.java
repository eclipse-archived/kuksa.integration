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

import org.apache.logging.log4j.Level;
import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.config.*;
import org.eclipse.kuksa.testing.model.Credentials;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import feign.Response;

@EnableAutoConfiguration
@ContextConfiguration(classes = {GlobalConfiguration.class, HawkBitConfiguration.class, AppStoreConfiguration.class, HawkbitMultiPartFileFeignClient.class})
public class SmallIntegrationTest extends AbstractTestCase {


    private static String securityToken;

    private static Credentials hawkbitCredentials;

    private static String hawkbitAddress;

    private static Credentials appstoreCredentials;

    private static String appstoreAddress;

    private static String controllerId;

    private static final String JSON_PROPERTY_ID = "id";

    private static String appName = "IntApp" + new Random().nextInt(1000);

    private static String deviceId = "OEM_Integration3";

    private static Long userId;

    private static JSONObject user;

    private static String userName;

    private static Long softwareModuleId;

    private static Long categoryId;

    private static JSONObject category;

    private static Long appId;

    private static JSONObject app;


    private static final String JSON_PROPERTY_USER_USERNAME = "username";
    private static final String JSON_PROPERTY_USER_USERNAME_VALUE = "testuser3";

    private static final String JSON_PROPERTY_USER_PASSWORD = "password";
    private static final String JSON_PROPERTY_USER_PASSWORD_VALUE = "testpassword";

    private static final String JSON_PROPERTY_USER_USERTYPE = "userType";
    private static final String JSON_PROPERTY_USER_USERTYPE_VALUE = "Normal";

    private static final String JSON_PROPERTY_USER_ADMINUSER = "adminuser";
    private static final boolean JSON_PROPERTY_USER_ADMINUSER_VALUE = false;

    // Hawkbit: /app
    private static final String JSON_PROPERTY_APP_HAWKBIT_NAME = "hawkbitname";
    private static final String JSON_PROPERTY_APP_HAWKBIT_NAME_VALUE = appName;

    // AppStore: /category
    private static final String JSON_PROPERTY_CATEGORY_NAME = "name";
    private static final String JSON_PROPERTY_CATEGORY_NAME_VALUE = "TestCategory2";

    // AppStore: /app
    private static final String JSON_PROPERTY_APP_NAME = "name";
    private static final String JSON_PROPERTY_APP_NAME_VALUE = appName;

    private static final String JSON_PROPERTY_APP_VERSION = "version";
    private static final String JSON_PROPERTY_APP_VERSION_VALUE = "1.0";

    private static final String JSON_PROPERTY_APP_DESCRIPTION = "description";
    private static final String JSON_PROPERTY_APP_DESCRIPTION_VALUE = JSON_PROPERTY_USER_USERNAME_VALUE;

    private static final String JSON_PROPERTY_APP_OWNER = "owner";
    private static final String JSON_PROPERTY_APP_OWNER_VALUE = "OEM_EXPLEO";

    private static final String JSON_PROPERTY_APP_DOWNLOADCOUNT = "downloadcount";
    private static final int JSON_PROPERTY_DOWNLOADCOUNT_VALUE = 0;

    private static final String JSON_PROPERTY_APP_PUBLISH_DATE = "publishdate";
    private static final Long JSON_PROPERTY_APP_PUBLISH_DATE_VALUE = new Date().getTime(); // today

    private static final String JSON_PROPERTY_APP_CATEGORY_NAME = "appcategory";
    private static JSONObject JSON_PROPERTY_APP_CATEGORY_NAME_VALUE = new JSONObject();

    @Autowired
    private GlobalConfiguration globalConfig;

    @Autowired
    private HawkBitConfiguration hawkbitConfig;

    @Autowired
    private AppStoreConfiguration appstoreConfig;

    @Autowired
    private HawkbitMultiPartFileFeignClient hawkbitMultiPartFileFeignClient;


    @Test
    @Order(1)
    public void integrationSetup() throws Exception {
        System.out.println("SETUP START");

        hawkbitAddress = hawkbitConfig.getAddress();
        hawkbitCredentials = new Credentials(hawkbitConfig.getUsername(), hawkbitConfig.getPassword());

        appstoreAddress = appstoreConfig.getAddress();
        appstoreCredentials = new Credentials(appstoreConfig.getUsername(), appstoreConfig.getPassword());

        System.out.println("CREATING USER");

        user = new JSONObject(createUser());
        System.out.println(user);
        userId = user.getLong(JSON_PROPERTY_ID);
        userName = user.getString(JSON_PROPERTY_USER_USERNAME);

        System.out.println("CREATING CATEGORY");

        category = new JSONObject(createCategory());
        categoryId = category.getLong(JSON_PROPERTY_ID);

        System.out.println("CREATING APP");

        app = new JSONObject(createApp(categoryId));
        appId = app.getLong(JSON_PROPERTY_ID) ;

        System.out.println("userId " + userId + " categoryId " + categoryId + " appId " + appId);
        addAppToUser();

        System.out.println("SETTING UP HAWKBIT");

        if(!testCheckForTarget()) {

            String responseBody = createTarget();
            JSONArray jsonArray = new JSONArray(responseBody);
            String element = jsonArray.getString(0);
            JSONObject body = new JSONObject(element);

            controllerId = body.getString("controllerId");
            securityToken = body.getString("securityToken");

            System.out.println(body);
        }

        updateTarget();
        getSoftwareModule();
    }

    @AfterClass
    public static void integrationCleanup() {
        System.out.println("CLEANUP START");

        if (appId != null) {
            removeApp(appId);
            removeUser(userId);
            removeCategory(categoryId);
            removeTargetForCleanup(controllerId);
        }
    }

    @Override
    protected String getTestFile() {
        return "SmallIntegration-TestSuite.yaml";
    }

    public boolean testCheckForTarget() throws JSONException {
        HttpHeaders headers = new HttpHeaders(getBaseRequestHeaders());
        headers.setBasicAuth("admin", "admin");


        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(),  "/rest/v1/targets"))
                .get()
                .headers(headers)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println(responseEntity.getBody());

        JSONArray array = new JSONObject(responseEntity.getBody()).getJSONArray("content");
        for (int i = 0; i < array.length(); i++) {
            JSONObject row = array.getJSONObject(i);
            System.out.println(row + "\n\n");
            if(row.getString("controllerId").equals(deviceId)) {

                controllerId = row.getString("controllerId");
                securityToken = row.getString("securityToken");

                return true;
            }
        }
        return false;

    }

    public void testGetTargetInfo() {
        // GIVEN

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(),  "/rest/controller/v1/" + controllerId))
                .get()
                .headers(getBaseRequestHeaders())
                .addHeader("Authorization", "TargetToken " + securityToken)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        String body = responseEntity.getBody();
        assertNotNull(body);
    }

    public void getSoftwareModule() throws Exception {
        // GIVEN
        System.out.println("GETTING OUR NEW SOFTWAREMODULE");

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(),  "/rest/v1/softwaremodules"))
                .get()
                .credentials(hawkbitCredentials)
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        JSONArray array = new JSONObject(responseEntity.getBody()).getJSONArray("content");
        for (int i = 0; i < array.length(); i++) {
            JSONObject row = array.getJSONObject(i);
            System.out.println(row + "\n\n");
            if(row.getString("name").equals(JSON_PROPERTY_APP_NAME_VALUE)) {
                softwareModuleId = row.getLong("id");
            }
        }
        uploadArtifactToHawkbit();
    }

    public void uploadArtifactToHawkbit() {
        ClassLoader classLoader = new HonoConfiguration().getClass().getClassLoader();
        File file = new File(classLoader.getResource("DOCKER_dashboard_arm.tar").getFile());

        MultipartFile imageFile = new MultipartFile() {

            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                return MediaType.MULTIPART_FORM_DATA_VALUE;
            }

            @Override
            public boolean isEmpty() {
                return readContentIntoByteArray(file).length == 0;
            }

            @Override
            public long getSize() {
                return readContentIntoByteArray(file).length;
            }

            @Override
            public byte[] getBytes() {
                return readContentIntoByteArray(file);
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(readContentIntoByteArray(file));
            }

            @Override
            public void transferTo(File dest) throws IllegalStateException {

            }
        };

        Response response = hawkbitMultiPartFileFeignClient.uploadFile(softwareModuleId.toString(), imageFile);

        System.out.println(response.body());

        testPurchaseApp();
    }

    private static String createTarget() throws JSONException {
        // build request
        String controllerId = deviceId;
        String name = deviceId;
        String description = userName;

        JSONArray requestBody = new JSONArray()
                .put(
                        new JSONObject()
                                .put("controllerId", controllerId)
                                .put("name", name)
                                .put("description", description)
                );

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitAddress, "/rest/v1/targets"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(requestBody)
                .credentials(hawkbitCredentials)
                .build();

        // execute request
        ResponseEntity<String> responseEntity = executeApiCall(request);

        if (responseEntity.getStatusCodeValue() != HttpStatus.CREATED.value()) {
            fail("Failed to create target.");
        }

        return responseEntity.getBody();
    }

    public void updateTarget() throws JSONException {

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitAddress, "rest/v1/targets/" + controllerId))
                .put()
                .credentials(hawkbitCredentials)
                .body(new JSONObject()
                        .put("securityToken", "78d06ca7aba7405ffd0dd82d24408fe6")
                        .put( "requestAttributes", true)
                        .put("controllerId", controllerId)
                        .put("name", controllerId)
                        .put("description", userName))
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println(responseEntity.getBody());
    }

    public void testPurchaseApp() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId + "/purchase/" + userId))
                .headers(headers)
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        System.out.println(response.getBody());
    }

    @Test
    @Order(3)
    public void installationTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        System.out.println("Installing app to " + controllerId);

        Request request = new Request.Builder()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId + "/install/" + userId))
                .post()
                .body(controllerId)
                .headers(headers)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println(responseEntity.getBody());

        Thread.sleep(20000);
    }



    private static Request.Builder getBaseRequestBuilder() {
        return new Request.Builder()
                .headers(getBaseRequestHeaders())
                .credentials(hawkbitCredentials);
    }


    private static String createUser() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/user/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_USER_ADMINUSER, JSON_PROPERTY_USER_ADMINUSER_VALUE)
                        .put(JSON_PROPERTY_USER_USERNAME, JSON_PROPERTY_USER_USERNAME_VALUE)
                        .put(JSON_PROPERTY_USER_PASSWORD, JSON_PROPERTY_USER_PASSWORD_VALUE)
                        .put(JSON_PROPERTY_USER_USERTYPE, JSON_PROPERTY_USER_USERTYPE_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        LOGGER.info("JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store user."));
        }

        return response.getBody();
    }

    private static String createCategory() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/appcategory/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_CATEGORY_NAME, JSON_PROPERTY_CATEGORY_NAME_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        LOGGER.log(Level.INFO, "JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store category."));
        }

        return response.getBody();
    }

    private static String createApp(Long categoryId) throws JSONException {
        try {
            JSON_PROPERTY_APP_CATEGORY_NAME_VALUE = new JSONObject().put("id", categoryId ).put("name", category.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(JSON_PROPERTY_APP_CATEGORY_NAME_VALUE);

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_APP_NAME, JSON_PROPERTY_APP_NAME_VALUE)
                        .put(JSON_PROPERTY_APP_VERSION, JSON_PROPERTY_APP_VERSION_VALUE)
                        .put(JSON_PROPERTY_APP_DESCRIPTION, JSON_PROPERTY_APP_DESCRIPTION_VALUE)
                        .put(JSON_PROPERTY_APP_HAWKBIT_NAME, JSON_PROPERTY_APP_HAWKBIT_NAME_VALUE)
                        .put(JSON_PROPERTY_APP_OWNER, JSON_PROPERTY_APP_OWNER_VALUE )
                        .put(JSON_PROPERTY_APP_CATEGORY_NAME, JSON_PROPERTY_APP_CATEGORY_NAME_VALUE )
                        .put(JSON_PROPERTY_APP_DOWNLOADCOUNT, JSON_PROPERTY_DOWNLOADCOUNT_VALUE )
                        .put(JSON_PROPERTY_APP_PUBLISH_DATE, JSON_PROPERTY_APP_PUBLISH_DATE_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        System.out.println("JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store app."));
        }
        return response.getBody();
    }

    private static void addAppToUser() throws Exception {

        Request requestApp = getBaseRequestBuilder()
                .put()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId))
                .body(new JSONObject(app.toString())
                        .put("installedusers", new JSONArray().put(user))
                )
                .build();

        ResponseEntity<String> response = executeApiCall(requestApp);

        if (!response.getStatusCode().is2xxSuccessful()) {
            fail("Failed to update app.");
        }

    }

    private static void removeCategory(Long categoryId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/appcategory/" + categoryId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store category."));
        }
    }

    private static void removeApp(Long appId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store app."));
        }
    }

    private static void removeUser(Long userId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/user/" + userId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store user."));
        }
    }

    private static void removeTargetForCleanup(String controllerId) {
        // build request
        Request request = new Request.Builder()
                .url(buildUrl(hawkbitAddress, "/rest/v1/targets/" + controllerId))
                .delete()
                .headers(getBaseRequestHeaders())
                .credentials(hawkbitCredentials)
                .build();

        // execute request
        ResponseEntity<String> responseEntity = executeApiCall(request);

        if (responseEntity.getStatusCodeValue() != HttpStatus.OK.value()) {
            fail("Failed to remove target.");
        }
    }


    private static byte[] readContentIntoByteArray(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try
        {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            for (int i = 0; i < bFile.length; i++)
            {
                System.out.print((char) bFile[i]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bFile;
    }

}


