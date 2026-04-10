package com.example.certalert.certificate.parser;

public class CertificateParseException extends RuntimeException {
    public CertificateParseException(String message) { super(message); }
    public CertificateParseException(String message, Throwable cause) { super(message, cause); }
}
