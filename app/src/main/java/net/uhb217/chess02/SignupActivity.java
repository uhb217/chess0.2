package net.uhb217.chess02;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import net.uhb217.chess02.ui.Dialogs;

import java.util.Map;

public class SignupActivity extends AppCompatActivity {
  EditText username, password;
  Button signupButton;
  TextView loginText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_signup);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });
    username = findViewById(R.id.username);
    password = findViewById(R.id.password);
    signupButton = findViewById(R.id.signup_button);
    loginText = findViewById(R.id.login_text);

    signupButton.setOnClickListener(v -> {
      String user = username.getText().toString();
      String pass = password.getText().toString();
      if (user.isEmpty() || pass.isEmpty()) {
        // Show error message
        username.setError("Username cannot be empty");
        password.setError("Password cannot be empty");
      } else {
        Dialog waitingDialog = Dialogs.INSTANCE.signupWaitingDialog(this);
        waitingDialog.show();
        String fakeEmail = user.toLowerCase() + "@chess.app.com";
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(fakeEmail, pass)
            .addOnSuccessListener(authResult -> {
              authResult.getUser().updateProfile(new UserProfileChangeRequest.Builder()
                  .setDisplayName(user).build());
              Map<String, Object> map = Map.of(
                  "uid", authResult.getUser().getUid(),
                  "rating", 1200,
                  "wins", 0,
                  "losses", 0,
                  "draws", 0
              );
              FirebaseDatabase.getInstance().getReference("users").child(user).setValue(map)
                  .addOnSuccessListener(aVoid -> {
                    waitingDialog.dismiss();
                    startActivity(new Intent(this, LoginActivity.class));
                    Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show();
                  })
                  .addOnFailureListener(e -> Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> {
              waitingDialog.dismiss();
              if (e instanceof FirebaseAuthUserCollisionException)
                username.setError("Username already exists");
              else
                Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show();
            });
      }
    });
    loginText.setOnClickListener(view -> startActivity(new Intent(this, LoginActivity.class)));

  }
}