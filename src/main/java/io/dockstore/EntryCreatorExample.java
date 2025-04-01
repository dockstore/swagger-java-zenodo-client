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
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.util.List;

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
        returnDeposit.getMetadata().setPublicationDate("2025-03-03");
        returnDeposit.getMetadata().setTitle("foobar title");
        returnDeposit.getMetadata().setUploadType(DepositMetadata.UploadTypeEnum.POSTER);
        // just above https://developers.zenodo.org/#list
        // there's a note that says " For string fields that allow HTML (e.g. description, notes), for security reasons, only the following tags are accepted: a, abbr, acronym, b, blockquote, br, code, caption, div, em, i, li, ol, p, pre, span, strike, strong, sub, table, caption, tbody, thead, th, td, tr, u, ul. "
        String descriptionHTML = """
                <b>This text is bold</b>
                <strike>This text is scratched out</strike>
                <p>
                <strike>This text is also scratched out, but in a new paragraph</strike>            
            """;

        String descriptionMarkdown = """
            commonmark-java
            ===============
                        
            Java library for parsing and rendering [Markdown] text according to the
            [CommonMark] specification (and some extensions).
                        
            [![Maven Central status](https://img.shields.io/maven-central/v/org.commonmark/commonmark.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.commonmark%22)
            [![javadoc](https://www.javadoc.io/badge/org.commonmark/commonmark.svg?color=blue)](https://www.javadoc.io/doc/org.commonmark/commonmark)
            [![ci](https://github.com/commonmark/commonmark-java/workflows/ci/badge.svg)](https://github.com/commonmark/commonmark-java/actions?query=workflow%3Aci)
            [![codecov](https://codecov.io/gh/commonmark/commonmark-java/branch/main/graph/badge.svg)](https://codecov.io/gh/commonmark/commonmark-java)
            [![SourceSpy Dashboard](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/commonmarkcommonmarkjava/)
                        
            Introduction
            ------------
                        
            Provides classes for parsing input to an abstract syntax tree (AST),
            visiting and manipulating nodes, and rendering to HTML or back to Markdown.
            It started out as a port of [commonmark.js], but has since evolved into an
            extensible library with the following features:
                        
            * Small (core has no dependencies, extensions in separate artifacts)
            * Fast (10-20 times faster than [pegdown] which used to be a popular Markdown
              library, see benchmarks in repo)
            * Flexible (manipulate the AST after parsing, customize HTML rendering)
            * Extensible (tables, strikethrough, autolinking and more, see below)
                        
            The library is supported on Java 11 and later. It works on Android too,
            but that is on a best-effort basis, please report problems. For Android the
            minimum API level is 19, see the
            [commonmark-android-test](commonmark-android-test)
            directory.
                        
            Coordinates for core library (see all on [Maven Central]):
                        
            ```xml
            <dependency>
                <groupId>org.commonmark</groupId>
                <artifactId>commonmark</artifactId>
                <version>0.24.0</version>
            </dependency>
            ```
                        
            The module names to use in Java 9 are `org.commonmark`,
            `org.commonmark.ext.autolink`, etc, corresponding to package names.
                        
            Note that for 0.x releases of this library, the API is not considered stable
            yet and may break between minor releases. After 1.0, [Semantic Versioning] will
            be followed. A package containing `beta` means it's not subject to stable API
            guarantees yet; but for normal usage it should not be necessary to use.
                        
            See the [spec.txt](commonmark-test-util/src/main/resources/spec.txt)
            file if you're wondering which version of the spec is currently
            implemented. Also check out the [CommonMark dingus] for getting familiar
            with the syntax or trying out edge cases. If you clone the repository,
            you can also use the `DingusApp` class to try out things interactively.
                        
                        
            Usage
            -----
                        
            #### Parse and render to HTML
                        
            ```java
            import org.commonmark.node.*;
            import org.commonmark.parser.Parser;
            import org.commonmark.renderer.html.HtmlRenderer;
                        
            Parser parser = Parser.builder().build();
            Node document = parser.parse("This is *Markdown*");
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            renderer.render(document);  // "<p>This is <em>Markdown</em></p>\\n"
            """;
        Parser parser = Parser.builder().build();
        Node document = parser.parse(descriptionMarkdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        descriptionHTML = renderer.render(document);  // "<p>This is <em>Markdown</em></p>\n"

        returnDeposit.getMetadata().setDescription(descriptionHTML);

        RelatedIdentifier relatedIdentifier = new RelatedIdentifier();
        relatedIdentifier.setIdentifier("https://dockstore.org/containers/quay.io%2Fpancancer%2Fpcawg-sanger-cgp-workflow");
        relatedIdentifier.setRelation(RelatedIdentifier.RelationEnum.ISIDENTICALTO);
        returnDeposit.getMetadata().setRelatedIdentifiers(List.of(relatedIdentifier));

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

        System.out.println("Success creating concept DOI");
        System.out.println(conceptDoiUrl);
        System.out.println("Success creating first deposit");
        System.out.println(publishedDeposit);

        // test out second deposit
        testSecondDeposit(depositApi, actionsApi, publishedDeposit.getId());
    }

    public static void testSecondDeposit(DepositsApi depositApi, ActionsApi actionsApi, int depositID) {
        Deposit returnDeposit = actionsApi.newDepositVersion(depositID);
        String depositURL = returnDeposit.getLinks().get("latest_draft");
        String depositionIDStr = depositURL.substring(depositURL.lastIndexOf("/") + 1).trim();
        int depositionID = Integer.parseInt(depositionIDStr);
        returnDeposit = depositApi.getDeposit(depositionID);
        DepositMetadata depositMetadata = returnDeposit.getMetadata();
        String doi = depositMetadata.getPrereserveDoi().getDoi();
        System.out.println("Success creating second deposit");
        System.out.println(doi);
    }
}
