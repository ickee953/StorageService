package com.online.files.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FilesStorageService {
    private static Logger LOGGER = LoggerFactory.getLogger(FilesStorageService.class);

    private final String UPLOAD_DIR = "/uploads";

    private final Path root = Paths.get(System.getProperty("user.dir") + UPLOAD_DIR).toAbsolutePath();

    public void init() {
        try {
            Files.createDirectory(root);

            StringBuilder sb = new StringBuilder();
            sb.append(root.toString());
            sb.append(" created.\n");

            LOGGER.info(sb.toString());
        } catch (IOException e) {
            if( e instanceof FileAlreadyExistsException ){
                StringBuilder sb = new StringBuilder();
                sb.append("File: ");
                sb.append(root.toUri());
                sb.append(" already exist.\n");

                LOGGER.info(sb.toString());
            } else {
              String msg = "Could not initialize folder for upload!\n";
              LOGGER.error( msg );

              throw new RuntimeException( msg );
            }
        }
    }

    public String save(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));

            return file.getOriginalFilename();
        } catch (Exception e) {
            if( e instanceof FileAlreadyExistsException ){
                LOGGER.info("File with name " + file.getOriginalFilename() + " already exist.\nReplacing...\n");
                try {
                    Files.delete( this.root.resolve(file.getOriginalFilename()) );
                } catch (IOException ex) {
                    LOGGER.error("Could not delete the file: " + file.getOriginalFilename() + ".\n" + ex.getMessage());
                    ex.printStackTrace();

                    throw new RuntimeException( ex.getMessage() );
                }
                return save( file );
            } else {
                String message = "Could not store the file. Error: " + e.getMessage();
                LOGGER.error( message );

                throw new RuntimeException( message );
            }
        }
    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
