package com.github.ickee953.micros.storage.service;

import com.github.ickee953.micros.common.SaveStatus;
import com.github.ickee953.micros.common.SavedResult;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    SavedResult<String, SaveStatus> save(MultipartFile file);

    Resource load(String filename);

}
