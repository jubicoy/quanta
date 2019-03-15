package fi.jubic.quanta.resources;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.models.view.FileUploadResponse;
import fi.jubic.quanta.util.Files;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Singleton
@Path("files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileResource {
    private final String filePath;

    @Inject
    public FileResource(Configuration configuration) {
        this.filePath = configuration.getFilePath();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SuppressFBWarnings(
            value = "PATH_TRAVERSAL_IN",
            justification = "fileName is not customizable AND also verified. "
                + "filePath is from server configuration."
    )
    public FileUploadResponse upload(@Context HttpServletRequest request) {
        String fileName = String.format("%s.csv", UUID.randomUUID().toString());

        Part file;
        try {
            file = request.getPart("file");
        }
        catch (IOException | ServletException e) {
            throw new BadRequestException("Can not read file");
        }

        if (file == null) {
            throw new BadRequestException("Can not read file");
        }

        java.nio.file.Path path = Paths
                .get(
                        filePath,
                        FilenameUtils.getName(fileName)
                )
                .toAbsolutePath();

        return Files.save(path, file)
                .map(ignored -> FileUploadResponse.builder()
                        .setFileName(fileName)
                        .build()
                )
                .orElseThrow(InternalServerErrorException::new);
    }
}
