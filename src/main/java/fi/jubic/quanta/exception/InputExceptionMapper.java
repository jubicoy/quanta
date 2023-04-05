package fi.jubic.quanta.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;


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
