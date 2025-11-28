package com.team3.dto.request;

/**
 * 예약 삭제(취소) 요청 DTO
 * <p>
 * 예약 취소 시 삭제할 예약의 ID를 전송한다.
 * </p>
 * @author bang9634
 * @since 2025-11-28
 */
public class DeleteReservationRequest {
    private final String id;

    public DeleteReservationRequest(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}