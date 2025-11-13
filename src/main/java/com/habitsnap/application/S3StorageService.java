package com.habitsnap.application;

import com.habitsnap.dto.MealUploadResponse;
import com.habitsnap.util.ExifMetadataExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Profile("prod")
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements FileStorageService{

    // AWS S3에 요청을 보내는 클라이언트 객체
    private final S3Client s3Client;

    // application.yml에 있는 설정값을 주입받는 부분
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public MealUploadResponse saveFile(MultipartFile file) {

        // 1) S3에 저장될 고유 파일명 생성
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // 2) S3 업로드 요청 객체 생성
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        try {
            // 3) 실제 S3에 파일 업로드
             s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
             log.info("S3 업로드 성공 -> s3://{}/{}", bucket, fileName);

            // 4) 프론트/DB가 사용할 공개 URL 생성
            String imageUrl = "https://" + bucket + ".s3." + System.getenv("AWS_REGION") + ".amazonaws.com/" + fileName;

            // 5) EXIF 추출을 위해 임시 파일 생성 (S3는 직접 EXIF 추출 불가)
            File temp = Files.createTempFile("habitsnap-", "").toFile();
            file.transferTo(temp);

            // EXIF 촬영시각 파싱
            LocalDateTime shotAt = ExifMetadataExtractor.extractDateTime(temp);

            LocalDate mealDate;
            LocalTime mealTime;

            // 6) EXIF 있는 경우
            if(shotAt != null){
                mealDate = shotAt.toLocalDate();
                mealTime = shotAt.toLocalTime();
                log.info("EXIF 촬영시각 추출 성공 -> {}", shotAt);
            }
            // 7) EXIF 없는 경우 업로드 시각으로 대체
            else {
                mealDate = LocalDate.now();
                mealTime = LocalTime.now();
                log.info("EXIF 정보 없음 -> 업로드 시각 기준 대체");
            }

            // 8) 임시 파일 삭제
            temp.delete();

            // 9) 업로드 결과 DTO 반환
            return MealUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .mealDate(mealDate)
                    .mealTime(mealTime)
                    .build();

        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("S3 업로드 실패", e);
        }


    }
}
