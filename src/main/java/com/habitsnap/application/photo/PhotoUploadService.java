package com.habitsnap.application.photo;

import com.habitsnap.application.FileStorageService;
import com.habitsnap.application.LocalFileStorageService;
import com.habitsnap.dto.MealUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/* 파일 업로드와 EXIF 추출을 통합 관리하는 서비스
 컨트롤러는 이 서비스만 호출하면 됨
* */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoUploadService {

    // private final LocalFileStorageService fileStorageService;
    private final FileStorageService fileStorageService;

    /* 파일을 저장하고 EXIF 메타데이터를 분석해 결과를 반환함
     @param file > 업로드된 이미지 파일
     @return MealUploadResponse > (파일명, 날짜, 시각)
    * */
    public MealUploadResponse uploadAndExtract(MultipartFile file){
        log.info("[PhotoUploadService] 업로드 요청 수신: {}", file.getOriginalFilename());
        MealUploadResponse response = fileStorageService.saveFile(file);
        log.info("[PhotoUploadService] 업로드 및 EXIF 추출 완료 -> {}", response);
        return response;
    }

}
