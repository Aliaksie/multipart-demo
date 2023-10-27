package com.example.demo;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
public class MultipartApplication {

   public static void main( String[] args ) {
      SpringApplication.run( MultipartApplication.class, args );
   }

   @Configuration
   public class MultipartConfig implements WebMvcConfigurer {

      @Bean
      public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
         return new Jackson2ObjectMapperBuilder().serializationInclusion( JsonInclude.Include.NON_NULL );
      }

   }

   @Controller
   public class UploadController {

      @PostMapping(
            value = "/upload",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
      )
      ResponseEntity<Void> uploadPackage( @RequestPart( value = "file", required = true ) MultipartFile file ) {
         throw new BadException( new RuntimeException() );
      }
   }

   public static class BadException extends RuntimeException {
      public BadException( final Throwable cause ) {
         super( cause );
      }
   }

   @JsonIgnoreProperties(
         ignoreUnknown = true
   )
   @JsonInclude( JsonInclude.Include.NON_EMPTY )
   public static class ErrorResponse {
      private final Error error;

      public ErrorResponse( final Error error ) {
         this.error = error;
      }

      @JsonCreator
      public ErrorResponse( @JsonProperty( "message" ) final String message, @JsonProperty( "path" ) final String path ) {
         this( new Error( message, path ) );
      }

      public Error getError() {
         return this.error;
      }

      public static class Error {
         private final String message;
         private final String path;
         private final Optional<String> code = Optional.empty();

         @JsonCreator
         public Error( @JsonProperty( "message" ) final String message, @JsonProperty( "path" ) final String path ) {
            this.message = message;
            this.path = path;
         }

         public Optional<String> getCode() {
            return this.code;
         }

         public String getMessage() {
            return this.message;
         }

         public String getPath() {
            return this.path;
         }

      }
   }

   @ControllerAdvice
   public class ResponseExceptionHandler {

      @ExceptionHandler( BadException.class )
      public ResponseEntity<ErrorResponse> handleAspectModelResolutionException( final HttpServletRequest request,
            final BadException e ) {
         return this.error( HttpStatus.BAD_REQUEST, request, e.getMessage() );
      }

      @ExceptionHandler( { MaxUploadSizeExceededException.class } )
      public ResponseEntity<ErrorResponse> handleMultipartMaxUploadSizeExceededException( final HttpServletRequest request,
            final MaxUploadSizeExceededException e ) {
         return this.tooLarge( request, e );
      }

      private ResponseEntity<ErrorResponse> tooLarge( final HttpServletRequest request, final MultipartException e ) {
         return this.error( HttpStatus.PAYLOAD_TOO_LARGE, request, e.getMessage() );
      }

      private ResponseEntity<ErrorResponse> error( final HttpStatusCode responseCode, final HttpServletRequest request, final String message ) {
         return new ResponseEntity( this.createErrorResponse( request, message ), responseCode );
      }

      protected ErrorResponse createErrorResponse( final HttpServletRequest request, final String message ) {
         return this.createErrorResponse( message, request.getRequestURI() );
      }

      protected ErrorResponse createErrorResponse( final String message, final String requestURI ) {
         return new ErrorResponse( message, requestURI );
      }
   }

}
