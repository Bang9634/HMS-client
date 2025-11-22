package com.team3.model;

import java.io.Serializable;

/**
 * 보고서 정보 저장 모델 클래스
 */
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String content; // 보고서 내용

    public Report(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
}