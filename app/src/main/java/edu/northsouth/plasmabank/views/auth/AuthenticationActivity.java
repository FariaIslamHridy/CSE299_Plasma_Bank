package edu.northsouth.plasmabank.views.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import edu.northsouth.plasmabank.R;
import edu.northsouth.plasmabank.utils.CustomProgressBar;

public class AuthenticationActivity extends AppCompatActivity {
    private CustomProgressBar progressBar;

    private EditText mPhone;
    Button mNext;

    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_authentication);

        mPhone = findViewById(R.id.et_phoneNumber);
        mNext = findViewById(R.id.btn_next);

        progressBar = new CustomProgressBar(this);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = mPhone.getText().toString().trim();

                progressBar.show("");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.hide();

                        if (phoneValidation(phoneNumber)) {
                            Intent intent = new Intent(AuthenticationActivity.this, VerificationActivity.class);
                            intent.putExtra("phoneNumber", "+88" + phoneNumber);

                            startActivity(intent);
                        }

                    }
                }, 550);

            }
        });
    }

    private boolean phoneValidation(String phoneNumber) {
        if (phoneNumber.length() < 11) {
            mPhone.setError("Invalid");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}