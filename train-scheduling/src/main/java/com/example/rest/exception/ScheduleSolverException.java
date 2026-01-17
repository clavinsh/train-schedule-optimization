package com.example.rest.exception;

/**
 * Exception thrown when there's an error with the solver.
 */
public class ScheduleSolverException extends RuntimeException {

    private final String jobId;

    public ScheduleSolverException(String jobId, String message) {
        super(message);
        this.jobId = jobId;
    }

    public ScheduleSolverException(String jobId, String message, Throwable cause) {
        super(message, cause);
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}
