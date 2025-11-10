package com.habitsnap.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService{

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String saveFile(MultipartFile file) {

        String fileName = file.getOriginalFilename();

        // 프로젝트 루트 기준으로 uploads 경로 생성
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        Path uploadPath = rootPath.resolve(uploadDir);

        try {
            // 폴더 없으면 자동 생성
            Files.createDirectories(uploadPath);

            // 파일 저장 경로 지정
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());     // MultipartFile 안에 들어있는 실제 파일 데이터를 filePath 위치에 복사 저장, 여기서 진짜 디스크에 파일이 생성됨

            log.info("파일 업로드 완료 → 경로: {}", filePath.toAbsolutePath());
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }


    }
}
