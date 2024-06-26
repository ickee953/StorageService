/**
 * © Panov Vitaly 2023 - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.github.ickee953.micros.storage.service;

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
import java.util.HashMap;
import java.util.Map;
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

    /**
     * Function for save file on backend
     *
     * @return SavedResult with saved file name and saved status. Saved status possible with:
     *
     * OK - file saved
     * ERR_REPLACING - file with this name already exist, error while replacing it
     * NOT_SAVED - file not saved
     *
     * */
    public SavedResult<String, SaveStatus> save(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(Objects.requireNonNull(file.getOriginalFilename())));

            return new SavedResult<>(file.getOriginalFilename(), SaveStatus.OK);
        } catch (Exception e) {
            if( e instanceof FileAlreadyExistsException ){
                LOGGER.info("File with name {} already exist. Replacing...", file.getOriginalFilename());
                try {
                    Files.delete( this.root.resolve(Objects.requireNonNull(file.getOriginalFilename())) );
                } catch (IOException ex) {
                    LOGGER.error("Could not delete the file: {}, {}", file.getOriginalFilename(), ex.getMessage());

                    return new SavedResult<>(file.getOriginalFilename(), SaveStatus.ERR_REPLACING);
                }
                SavedResult<String, SaveStatus> replaced = save( file );
                if( replaced.status == SaveStatus.OK ) {
                    LOGGER.info("File replaced: {}", file.getOriginalFilename());
                }

                return replaced;

            } else {
                String message = "Could not store the file. Error: " + e.getMessage();
                LOGGER.error( message );
            }

            return new SavedResult<>(file.getOriginalFilename(), SaveStatus.NOT_SAVED);
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

    /**
     * OK - file saved
     * ERR_REPLACING - file with this name already exist, error while replacing it
     * NOT_SAVED - file not saved
     * */
    public enum SaveStatus {
        OK,
        ERR_REPLACING,
        NOT_SAVED
    }

    public class SavedResult<K, V> {
        K resource;
        V status;

        public SavedResult(K resource, V status){
            this.resource = resource;
            this.status = status;
        }

        public K getResource() {
            return resource;
        }

        public void setResource(K resource) {
            this.resource = resource;
        }

        public V getStatus() {
            return status;
        }

        public void setStatus(V status) {
            this.status = status;
        }
    }
}
