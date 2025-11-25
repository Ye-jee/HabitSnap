package com.habitsnap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    protected MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        // application/octet-stream 요청도 읽을 수 있도록 허용
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    // 쓰기 관련 기능은 막고, 읽기(read)만 허용
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}
