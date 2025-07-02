package net.uhb217.chess02;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.uhb217.chess02.ui.PlayerInfoView;
import net.uhb217.chess02.ux.Player;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Dialogs;

import java.util.Random;

public class RoomActivity extends AppCompatActivity {
    Button createRoomButton, joinButton;
    EditText roomIdInput;
    String username = "?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_room);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        username = getIntent().getStringExtra("username");
        createRoomButton = findViewById(R.id.create_room_button);
        joinButton = findViewById(R.id.join_button);
        roomIdInput = findViewById(R.id.room_code_edit);

        Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser(), player -> {
            if (player != null)
                PlayerInfoView.overrideXMLPlayerInfoView(findViewById(R.id.bottomPlayerInfo), player);
            else
                Log.e("Player", "Failed to fetch player data");
        });

        createRoomButton.setOnClickListener(view -> createUniqueRoom(this));
        joinButton.setOnClickListener(v -> {
            String roomId = roomIdInput.getText().toString().trim().toUpperCase();

            if (roomId.isEmpty()) {
                roomIdInput.setError("Room ID cannot be empty");
                return;
            }

            DatabaseReference roomRef = FirebaseDatabase.getInstance()
                    .getReference("rooms").child(roomId);
            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()){
                        roomIdInput.setError("Room does not exist");
                        return;
                    }
                    String player2 = snapshot.child("player2").getValue(String.class);
                    int white = snapshot.child("white").getValue(Integer.class);
                    if (player2 != null) {
                        roomIdInput.setError("Room is already full");
                        return;
                    }
                    roomRef.child("player2").setValue(username)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(RoomActivity.this, "Joined room: " + roomId, Toast.LENGTH_SHORT).show();
                                // TODO: Start game activity here
                                Intent intent = new Intent(RoomActivity.this, MainActivity.class);
                                intent.putExtra("color", white);

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RoomActivity.this, "Failed to join room", Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });


    }
    public void createUniqueRoom(Context context) {
        tryGenerateRoom(context, 0);
    }

    private void tryGenerateRoom(Context context,int attempts) {
        if (attempts >= 5) {
            Toast.makeText(context, "Failed to create room. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String roomId = generateRoomId();
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retry if room already exists
                    tryGenerateRoom(context,attempts + 1);
                } else {
                    Log.d("RoomActivity", "Room ID is unique: " + roomId);
                    // Room is safe to create
                    roomRef.child("player1").setValue(username);
                    Dialogs.showWaitingDialog(context, roomRef::removeValue);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error checking room ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static String generateRoomId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder id = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            id.append(chars.charAt(random.nextInt(chars.length())));
        }

        return id.toString(); // e.g., "G2K9X7"
    }

}