package com.example.learn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        etEmail = findViewById(R.id.editTextText);
        etPassword = findViewById(R.id.editTextTextPassword);
        Button btnSignup = findViewById(R.id.button2);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String pass = etPassword.getText().toString();
                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseRef.refAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    goToMain();
                                } else {
                                    showError(task.getException());
                                }
                            }
                        });
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String pass = etPassword.getText().toString();
                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseRef.refAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    goToMain();
                                } else {
                                    showError(task.getException());
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // if the user is already logged in go straight to the list
        if (FirebaseRef.refAuth.getCurrentUser() != null) {
            goToMain();
        }
    }

    private void goToMain() {
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish();
    }

    private void showError(Exception exp) {
        if (exp instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseAuthWeakPasswordException) {
            Toast.makeText(this, "Password too weak.", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "User already exists.", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
        } else if (exp instanceof FirebaseNetworkException) {
            Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }
}
