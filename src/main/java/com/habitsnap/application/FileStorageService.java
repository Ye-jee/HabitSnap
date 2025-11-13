package com.habitsnap.application;

import com.habitsnap.dto.MealUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    MealUploadResponse saveFile(MultipartFile file);
}
