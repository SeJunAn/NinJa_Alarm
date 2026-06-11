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
import com.ninja.alarm.ui.HomeActivity;

/**
 * 로그인/회원가입 (지시서 Phase 1 #2).
 *
 * 인증 로직 자체는 BE(김동환) 담당이라 여기서는 입력 유효성만 검사하고
 * 통과 시 홈으로 진입한다. (Fake 흐름)
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputLayout emailLayout = findViewById(R.id.emailLayout);
        TextInputLayout nicknameLayout = findViewById(R.id.nicknameLayout);
        TextInputEditText emailInput = findViewById(R.id.emailInput);
        TextInputEditText nicknameInput = findViewById(R.id.nicknameInput);
        MaterialButton enterButton = findViewById(R.id.enterButton);

        enterButton.setOnClickListener(v -> {
            boolean ok = true;

            String email = emailInput.getText() == null ? "" : emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.setError(getString(R.string.error_email_invalid));
                ok = false;
            } else {
                emailLayout.setError(null);
            }

            String nickname = nicknameInput.getText() == null ? "" : nicknameInput.getText().toString().trim();
            if (TextUtils.isEmpty(nickname)) {
                nicknameLayout.setError(getString(R.string.error_nickname_empty));
                ok = false;
            } else {
                nicknameLayout.setError(null);
            }

            if (ok) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });
    }
}
