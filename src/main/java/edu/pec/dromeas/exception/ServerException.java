package edu.pec.dromeas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ServerException extends RuntimeException
{
    private static final long serialVersionUID = -6450482266114441055L;

    public ServerException(String message)
    {
        super(message);
    }

    public ServerException() {
        super();
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}