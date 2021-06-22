package com.dhanmani.mysafetyapplication.Credential;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dhanmani.mysafetyapplication.Home.MainActivity;
import com.dhanmani.mysafetyapplication.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Signin extends AppCompatActivity implements View.OnClickListener {

    EditText email, password;
    Button signIn;
    TextView goToSignUp;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth, mAuth;

    ImageView google, phone;

    GoogleSignInClient mGoogleSignInClient;

    UserStatus us;

    private final static int RC_SIGN_IN= 123;

    TextView forgetPassword, signup;

    ImageView arrow;

    // Defining onStart() to check whether user is already registered or new user...

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user= firebaseAuth.getCurrentUser();

        if (user!=null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        us= new UserStatus(Signin.this);

        forgetPassword= (TextView) findViewById(R.id.forget_password_id);

        forgetPassword.setOnClickListener(this);

        createRequest();

        progressDialog= new ProgressDialog(this);

        firebaseAuth= FirebaseAuth.getInstance();

        mAuth= FirebaseAuth.getInstance();

        email= (EditText) findViewById(R.id.email_id);
        password= (EditText) findViewById(R.id.password_id);

        arrow= (ImageView) findViewById(R.id.arrow_id);

        arrow.setOnClickListener(this);

        signup= (TextView) findViewById(R.id.create_new_one_id);

        google= (ImageView) findViewById(R.id.g_signin_id);

        google.setOnClickListener(this);

        signup.setOnClickListener(this);

      //  signIn.setOnClickListener(this);
      //  goToSignUp.setOnClickListener(this);

        // in the above code I initialized all the requred variables....

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.arrow_id:
                Toast.makeText(this, "Clicked on signin", Toast.LENGTH_SHORT).show();
                Login();
                break;
            case R.id.create_new_one_id:
                startActivity(new Intent(this, Signup.class));
                break;
            case R.id.forget_password_id:
                reset1();
                break;
            case R.id.g_signin_id:
                signIn();
                break;
        }
    }

    // In this createRequest() method using GoogleSignInOptions along with an api key to signin directly into the ap without register...

    private void createRequest() {
        GoogleSignInOptions gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("150134937666-0d7j1u2c6d801er9k564171tnnqmuup2.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
    }

    // In this Signin() I am using an Intent to fire or enable the google signin ...

    // As a result the control next goes to the onActivityResult() method there after proper validations

    // the control flow goes to the google signin api ...

    private void signIn() {
        Intent signInIntent= mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account= task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {}
        }
    }

    // In this firebaseAuthWithGoogle(GoogleSignInAccount account) the final step of signin is done

    // once authenticated by firebase then the user gets redirect to the app dashboard...

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user= firebaseAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else {
                            Toast.makeText(Signin.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Login() method is for manually signin to the app but before that signup() is required...

    private void Login() {
        String getEmail= email.getText().toString().trim();
        String getPassword= password.getText().toString().trim();

        if (TextUtils.isEmpty(getEmail)) {
            email.setError("Please input your Email id!");
            return;
        }
        if (TextUtils.isEmpty(getPassword)) {
            password.setError("Please input your Password!");
            return;
        }
        if (!isValidEmail(getEmail)) {
            email.setError("Please input valid email!");
            return;
        }
        if (getPassword.length()< 6) {
            password.setError("Password length must be > 5");
        }
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth.signInWithEmailAndPassword(getEmail,getPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(Signin.this, "SignIn Successfull!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(Signin.this, "Signin error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reset1() {
        AlertDialog.Builder a11= new AlertDialog.Builder(Signin.this);
        a11.setTitle("Reset password");
        a11.setMessage("Password reset link has been sent");
        a11.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder a10= new AlertDialog.Builder(Signin.this);
                a10.setTitle("Forget password");
                a10.setCancelable(false);
                a10.setMessage("Password reset link has been sent to your emailid");
                a10.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
             //   AlertDialog a101= a10.create();
             //   a101.show();
            }
        });

        a11.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog a1= a11.create();
        a1.show();

        String Email= email.getText().toString().trim();
        if (TextUtils.isEmpty(Email) || !isValidEmail(Email)) {
            email.setError("Please enter emailid to receive reset link");
        }
        else {
            firebaseAuth.sendPasswordResetEmail(Email).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(Signin.this, "Password reset link has been sent to your emailid", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Signin.this, "Error sending reset link due to: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}