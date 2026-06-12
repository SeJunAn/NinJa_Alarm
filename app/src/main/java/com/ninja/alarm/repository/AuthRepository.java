package com.ninja.alarm.repository;

import com.ninja.alarm.model.AuthResult;

/**
 * 인증 경계 — 로그인/회원가입. (지시서 BE Phase B4)
 *
 * 비밀번호 해시·검증·중복 이메일 처리는 구현체(Room)가 담당하고,
 * UI 는 이 인터페이스에만 의존한다. CPU 부하가 있으므로 호출은 백그라운드 스레드에서 한다.
 */
public interface AuthRepository {

    /** 이메일+비밀번호로 로그인. 성공 시 userId 를 담은 SUCCESS, 아니면 INVALID_CREDENTIALS. */
    AuthResult login(String email, String password);

    /** 신규 가입. 이메일 중복이면 EMAIL_TAKEN, 성공 시 새 userId 를 담은 SUCCESS. */
    AuthResult register(String email, String nickname, String password);
}
