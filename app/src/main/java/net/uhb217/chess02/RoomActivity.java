package net.uhb217.chess02;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.uhb217.chess02.ui.PlayerInfoView;
import net.uhb217.chess02.ux.Player;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Dialogs;
import net.uhb217.chess02.ux.utils.FirebaseUtils;
import net.uhb217.chess02.ux.utils.StockfishApi;

import java.util.Random;

public class RoomActivity extends AppCompatActivity {
  Button createRoomButton, joinButton;
  FrameLayout playVSStockfish;
  EditText roomIdInput;
  private boolean triggered = false;

  @SuppressLint("ClickableViewAccessibility")
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
      roomRef.addListenerForSingleValueEvent(FirebaseUtils.ValueListener(snapshot -> {
        if (!snapshot.exists()) {
          roomIdInput.setError("Room does not exist");
          return;
        }
        Player player1 = snapshot.child("player1").getValue(Player.class);
        Player player2 = snapshot.child("player2").getValue(Player.class);
        if (player2 != null || player1 == null) {
          roomIdInput.setError("Room is already full or closed");
          return;
        }

        Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser(), player -> {
          player.setColor(player1.getColor().opposite());
          roomRef.child("player2").setValue(player)
              .addOnSuccessListener(unused -> startGameActivity(roomId, player, player1));
        });
      }));
    });
    playVSStockfish = findViewById(R.id.play_vs_stockfish);
    playVSStockfish.setOnClickListener(view -> {
      Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser()
          ,player -> startGameActivity("15", player.setColor(Color.WHITE)
          ,Player.Stockfish(15)));
    });
  }

  public void createUniqueRoom(Context context) {
    tryGenerateRoom(context, 0);
  }

  private void tryGenerateRoom(Context context, int attempts) {
    if (attempts >= 5) {
      Toast.makeText(context, "Failed to create room. Try again.", Toast.LENGTH_SHORT).show();
      return;
    }

    String roomId = generateRoomId();
    DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);

    roomRef.addListenerForSingleValueEvent(FirebaseUtils.ValueListener(snapshot -> {
      if (snapshot.exists())// Retry if room already exists
        tryGenerateRoom(context, attempts + 1);
      else {
        Dialog waitingDialog = Dialogs.INSTANCE.showWaitingDialog(context, roomId, roomRef::removeValue);
        // Room is safe to create
        Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser(), player -> {
          if (player != null) {
            player.setColor(new Random().nextBoolean() ? Color.WHITE : Color.BLACK);
            roomRef.child("player1").setValue(player);

            roomRef.child("player2").addValueEventListener(FirebaseUtils.ValueListener(snapshot1 -> {
              Log.d("RoomActivity", "Player2 data changed: " + snapshot1.getValue());
              if (!triggered && snapshot1.exists()) {
                triggered = true;
                startGameActivity(roomId, player, snapshot1.getValue(Player.class));
              }
            }));
          } else {
            Log.e("Player", "Failed to fetch player data");
            Dialogs.INSTANCE.dismissWaitingDialog();
          }
        });
      }
    }));
  }

  private static String generateRoomId() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder id = new StringBuilder();
    Random random = new Random();

    for (int i = 0; i < 6; i++) {
      id.append(chars.charAt(random.nextInt(chars.length())));
    }

    return id.toString(); // e.g., "G2K9X7"
  }

  private void startGameActivity(String roomId, Player mainPlayer, Player opponentPlayer) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra("mainPlayer", mainPlayer);
    intent.putExtra("opponentPlayer", opponentPlayer);
    intent.putExtra("roomId", roomId);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Dialogs.INSTANCE.dismissWaitingDialog();
  }

  // Helper method for color blending
}
