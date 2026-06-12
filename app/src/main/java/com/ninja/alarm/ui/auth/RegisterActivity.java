package com.ninja.alarm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ninja.alarm.R;
import com.ninja.alarm.model.AuthResult;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.ui.HomeActivity;
import com.ninja.alarm.util.Session;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 회원가입 — 이메일/닉네임/비밀번호로 Room 계정을 생성하고 자동 로그인한다. (인증 Phase B4)
 *
 * 비밀번호는 PBKDF2 해시로 저장되며 평문은 보관하지 않는다. 가입 성공 시 바로 홈으로 진입한다.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD = 6;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private TextInputLayout emailLayout;
    private TextInputLayout nicknameLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout passwordConfirmLayout;
    private TextInputEditText emailInput;
    private TextInputEditText nicknameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText passwordConfirmInput;
    private MaterialButton registerButton;
    private MaterialButton backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailLayout = findViewById(R.id.emailLayout);
        nicknameLayout = findViewById(R.id.nicknameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        passwordConfirmLayout = findViewById(R.id.passwordConfirmLayout);
        emailInput = findViewById(R.id.emailInput);
        nicknameInput = findViewById(R.id.nicknameInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordConfirmInput = findViewById(R.id.passwordConfirmInput);
        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        registerButton.setOnClickListener(v -> attemptRegister());
        backToLoginButton.setOnClickListener(v -> finish()); // 로그인 화면으로 복귀
    }

    private void attemptRegister() {
        boolean ok = true;

        String email = text(emailInput);
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_email_invalid));
            ok = false;
        } else {
            emailLayout.setError(null);
        }

        String nickname = text(nicknameInput);
        if (TextUtils.isEmpty(nickname)) {
            nicknameLayout.setError(getString(R.string.error_nickname_empty));
            ok = false;
        } else {
            nicknameLayout.setError(null);
        }

        String password = text(passwordInput);
        if (password.length() < MIN_PASSWORD) {
            passwordLayout.setError(getString(R.string.error_password_short));
            ok = false;
        } else {
            passwordLayout.setError(null);
        }

        String confirm = text(passwordConfirmInput);
        if (!password.equals(confirm)) {
            passwordConfirmLayout.setError(getString(R.string.error_password_mismatch));
            ok = false;
        } else {
            passwordConfirmLayout.setError(null);
        }

        if (!ok) return;

        setBusy(true);
        io.execute(() -> {
            final AuthResult result = Repositories.auth().register(email, nickname, password);
            runOnUiThread(() -> {
                setBusy(false);
                if (result.isSuccess()) {
                    Session.setUserId(this, result.userId); // 가입 후 자동 로그인
                    startActivity(new Intent(this, HomeActivity.class));
                    finishAffinity();
                } else if (result.status == AuthResult.Status.EMAIL_TAKEN) {
                    emailLayout.setError(getString(R.string.error_email_taken));
                } else {
                    passwordLayout.setError(getString(R.string.error_auth_generic));
                }
            });
        });
    }

    private void setBusy(boolean busy) {
        registerButton.setEnabled(!busy);
        backToLoginButton.setEnabled(!busy);
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}
