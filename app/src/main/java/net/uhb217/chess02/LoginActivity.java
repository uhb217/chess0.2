package net.uhb217.chess02;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import net.uhb217.chess02.ux.utils.Dialogs;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button loginButton;
    TextView signupText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        signupText = findViewById(R.id.signup_text);

        loginButton.setOnClickListener(v ->{
            String user = username.getText().toString();
            String pass = password.getText().toString();
            if (user.isEmpty() || pass.isEmpty()) {
                username.setError("Username cannot be empty");
                password.setError("Password cannot be empty");
            } else {
                Dialog waitingDialog = Dialogs.loginWaitingDialog(this);
                waitingDialog.show();
                String fakeEmail = user.toLowerCase() + "@chess.app.com";
                FirebaseAuth.getInstance().signInWithEmailAndPassword(fakeEmail,pass)
                        .addOnSuccessListener(authResult -> {
                            waitingDialog.dismiss();
                            Intent intent = new Intent(this, RoomActivity.class);
                            intent.putExtra("username", user);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            waitingDialog.dismiss();
                            username.setError("Invalid username or password");
                            password.setError("Invalid username or password");
                        });
            }
        });
        signupText.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }
}