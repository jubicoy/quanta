package fi.jubic.quanta.exception;

import org.apache.http.HttpStatus;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InputExceptionMapper implements ExceptionMapper<InputException> {

    public InputExceptionMapper() {

    }

    @Override
    public Response toResponse(InputException throwable) {
        return Response
                .status(HttpStatus.SC_BAD_REQUEST)
                .entity(throwable.getMessage())
                .build();
    }
}
