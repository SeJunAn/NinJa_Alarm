package com.ninja.alarm.repository.room;

import android.content.Context;
import android.util.Log;

import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.entity.UserEntity;
import com.ninja.alarm.model.AuthResult;
import com.ninja.alarm.repository.AuthRepository;
import com.ninja.alarm.util.PasswordHasher;

/**
 * Room 기반 인증 — users 테이블에 PBKDF2 해시로 계정을 저장/검증한다.
 *
 * 입력 정규화(트림/소문자 이메일)는 여기서 처리한다. 비밀번호 평문은 저장하지 않는다.
 */
public class RoomAuthRepository implements AuthRepository {

    private static final String TAG = "RoomAuth";

    private final NinjaDatabase db;

    public RoomAuthRepository(Context context) {
        this.db = NinjaDatabase.getInstance(context);
    }

    @Override
    public AuthResult login(String email, String password) {
        try {
            String normalized = normalizeEmail(email);
            UserEntity user = db.userDao().getByEmail(normalized);
            // 계정 없음 또는 비밀번호 미설정(게스트 seed 계정)은 로그인 불가.
            if (user == null || user.passwordHash == null || user.passwordSalt == null) {
                return AuthResult.failure(AuthResult.Status.INVALID_CREDENTIALS);
            }
            if (!PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
                return AuthResult.failure(AuthResult.Status.INVALID_CREDENTIALS);
            }
            return AuthResult.success(user.userId);
        } catch (Exception e) {
            Log.e(TAG, "login failed", e);
            return AuthResult.failure(AuthResult.Status.ERROR);
        }
    }

    @Override
    public AuthResult register(String email, String nickname, String password) {
        try {
            String normalized = normalizeEmail(email);
            if (db.userDao().getByEmail(normalized) != null) {
                return AuthResult.failure(AuthResult.Status.EMAIL_TAKEN);
            }

            String salt = PasswordHasher.newSalt();
            String hash = PasswordHasher.hash(password, salt);
            long now = System.currentTimeMillis();

            // 신규 계정은 성장표 기준 0exp·레벨1·하급닌자로 시작. user_id 는 자동 생성.
            UserEntity user = new UserEntity(
                    0, normalized, nickname.trim(), hash, salt,
                    0, 1, "하급닌자", now, now);

            long id = db.userDao().insertNew(user);
            return AuthResult.success(id);
        } catch (android.database.sqlite.SQLiteConstraintException dup) {
            // getByEmail 검사와 insert 사이 경합 등으로 unique 충돌 시.
            return AuthResult.failure(AuthResult.Status.EMAIL_TAKEN);
        } catch (Exception e) {
            Log.e(TAG, "register failed", e);
            return AuthResult.failure(AuthResult.Status.ERROR);
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
