package io.dockstore;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;



public class ZenodoClientTest {

    @org.junit.jupiter.api.Test
    public void testZenodoClient() throws io.swagger.zenodo.client.ApiException {
        io.swagger.zenodo.client.ApiClient client = new io.swagger.zenodo.client.ApiClient();
        client.setBasePath("https://sandbox.zenodo.org/api");
        io.swagger.zenodo.client.api.PreviewApi previewApi = new io.swagger.zenodo.client.api.PreviewApi(client);
        HashMap<String, Object> o = (HashMap<String, Object>)previewApi.listCommunities();
        assertTrue(o != null && !o.keySet().isEmpty(), "not able to list communities as basic test");
    }
}