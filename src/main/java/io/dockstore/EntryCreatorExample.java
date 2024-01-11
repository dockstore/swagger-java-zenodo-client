package io.dockstore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import io.swagger.zenodo.client.ApiClient;
import io.swagger.zenodo.client.ApiException;
import io.swagger.zenodo.client.api.ActionsApi;
import io.swagger.zenodo.client.api.DepositsApi;
import io.swagger.zenodo.client.api.FilesApi;
import io.swagger.zenodo.client.model.Author;
import io.swagger.zenodo.client.model.Deposit;
import io.swagger.zenodo.client.model.DepositMetadata;
import io.swagger.zenodo.client.model.DepositionFile;
import io.swagger.zenodo.client.model.NestedDepositMetadata;
import io.swagger.zenodo.client.model.RelatedIdentifier;

public class EntryCreatorExample {

    public static void main(String[] args) throws ApiException, IOException {
        // for testing, your access token is the second parameter
        String token = args[1];
        ApiClient client = new ApiClient();
        // for testing, either 'https://sandbox.zenodo.org/api' or 'https://zenodo.org/api' is the first parameter
        client.setBasePath(args[0]);
        client.setApiKey(token);

        // this is working through the quick start to make sure we can do normal operations
        // http://developers.zenodo.org/#quickstart-upload

        DepositsApi depositApi = new DepositsApi(client);
        Deposit deposit = new Deposit();
        Deposit returnDeposit = depositApi.createDeposit(deposit);
        System.out.println(returnDeposit.toString());

        // upload a new file
        int depositionID = returnDeposit.getId();
        FilesApi filesApi = new FilesApi(client);
        Path tempFile = Files.createTempFile("test", "txt");
        Files.write(tempFile, Collections.singletonList("foobar content"), StandardCharsets.UTF_8);
        DepositionFile file = filesApi.createFile(depositionID, tempFile.toFile(), "test_file.txt");
        System.out.println(file);

        // add some metadata
        returnDeposit.getMetadata().setTitle("foobar title");
        returnDeposit.getMetadata().setUploadType(DepositMetadata.UploadTypeEnum.POSTER);
        returnDeposit.getMetadata().setDescription("This is a first upload");

        RelatedIdentifier relatedIdentifier = new RelatedIdentifier();
        relatedIdentifier.setIdentifier("https://dockstore.org/containers/quay.io%2Fpancancer%2Fpcawg-sanger-cgp-workflow");
        relatedIdentifier.setRelation(RelatedIdentifier.RelationEnum.ISIDENTICALTO);
        returnDeposit.getMetadata().setRelatedIdentifiers(Arrays.asList(relatedIdentifier));

        Author author1 = new Author();
        author1.setName("lastname1, firstname1");
        Author author2 = new Author();
        author2.setName("lastname2, firstname2");
        returnDeposit.getMetadata().setCreators(Arrays.asList(author1, author2));
        NestedDepositMetadata nestedDepositMetadata = new NestedDepositMetadata();
        nestedDepositMetadata.setMetadata(returnDeposit.getMetadata());
        depositApi.putDeposit(depositionID, nestedDepositMetadata);

        // publish it
        ActionsApi actionsApi = new ActionsApi(client);
        Deposit publishedDeposit = actionsApi.publishDeposit(depositionID);

        String conceptDoiUrl = publishedDeposit.getLinks().get("parent_doi");

        System.out.println(conceptDoiUrl);
        System.out.println(publishedDeposit);
    }
}
