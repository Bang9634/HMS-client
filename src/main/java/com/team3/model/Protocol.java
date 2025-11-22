package com.team3.model;

import java.io.Serializable;

public class Protocol implements Serializable {
    private static final long serialVersionUID = 1L;

    // [100] 로그인
    public static final int PT_LOGIN_REQ = 100;
    public static final int PT_LOGIN_RES = 101;
    public static final int PT_LOGIN_FAIL = 102;

    // [200] 사용자 관리
    public static final int PT_USER_ADD_REQ = 201;
    public static final int PT_USER_DELETE_REQ = 202;
    public static final int PT_USER_LIST_REQ = 203;

    // [300] 보고서 관리
    public static final int PT_REPORT_REQ = 301;
    
    // [400] 결제 관리
    public static final int PT_PAYMENT_LIST_REQ = 401; // 결제 목록 요청

    // [500] 객실 관리
    public static final int PT_ROOM_LIST_REQ = 501; // 객실 목록 요청
    public static final int PT_ROOM_LIST_RES = 502; // 객실 목록 응답
    public static final int PT_ROOM_ADD_REQ = 503;  // 객실 추가 요청
    public static final int PT_ROOM_ADD_RES = 504;  // 객실 추가 응답

    // [공통]
    public static final int PT_SUCCESS = 299;
    public static final int PT_FAIL = 298;

    private final int type;
    private final Object data;

    public Protocol(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int getType() { return type; }
    public Object getData() { return data; }
}