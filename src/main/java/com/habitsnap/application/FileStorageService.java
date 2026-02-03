package com.habitsnap.application;

import com.habitsnap.dto.mealrecord.response.MealUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    MealUploadResponse saveFile(MultipartFile file);
}
