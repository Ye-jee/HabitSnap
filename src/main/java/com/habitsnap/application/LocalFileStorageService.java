package com.habitsnap.application;

import com.habitsnap.dto.MealUploadResponse;
import com.habitsnap.util.ExifMetadataExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService{

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 반환 타입 변경 : String -> MealUploadResponse
    @Override
    public MealUploadResponse saveFile(MultipartFile file) {

        String fileName = file.getOriginalFilename();

        // 1) 프로젝트 루트 기준으로 업로드 디렉터리 uploads 경로 생성
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        Path uploadPath = rootPath.resolve(uploadDir);

        try {
            // 2) 업로드 디렉터리 없으면 자동 생성
            Files.createDirectories(uploadPath);

            // 3) 실제 저장할 때 파일 전체 경로 (폴더+파일명)
            Path filePath = uploadPath.resolve(fileName);

            // 4) MultipartFile -> 실제 파일로 저장
            file.transferTo(filePath.toFile());     // MultipartFile 안에 들어있는 실제 파일 데이터를 filePath 위치에 복사 저장, 여기서 진짜 디스크에 파일이 생성됨
            log.info("파일 업로드 완료 → 경로: {}", filePath.toAbsolutePath());

            // 5) EXIF 메타데이터에서 촬영 시각 추출
            LocalDateTime shotAt = ExifMetadataExtractor.extractDateTime(filePath.toFile());

            // 6) 예외 케이스 대비: EXIF 없을 경우 업로드 시각으로 대체
            LocalDate mealDate = (shotAt != null) ? shotAt.toLocalDate() : LocalDate.now();
            LocalTime mealTime = (shotAt != null) ? shotAt.toLocalTime() : LocalTime.now();

            if(shotAt != null) {
                log.info("EXIF 촬영 시각 추출 성공 및 적용 -> mealDate: {}, mealTime: {}", mealDate, mealTime);
            }
            else {
                log.info("EXIF 촬영 시각 정보 없음 -> 업로드 시각으로 대체, mealDate: {}, mealTime: {}", mealDate, mealTime);
            }

            // 7) DTO로 응답 반환
            return MealUploadResponse.builder()
                    .fileName(fileName)
                    .mealDate(mealDate)
                    .mealTime(mealTime)
                    .build();

        } catch (IOException e) {
            log.error("파일 저장 중 I/O 오류 발생. 파일: {}, 메시지: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
        }catch (Exception e) {
            log.error("파일 저장 중 알 수 없는 오류 발생. 파일: {}, 메시지: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
        }


    }
}
