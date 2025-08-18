package net.uhb217.chess02;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.uhb217.chess02.ui.Dialogs;
import net.uhb217.chess02.ui.PlayerInfoView;
import net.uhb217.chess02.ux.Player;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.FirebaseUtils;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class RoomFragment extends Fragment {
  Button createRoomButton, joinButton;
  FrameLayout playVSStockfish;
  EditText roomIdInput;
  ImageView clientIcon;
  private boolean triggered = false;
  private ActivityResultLauncher<Void> takePicture;

  public RoomFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_room, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    createRoomButton = view.findViewById(R.id.create_room_button);
    joinButton = view.findViewById(R.id.join_button);
    roomIdInput = view.findViewById(R.id.room_code_edit);
    clientIcon = view.findViewById(R.id.player_bottom_icon);

    takePicture = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), this::clientIconClick);

    clientIcon.setOnClickListener(v -> takePicture.launch(null));

    Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser(), player -> {
      if (player != null)
        PlayerInfoView.overrideXMLPlayerInfoView(view.findViewById(R.id.bottomPlayerInfo), player);
      else
        Log.e("Player", "Failed to fetch player data");
    });

    createRoomButton.setOnClickListener(v -> createUniqueRoom(getContext()));
    joinButton.setOnClickListener(v -> {
      String roomId = roomIdInput.getText().toString().trim().toUpperCase();

      if (roomId.isEmpty()) {
        roomIdInput.setError("Room ID cannot be empty");
        return;
      }

      DatabaseReference roomRef = FirebaseDatabase.getInstance()
          .getReference("rooms").child(roomId);
      roomRef.addListenerForSingleValueEvent(FirebaseUtils.valueListener(snapshot -> {
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
    playVSStockfish = view.findViewById(R.id.play_vs_stockfish);
    playVSStockfish.setOnClickListener(v -> Dialogs.INSTANCE.stockfishDialog(getContext()
        , () -> Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser()
            , player -> startGameActivity(String.valueOf(Dialogs.INSTANCE.getStockfishElo()), player.setColor(Color.WHITE)
                , Player.Stockfish(Dialogs.INSTANCE.getStockfishElo())))));
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

    roomRef.addListenerForSingleValueEvent(FirebaseUtils.valueListener(snapshot -> {
      if (snapshot.exists())// Retry if room already exists
        tryGenerateRoom(context, attempts + 1);
      else {
        Dialogs.INSTANCE.showWaitingDialog(context, roomId, roomRef::removeValue);
        // Room is safe to create
        Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser(), player -> {
          if (player != null) {
            player.setColor(new Random().nextBoolean() ? Color.WHITE : Color.BLACK);
            roomRef.child("player1").setValue(player);

            roomRef.child("player2").addValueEventListener(FirebaseUtils.valueListener(snapshot1 -> {
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
    Intent intent = new Intent(getContext(), MainActivity.class);
    intent.putExtra("mainPlayer", mainPlayer);
    intent.putExtra("opponentPlayer", opponentPlayer);
    intent.putExtra("roomId", roomId);
    startActivity(intent);
  }

  private void clientIconClick(Bitmap bitmap) {
    clientIcon.setImageBitmap(bitmap);

    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, boas);
    String base64encoded = Base64.encodeToString(boas.toByteArray(), Base64.DEFAULT);

    String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    FirebaseDatabase.getInstance().getReference("users").child(username).child("base64encodedIcon").setValue(base64encoded)
        .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Icon saved", Toast.LENGTH_SHORT).show());
  }
}