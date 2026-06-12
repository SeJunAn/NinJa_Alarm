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
 * 로그인 — 이메일+비밀번호로 Room 계정을 인증한다. (인증 Phase B4)
 *
 * 해시 검증은 CPU 부하가 있어 백그라운드에서 수행하고 결과만 UI 로 돌려준다.
 * 가입 버튼은 회원가입 화면으로, 게스트 버튼은 시드 로컬 사용자로 둘러보게 한다.
 */
public class LoginActivity extends AppCompatActivity {

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton enterButton;
    private MaterialButton signupButton;
    private MaterialButton guestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        enterButton = findViewById(R.id.enterButton);
        signupButton = findViewById(R.id.signupButton);
        guestButton = findViewById(R.id.guestButton);

        enterButton.setOnClickListener(v -> attemptLogin());

        signupButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        guestButton.setOnClickListener(v -> {
            // 비밀번호 없는 시드 로컬 사용자로 둘러보기.
            Session.setUserId(this, Session.GUEST_USER_ID);
            goHome();
        });
    }

    private void attemptLogin() {
        boolean ok = true;

        String email = text(emailInput);
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_email_invalid));
            ok = false;
        } else {
            emailLayout.setError(null);
        }

        String password = text(passwordInput);
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_password_empty));
            ok = false;
        } else {
            passwordLayout.setError(null);
        }

        if (!ok) return;

        setBusy(true);
        io.execute(() -> {
            final AuthResult result = Repositories.auth().login(email, password);
            runOnUiThread(() -> {
                setBusy(false);
                if (result.isSuccess()) {
                    Session.setUserId(this, result.userId);
                    goHome();
                } else {
                    // 보안상 어떤 필드가 틀렸는지 구분하지 않고 공통 메시지를 보여준다.
                    passwordLayout.setError(getString(R.string.error_login_failed));
                }
            });
        });
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
    }

    private void setBusy(boolean busy) {
        enterButton.setEnabled(!busy);
        signupButton.setEnabled(!busy);
        guestButton.setEnabled(!busy);
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
