package com.team3.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * JSON 관련 유틸리티
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final Gson gson = new Gson();

     /**
     * 응답 본문에서 토큰 추출
     * 
     * @param jsonBody 서버 응답 JSON 문자열
     * @return 토큰 값 (없으면 null)
     */
    public static String extract(String jsonBody, String key) {
        try {
            JsonObject obj = gson.fromJson(jsonBody, JsonObject.class);
            
            if (obj.has(key)) {
                String value = obj.get(key).getAsString();
                logger.debug("응답 본문 추출 시도: {}={}", key, value);
                return value;
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Json 파싱 예외 발생");
        }
        return null;
    }
}