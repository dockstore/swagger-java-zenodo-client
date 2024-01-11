![example workflow](https://github.com/dockstore/swagger-java-zenodo-client/actions/workflows/mvn.yml/badge.svg)

# zenodo-client
A generated client for zenodo using a swagger (soon be openAPI) description

This is a work in progress and mostly describes the deposit, files, and actions API
Note that Zenodo does not provide an openAPI or Swagger yaml. You can find a list of the Zenodo REST APIs at this endpoint: https://zenodo.org/api/ and you can find the REST API documentation here: http://developers.zenodo.org/

The OpenAPI description was created by our team and can be updated at  [src/main/resources/zenodo-1.0.0-swagger-2.0.yaml](src/main/resources/zenodo-1.0.0-swagger-2.0.yaml)

There is also a test application at [src/main/java/io/dockstore/EntryCreatorExample.java](src/main/java/io/dockstore/EntryCreatorExample.java) that can be used to test some basic zenodo operations like depositing and publishing given a valid zenodo token. 
