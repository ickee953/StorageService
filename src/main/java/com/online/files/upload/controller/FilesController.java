/**
 * © Panov Vitaly 2023 - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.online.files.upload.controller;

import com.online.files.upload.FilesStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Controller
public class FilesController {

    @Autowired
    FilesStorageService storageService;

    @RequestMapping(
	    value = "/upload",
	    method = RequestMethod.POST,
	    produces = MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadFile(
	    @RequestPart(name = "files", required = true) List<MultipartFile> files
    ) {
        AtomicReference<HttpStatus> status = new AtomicReference<>(HttpStatus.EXPECTATION_FAILED);
	    if( !files.isEmpty() ){
            List<String> uploadedUrls = new LinkedList<>();
            Phaser       phase        = new Phaser(1);

            status.set(HttpStatus.OK);
            files.forEach(file-> {
                phase.register();
                new Thread(() -> {
                    try {
                        String uploadedFilename = storageService.save(file);
                        uploadedUrls.add(uploadedFilename);
                    } catch(RuntimeException e) {
                        status.set(HttpStatus.EXPECTATION_FAILED);
                    } finally {
                        phase.arriveAndDeregister();
                    }
                }).start();
            } );

	        phase.arriveAndAwaitAdvance();
	        String result = uploadedUrls.stream().map(Object::toString).collect(Collectors.joining(","));

            return ResponseEntity.status(status.get()).body(result);
	    }

        return ResponseEntity.status(status.get()).body(null);

    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = storageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
