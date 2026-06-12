package com.ninja.alarm.util;

import android.util.Base64;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 온디바이스 비밀번호 해시 — PBKDF2 + per-user salt.
 *
 * minSdk 24 호환을 위해 PBKDF2WithHmacSHA1 을 사용한다(SHA256 변형은 API26+).
 * salt/hash 는 Base64 문자열로 보관하며 {@code UserEntity.passwordSalt/passwordHash} 에 저장한다.
 * 평문 비밀번호는 어디에도 저장하지 않는다.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;

    private PasswordHasher() {}

    /** 새 임의 salt(Base64). 회원가입 시 1회 생성한다. */
    public static String newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /** 평문 비밀번호 + saltBase64 → 해시(Base64). CPU 부하가 크므로 백그라운드에서 호출할 것. */
    public static String hash(String password, String saltBase64) {
        byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
        char[] chars = password.toCharArray();
        try {
            KeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("password hashing failed", e);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /** 평문 비밀번호가 저장된 salt/hash 와 일치하는지 상수시간 비교. */
    public static boolean verify(String password, String saltBase64, String expectedHashBase64) {
        if (saltBase64 == null || expectedHashBase64 == null) return false;
        String actual = hash(password, saltBase64);
        return constantTimeEquals(actual, expectedHashBase64);
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] ba = a.getBytes();
        byte[] bb = b.getBytes();
        if (ba.length != bb.length) return false;
        int diff = 0;
        for (int i = 0; i < ba.length; i++) diff |= ba[i] ^ bb[i];
        return diff == 0;
    }
}
