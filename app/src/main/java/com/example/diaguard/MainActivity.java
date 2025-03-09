package com.example.diaguard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diaguard.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference users;

    private RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button btnSignIn = findViewById(R.id.btnLogIn);
        Button btnRegister = findViewById(R.id.btnRegister);
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        root = findViewById(R.id.main);
        // TODO: 27.02.2025 Remove

        btnRegister.setOnClickListener(view -> showRegisterWindow());
        btnSignIn.setOnClickListener(view -> showSignInWindow());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        startActivity(new Intent(MainActivity.this, MapActivity.class));
    }

    private void showSignInWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Log in your account");
        dialog.setMessage("Enter your email and password");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signin_window = inflater.inflate(R.layout.sign_in_window, null);
        dialog.setView(signin_window);

        final TextInputEditText email = signin_window.findViewById(R.id.emailField);
        final TextInputEditText pass = signin_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Log in", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(email.getText().toString())) {
                Snackbar.make(root, "Enter your email", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (pass.getText().toString().length() < 6) {
                Snackbar.make(root, "Enter a password that is longer than 5 characters", Snackbar.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    startActivity(new Intent(MainActivity.this, MapActivity.class));
                    finish();
                }
            }).addOnFailureListener(e -> Snackbar.make(root, "Authorization error. " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Create an account");
        dialog.setMessage("Enter your details to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        final TextInputEditText email = register_window.findViewById(R.id.emailField);
        final TextInputEditText pass = register_window.findViewById(R.id.passField);
        final TextInputEditText name = register_window.findViewById(R.id.nameField);
        final TextInputEditText phone = register_window.findViewById(R.id.phoneField);

        dialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Add", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(email.getText().toString())) {
                Snackbar.make(root, "Enter your email", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(name.getText().toString())) {
                Snackbar.make(root, "Enter your name", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(phone.getText().toString())) {
                Snackbar.make(root, "Enter your phone", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (pass.getText().toString().length() < 6) {
                Snackbar.make(root, "Enter a password that is longer than 5 characters", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Регистрация пользователя
            auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        // Пользователь успешно зарегистрирован
                        User user = new User();
                        user.setEmail(email.getText().toString());
                        user.setName(name.getText().toString());
                        user.setPass(pass.getText().toString());
                        user.setPhone(phone.getText().toString());

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(unused -> {
                                    Snackbar.make(root, "User was added!", Snackbar.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Snackbar.make(root, "Failed to save user data: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Обработка ошибок регистрации
                        Snackbar.make(root, "Registration failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }
}