package com.habitsnap.api;

import com.habitsnap.application.FileStorageService;
import com.habitsnap.application.LocalFileStorageService;
import com.habitsnap.dto.MealUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final LocalFileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MealUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        MealUploadResponse response = fileStorageService.saveFile(file);
        return ResponseEntity.ok(response);

        /*String fileName = fileStorageService.saveFile(file);
        return ResponseEntity.ok("파일이 성공적으로 업로드됨: " + fileName);*/
    }


}
