package com.dhanmani.mysafetyapplication.Credential;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dhanmani.mysafetyapplication.Home.MainActivity;
import com.dhanmani.mysafetyapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity implements View.OnClickListener {

    EditText email, password, confirmPassword;
    Button SignUp;
    TextView gotoSignIn;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    ImageView arrow;

    UserStatus us;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        us = new UserStatus(Signup.this);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        email = (EditText) findViewById(R.id.email_id);
        password = (EditText) findViewById(R.id.password_id);
        confirmPassword = (EditText) findViewById(R.id.cnf_password_id);
      //  SignUp = (Button) findViewById(R.id.btn_signup_id);
        gotoSignIn = (TextView) findViewById(R.id.go_to_signin_id);

        arrow= (ImageView) findViewById(R.id.arrow_id);

        arrow.setOnClickListener(this);

      //  SignUp.setOnClickListener(this);
        gotoSignIn.setOnClickListener(this);

        // I have initialized the required variables....
    }

    // Onclick() its a listener once user clicks on the button or to any widgets then this onClick() method is called

    // to execute the defined methods...

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.arrow_id:
                Register();
                break;
            case R.id.go_to_signin_id:
                startActivity(new Intent(this, Signin.class));
                break;
        }
    }

    // Register() its for manually signup...


    private void Register() {
        String getEmail= email.getText().toString().trim();
        String getPassword= password.getText().toString().trim();
        String getConfirmPassword= confirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(getEmail)) {
            email.setError("Please input your Email id!");
            return;
        }
        if (TextUtils.isEmpty(getPassword)) {
            password.setError("Please input your Password!");
            return;
        }
        if (TextUtils.isEmpty(getConfirmPassword)) {
            confirmPassword.setError("Please input this confirm password field!");
            return;
        }
        if (!getPassword.equals(getConfirmPassword)) {
            confirmPassword.setError("Passwords donot match, please try again!");
            return;
        }
        if (!isValidEmail(getEmail)) {
            email.setError("Please input valid email!");
            return;
        }
        if (getPassword.length()< 6) {
            password.setError("Password length must be > 5");
        }
        if (getConfirmPassword.length()< 6) {
            password.setError("Incorrect password! p;ease try again");
        }
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth.createUserWithEmailAndPassword(getEmail,getPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Signup.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                    Intent intent= new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(Signup.this, "SignUp Failed!", Toast.LENGTH_SHORT).show();
                    System.out.println("Task failed due to: " + task.getException());
                }
                progressDialog.dismiss();
            }
        });
    }

    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}