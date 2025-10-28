package com.habitsnap.application;

import com.habitsnap.domain.HelloEntity;
import com.habitsnap.domain.HelloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 샘플 서비스
@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;

    public String createHello(String message){
        HelloEntity entity = new HelloEntity(message);
        helloRepository.save(entity);
        return "✅ Saved message: " + message;
    }

}
