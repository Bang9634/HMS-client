package com.team3.client.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.team3.client.HmsClient;
import com.team3.dto.request.AddCustomerRequest;
import com.team3.dto.request.DeleteCustomerRequest;
import com.team3.dto.response.ApiResponse;

/**
 * 고객 관리 API 클라이언트
 * @author bang9634
 * @since 2025-11-29
 */
public class CustomerApi extends HmsClient {
    private static final Logger logger = LoggerFactory.getLogger(CustomerApi.class);

    public CustomerApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    /** 전체 고객 목록 조회 */
    public ApiResponse getCustomerList() {
        try {
            HttpResponse<String> response = sendGet("/api/customer/list");
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("연결 실패: " + e.getMessage());
        }
    }

    /** 고객 검색 (SFR-703) */
    public ApiResponse searchCustomers(String type, String keyword) {
        try {
            // 한글 검색어를 위해 인코딩 처리
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String endpoint = String.format("/api/customer/search?type=%s&keyword=%s", type, encodedKeyword);
            
            HttpResponse<String> response = sendGet(endpoint);
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("검색 실패: " + e.getMessage());
        }
    }

    /** 고객 추가 */
    public ApiResponse addCustomer(AddCustomerRequest request) {
        try {
            HttpResponse<String> response = sendPost("/api/customer/add", request);
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("요청 실패: " + e.getMessage());
        }
    }

    /** 고객 삭제 */
    public ApiResponse deleteCustomer(DeleteCustomerRequest request) {
        try {
            HttpResponse<String> response = sendPost("/api/customer/delete", request);
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("요청 실패: " + e.getMessage());
        }
    }
}