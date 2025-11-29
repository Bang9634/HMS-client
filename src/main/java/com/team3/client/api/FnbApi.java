package com.team3.client.api;

import java.io.IOException;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.team3.client.HmsClient;
import com.team3.dto.request.AddFnbRequest;
import com.team3.dto.request.DeleteFnbRequest;
import com.team3.dto.response.ApiResponse;

public class FnbApi extends HmsClient {
    private static final Logger logger = LoggerFactory.getLogger(FnbApi.class);

    public FnbApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    public ApiResponse getFnbList() {
        try {
            HttpResponse<String> response = sendGet("/api/fnb/list");
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("연결 실패: " + e.getMessage());
        }
    }

    public ApiResponse addFnbItem(AddFnbRequest request) {
        try {
            HttpResponse<String> response = sendPost("/api/fnb/add", request);
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("요청 실패: " + e.getMessage());
        }
    }

    public ApiResponse deleteFnbItem(DeleteFnbRequest request) {
        try {
            HttpResponse<String> response = sendPost("/api/fnb/delete", request);
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("요청 실패: " + e.getMessage());
        }
    }
}