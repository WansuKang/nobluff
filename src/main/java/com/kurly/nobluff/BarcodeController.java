package com.kurly.nobluff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BarcodeController {

    private static final String OPEN_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPEN_API_KEY = "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";

    @PostMapping(value ="/barcode-checker", consumes = {"multipart/form-data"})
    public String barcodeChecker(@RequestParam("file") MultipartFile file) throws IOException {
        // 업로드된 파일을 Base64로 변환
        String base64Image = encodeImageToBase64(file);

        // OpenAI API 호출
        String apiResponse = callOpenAiApi(base64Image);

        // OpenAI API 응답 반환
        return apiResponse;
    }

    private String encodeImageToBase64(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String callOpenAiApi(String base64Image) {
        // RestTemplate 초기화
        RestTemplate restTemplate = new RestTemplate();

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + OPEN_API_KEY);

        // 요청 메시지 생성
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", "If all the barcodes visible in this image are the same, respond with 'true'; if even one is different, respond with 'false'.");

        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + base64Image));

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", new JSONArray().put(textContent).put(imageContent)));

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 300);

        // HTTP 요청 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // API 호출
        ResponseEntity<String> response = restTemplate.exchange(
            OPEN_API_URL,
            HttpMethod.POST,
            entity,
            String.class
                                                               );

        // API 응답 반환
        return response.getBody();
    }
}
