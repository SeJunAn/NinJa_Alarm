package com.ninja.alarm.model;

/**
 * 로그인/회원가입 결과. (인증 경계 — AuthRepository)
 * 성공 시 userId 가 채워지고, 실패 시 status 로 사유를 구분한다.
 */
public class AuthResult {

    public enum Status {
        SUCCESS,
        INVALID_CREDENTIALS, // 로그인: 이메일/비밀번호 불일치
        EMAIL_TAKEN,         // 회원가입: 이미 가입된 이메일
        ERROR                // 그 외 내부 오류
    }

    public final Status status;
    public final long userId;

    private AuthResult(Status status, long userId) {
        this.status = status;
        this.userId = userId;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public static AuthResult success(long userId) {
        return new AuthResult(Status.SUCCESS, userId);
    }

    public static AuthResult failure(Status status) {
        return new AuthResult(status, -1L);
    }
}
