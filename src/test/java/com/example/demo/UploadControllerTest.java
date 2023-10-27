package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@AutoConfigureWebTestClient( timeout = "PT120S" )
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
class UploadControllerTest {

   @Autowired
   WebTestClient webTestClient;

   @ParameterizedTest
   @ValueSource( strings = { "zip/zip_2mb.zip", "zip/zip_6mb.zip" } )
   void uploadTest413( String path ) {

      final Resource resource = new ClassPathResource( path );
      MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
      multipartBodyBuilder.part( "file", resource );

      final MultipartApplication.ErrorResponse errorResponse = webTestClient.post()
            .uri( "/upload" )
            .contentType( MediaType.MULTIPART_FORM_DATA )
            .body( BodyInserters.fromMultipartData( multipartBodyBuilder.build() ) )
            .exchange()
            .expectStatus()
            .isEqualTo( HttpStatus.PAYLOAD_TOO_LARGE )
            .expectBody( MultipartApplication.ErrorResponse.class )
            .returnResult()
            .getResponseBody();

      System.out.println( errorResponse );
   }

   @Test
   void uploadTest404() {
      final Resource resource = new ClassPathResource( "zip/zip_5kb.zip" );
      MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
      multipartBodyBuilder.part( "file", resource );

      final MultipartApplication.ErrorResponse errorResponse = webTestClient.post()
            .uri( "/upload" )
            .contentType( MediaType.MULTIPART_FORM_DATA )
            .body( BodyInserters.fromMultipartData( multipartBodyBuilder.build() ) )
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody( MultipartApplication.ErrorResponse.class )
            .returnResult()
            .getResponseBody();

      System.out.println( errorResponse );
   }
}
