package com.taxreco.recon.dataloader.rest

import com.taxreco.recon.dataloader.model.ApiError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ControllerAdviceRequestError : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [(SecurityException::class)])
    fun securityException(ex: SecurityException, request: WebRequest): ResponseEntity<*> {
        return ResponseEntity(ApiError("SEC0001", "Invalid Auth Credentials!"), HttpStatus.UNAUTHORIZED)
    }
}