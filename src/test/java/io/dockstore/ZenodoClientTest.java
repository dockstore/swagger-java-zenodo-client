package io.dockstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.zenodo.client.ApiClient;
import io.swagger.zenodo.client.ApiException;
import io.swagger.zenodo.client.api.DepositsApi;
import io.swagger.zenodo.client.api.PreviewApi;
import io.swagger.zenodo.client.model.Deposit;
import io.swagger.zenodo.client.model.Hit;
import io.swagger.zenodo.client.model.Hits;
import io.swagger.zenodo.client.model.Records;
import io.swagger.zenodo.client.model.SearchResult;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ZenodoClientTest {

    private ApiClient client;
    private PreviewApi previewApi;

    @BeforeEach
    void setup() {
        client = new ApiClient();
        client.setBasePath("https://sandbox.zenodo.org/api");
        previewApi = new io.swagger.zenodo.client.api.PreviewApi(client);
    }

    @Test
    @Disabled("Test disabled as the communities API is failing on sandbox")
    public void testZenodoClient() throws ApiException {
        HashMap<String, Object> o = (HashMap<String, Object>)previewApi.listCommunities();
        assertTrue(o != null && !o.keySet().isEmpty(), "not able to list communities as basic test");
    }

    @Test
    void testConceptDoi() {
        final SearchResult searchResult = previewApi.listRecords(null, "bestmatch", 1, 100);
        assertNotNull(searchResult.getHits().getHits().get(0).getConceptdoi());
        assertNotNull(searchResult.getHits().getHits().get(0).getCreated());
        assertNotNull(searchResult.getHits().getHits().get(0).getModified());
    }


    @Test
    @Disabled("worked with my personal token, need to dry run with dockstore-bot")
    public void testZenodoDraftQuery() throws io.swagger.zenodo.client.ApiException {
        ApiClient client = new ApiClient();
        String token = "<<your token here>>";
        client.setBasePath("https://zenodo.org/api");
        if (token != null) {
            // pretty much need a token to get records for a specific user
            client.setApiKey(token);
            PreviewApi previewApi = new PreviewApi(client);
            // noticed the is_published query fragment from the new api documentation, gives seemingly better results than is_draft
            Records records = previewApi.listUserRecords("is_published:false", "bestmatch", 10, 1, true, false);
            // index the various hit ids?
            Hits hits = records.getHits();

            Set<Integer> draftIds = hits.getHits().stream().map(Hit::getId).collect(Collectors.toSet());

            DepositsApi depositsApi = new DepositsApi(client);
            for (int depositId : draftIds) {
                Deposit deposit = depositsApi.getDeposit(depositId);
                assertEquals((int) deposit.getId(), depositId);
                // check something with state
                assertTrue("inprogress".equals(deposit.getState()) || "unsubmitted".equals(deposit.getState()));
                // then delete them using an undocumented endpoint DELETE https://zenodo.org/api/records/911480/draft
                try {
                    previewApi.deleteDraftRecord(depositId);
                } catch (ApiException e) {
                    System.out.println("had issues deleting deposit id: " + depositId);
                }
            }
        }
    }
}