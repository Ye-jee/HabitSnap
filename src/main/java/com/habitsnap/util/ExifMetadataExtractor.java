package com.habitsnap.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
public class ExifMetadataExtractor {

    /* 이미지 파일에서 EXIF DateTimeOriginal(촬영 일시)를 추출함
     imageFile은 EXIF 정보를 읽을 대상 이미지 파일
     촬영 일시(LocalDateTime)이 없거나 실패하면 null 반환
    * */
    public static LocalDateTime extractDateTime(File imageFile) {
        try {
            // 1) 이미지 파일에서 전체 메타데이터 읽기
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            // 2) EXIF SubIFD 디렉터리(촬영 정보 관련) 가져오기
            //      -> null일 수도 있음 (예: 스크린샷, 편집된 이미지 등)
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            // 3) EXIF 정보 자체가 없을 경우
            if(directory == null){
                log.info("EXIF 정보 없음. 파일: {}", imageFile.getName());
                return null;
            }

            // 4) 촬영 일시(DateTimeOriginal) 태그 추출
            Date originalDate = directory.getDateOriginal();

            // 5) 촬영 시각 태그가 비어 있는 경우 (EXIF는 있지만 촬영 일시 정보가 없는 경우)
            if(originalDate == null) {
                log.info("EXIF DateTimeOriginal 정보 없음. 파일: {}", imageFile.getName());
                return null;
            }

            // 6) java.util.Date -> java.time.LocalDateTime 으로 변환 (타임존은 시스템 기본값, 한국이면 Asia/Seoul)
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    originalDate.toInstant(),
                    ZoneId.systemDefault()
            );

            // 7) 정상적으로 추출된 경우 로그 출력 후 반환
            log.info("EXIF 촬영 시각 추출 성공. 파일: {}, dateTime: {}", imageFile.getName(), dateTime);
            return dateTime;

        // 8) EXIF는 있는데 구조가 깨졌거나 포맷을 지원하지 않는 경우
        } catch (ImageProcessingException e) {
            log.error("EXIF 포맷 해석 실패(비지원 포맷). 파일: {}, 메시지: {}", imageFile.getName(), e.getMessage());

        // 9) 파일 접근 자체가 실패한 경우 (입출력 오류)
        } catch (IOException e) {
            log.error("파일 접근 중 I/O 오류. 파일: {}, 메시지: {}", imageFile.getName(), e.getMessage());

        // 10) 그 외 모든 예외 (알 수 없는 내부 오류)
        } catch (Exception e) {
            log.error("EXIF 처리 중 알 수 없는 오류 발생. 파일: {}, 메시지: {}", imageFile.getName(), e.getMessage(), e);
        }

        // 모든 예외 상황에서 null 반환 -> 상위 로직이 업로드 시각으로 대체 처리하도록 유도
        return null;
    }


}
