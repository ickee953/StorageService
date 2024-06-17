/**
 * © Panov Vitaly 2023 - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

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
import java.util.Objects;

@Service
public class FilesStorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesStorageService.class);

    private final String UPLOAD_DIR = "/uploads";

    private final Path root = Paths.get(System.getProperty("user.dir") + UPLOAD_DIR).toAbsolutePath();

    public void init() {
        try {
            Files.createDirectory(root);

            String sb = root.toString() +
                    " created.\n";

            LOGGER.info(sb);
        } catch (IOException e) {
            if( e instanceof FileAlreadyExistsException ){
                String sb = "File: " +
                        root.toUri() +
                        " already exist.\n";

                LOGGER.info(sb);
            } else {
              String msg = "Could not initialize folder for upload!\n";
              LOGGER.error( msg );

              throw new RuntimeException( msg );
            }
        }
    }

    public String save(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(),
                    this.root.resolve(Objects.requireNonNull(file.getOriginalFilename()))
            );

            return file.getOriginalFilename();
        } catch (Exception e) {
            if( e instanceof FileAlreadyExistsException ){
                LOGGER.info("File with name {} already exist.\nReplacing...\n", file.getOriginalFilename());
                try {
                    Files.delete( this.root.resolve(Objects.requireNonNull(file.getOriginalFilename())) );
                } catch (IOException ex) {
                    LOGGER.error("Could not delete the file: {}.\n{}", file.getOriginalFilename(), ex.getMessage());

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
