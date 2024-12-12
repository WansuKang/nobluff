package com.kurly.nobluff;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private static final String DEFAULT_BARCODE_CHECK_PROMPT = "If all the barcodes visible in this image are the same, respond with 'true'; if even one is different, respond with 'false'.";

    @PostMapping(value ="/ai-checker", consumes = {"multipart/form-data"})
    public String barcodeChecker(@RequestParam("file") MultipartFile file, @RequestParam(required = false) String prompt) throws IOException {
        if(prompt == null) {
            prompt = DEFAULT_BARCODE_CHECK_PROMPT;
        }
        return callOpenAiApi(file, prompt).getChoices().get(0).getMessage().getContent();
    }

    private GptResponse callOpenAiApi(MultipartFile file, String prompt) throws IOException {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        RestTemplate restTemplate = new RestTemplate();

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + OPEN_API_KEY);

        // 요청 메시지 생성
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", prompt);

        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + base64Image));

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", new JSONArray().put(textContent).put(imageContent)));

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 300);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(OPEN_API_URL, HttpMethod.POST, entity, String.class);
        return GsonUtils.fromJson(response.getBody(), GptResponse.class);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GptResponse {

        private List<Choice> choices;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Choice {

            private Message message;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Message {

                private String content;

            }
        }
    }
}
