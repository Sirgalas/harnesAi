package ru.sergalas.harnes.exception;

import java.nio.file.Path;

public class WorkspaceViolationException extends RuntimeException {
    public WorkspaceViolationException(String message) {
        super(message);
    }
}
