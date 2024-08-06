/**
 * © Panov Vitaly 2023 - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.github.ickee953.micros.storage.service;

import com.github.ickee953.micros.common.SavedStatus;
import com.github.ickee953.micros.common.SavedResult;
import jakarta.annotation.PostConstruct;
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
import java.util.UUID;

@Service
public class FileService implements StorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final String UPLOAD_DIR = "/uploads";

    private final Path root = Paths.get(System.getProperty("user.dir") + UPLOAD_DIR).toAbsolutePath();

    @PostConstruct
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
    public SavedResult<String, SavedStatus> save(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            Files.copy(file.getInputStream(), this.root.resolve(fileName));

            return new SavedResult<>(fileName, SavedStatus.OK);
        } catch (Exception e) {
            if( e instanceof FileAlreadyExistsException ){
                LOGGER.info("File with name {} already exist. Replacing...", fileName);
                try {
                    Files.delete( this.root.resolve(fileName) );
                } catch (IOException ex) {
                    LOGGER.error("Could not delete the file: {}, {}", fileName, ex.getMessage());

                    return new SavedResult<>(fileName, SavedStatus.ERR_REPLACING);
                }
                SavedResult<String, SavedStatus> replaced = save( file );
                if( replaced.getStatus() == SavedStatus.OK ) {
                    LOGGER.info("File replaced: {}", fileName);
                }

                return replaced;

            } else {
                String message = "Could not store the file. Error: " + e.getMessage();
                LOGGER.error( message );
            }

            return new SavedResult<>(fileName, SavedStatus.NOT_SAVED);
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
