package com.capg.RechargeHub.exception;

import com.capg.RechargeHub.dto.ErrorResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleRuntimeExceptions() {
        RuntimeException ex = new RuntimeException("Test runtime exception");
        ResponseEntity<ErrorResponseDTO> response = handler.handleRuntimeExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Test runtime exception", response.getBody().getMessage());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    void testHandleGenericExceptions() {
        Exception ex = new Exception("Test generic exception");
        ResponseEntity<ErrorResponseDTO> response = handler.handleGenericExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Test generic exception", response.getBody().getMessage());
        assertEquals("Error Occurred", response.getBody().getError());
    }

    @Test
    void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Failed", response.getBody().getError());
        Map<String, String> errors = response.getBody().getValidationErrors();
        assertTrue(errors.containsKey("field"));
        assertEquals("defaultMessage", errors.get("field"));
    }
}
