package com.sparta.spartadelivery.user.domain.entity;

public enum Role {
    // 고객 : 주문 생성, 본인 주문 조회, 리뷰 작성
    CUSTOMER,
    // 가게 주인 : 본인 가게/메뉴/주문 관리, 주문 상태 변경
    OWNER,
    // 서비스 담당자 : 모든 가게/주문 관리 권한
    MANAGER,
    // 최종 관리자 : 전체 권한 + MANAGER 관리/생성/삭제
    MASTER;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
