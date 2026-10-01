package com.winter.ecommerce.controller;

import com.winter.ecommerce.dto.ErrorResponse;
import com.winter.ecommerce.service.OrderNotCancellableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converts expected application exceptions into consistent client-facing HTTP errors.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("Request validation failed.");
		return ResponseEntity.badRequest().body(new ErrorResponse(message));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		String message = exception.getConstraintViolations().stream()
				.map(ConstraintViolation::getMessage)
				.findFirst()
				.orElse("Request validation failed.");
		return ResponseEntity.badRequest().body(new ErrorResponse(message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		return ResponseEntity.badRequest().body(new ErrorResponse("Request body is missing or malformed."));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
		return ResponseEntity.badRequest()
				.body(new ErrorResponse("Invalid value for parameter: " + exception.getName() + "."));
	}

	@ExceptionHandler(OrderNotCancellableException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotCancellable(OrderNotCancellableException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
	}
}
