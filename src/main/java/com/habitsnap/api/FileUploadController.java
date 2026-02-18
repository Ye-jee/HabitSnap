package com.habitsnap.api;

import com.habitsnap.application.photo.PhotoUploadService;
import com.habitsnap.dto.mealrecord.response.MealUploadResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;
@Hidden
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {     // 파일 업로드 기능 확인

    private final PhotoUploadService photoUploadService;

    // private final LocalFileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MealUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {

        MealUploadResponse response = photoUploadService.uploadAndExtract(file);
        // MealUploadResponse response = fileStorageService.saveFile(file);

        return ResponseEntity.ok(response);

        /*String fileName = fileStorageService.saveFile(file);
        return ResponseEntity.ok("파일이 성공적으로 업로드됨: " + fileName);*/
    }


}
