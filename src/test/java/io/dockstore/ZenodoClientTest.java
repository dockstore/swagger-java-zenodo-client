package io.dockstore;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.zenodo.client.ApiClient;
import io.swagger.zenodo.client.model.SearchResult;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ZenodoClientTest {

    private static ApiClient client;
    private static io.swagger.zenodo.client.api.PreviewApi previewApi;

    @BeforeAll
    static void setup() {
        client = new ApiClient();
        client.setBasePath("https://sandbox.zenodo.org/api");
        previewApi = new io.swagger.zenodo.client.api.PreviewApi(client);
    }

    @Test
    @Disabled("Test disabled as the communities API is failing on sandbox")
    public void testZenodoClient() throws io.swagger.zenodo.client.ApiException {
        HashMap<String, Object> o = (HashMap<String, Object>)previewApi.listCommunities();
        assertTrue(o != null && !o.keySet().isEmpty(), "not able to list communities as basic test");
    }

    @Test
    void testConceptDoi() {
        final SearchResult searchResult = previewApi.listRecords(null, "bestmatch", 1, 100);
        assertNotNull(searchResult.getHits().getHits().get(0).getConceptdoi());
    }
}