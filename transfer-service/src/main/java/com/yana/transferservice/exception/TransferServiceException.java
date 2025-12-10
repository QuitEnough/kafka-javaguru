package com.yana.transferservice.exception;

public class TransferServiceException extends RuntimeException {
    public TransferServiceException(String message) {
        super(message);
    }

  public TransferServiceException(Throwable cause) {
    super(cause);
  }
}
