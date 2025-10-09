package com.positivo.podcast.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handler para as validações do @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(
            Instant.now(),
            status.value(),
            "Erro de Validação",
            "Dados inválidos na requisição.",
            request.getRequestURI()
        );

        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            err.addError(f.getField(), f.getDefaultMessage());
        }

        return ResponseEntity.status(status).body(err);
    }

    // Handler para nossa exceção de "não encontrado"
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(
            Instant.now(),
            status.value(),
            "Recurso não encontrado",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    // Handler para nossa exceção de "email já existe"
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<StandardError> handleEmailAlreadyExists(EmailAlreadyExistsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        StandardError err = new StandardError(
            Instant.now(),
            status.value(),
            "Conflito de Recurso",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<StandardError> handleFileUploadException(FileUploadException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(
            Instant.now(),
            status.value(),
            "Erro de Upload de Arquivo",
            e.getMessage(), // Mensagem vinda da exceção (ex: "Falha ao ler o arquivo...")
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    // Handler genérico para qualquer outra exceção não tratada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleGenericException(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(
            Instant.now(),
            status.value(),
            "Erro Interno do Servidor",
            "Ocorreu um erro inesperado. Tente novamente mais tarde.",
            request.getRequestURI()
        );
        // Opcional: Logar o erro real no console para debug
        e.printStackTrace();
        return ResponseEntity.status(status).body(err);
    }
}
