package com.example.rest.exception;

/**
 * Error information returned by the REST API.
 */
public class ErrorInfo {

    private String jobId;
    private String message;

    public ErrorInfo() {
    }

    public ErrorInfo(String jobId, String message) {
        this.jobId = jobId;
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
