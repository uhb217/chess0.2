package net.uhb217.chess02;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.uhb217.chess02.ui.PlayerInfoView;
import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.Player;
import net.uhb217.chess02.ux.utils.BoardUtils;
import net.uhb217.chess02.ux.utils.Color;

public class MainActivity extends AppCompatActivity {
  private Board board;
  LinearLayout rootLayout;
  PlayerInfoView topPlayerInfoView, bottomPlayerInfoView;
  Player mainPlayer, opponentPlayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    mainPlayer = (Player) getIntent().getSerializableExtra("mainPlayer");
    opponentPlayer = (Player) getIntent().getSerializableExtra("opponentPlayer");//TODO: move tests
//    mainPlayer = new Player("uhb217", 1600, Color.BLACK);
//    opponentPlayer = new Player("Opponent", 1600, Color.BLACK);

    rootLayout = findViewById(R.id.main);
    board = new Board(this, mainPlayer.getColor(),getIntent().getStringExtra("roomId"));
    topPlayerInfoView = new PlayerInfoView(this, opponentPlayer);
    bottomPlayerInfoView = new PlayerInfoView(this, mainPlayer);

    rootLayout.addView(topPlayerInfoView);
    rootLayout.addView(board);
    rootLayout.addView(bottomPlayerInfoView);

    EditText move = new EditText(this);
    move.setHint("Enter move (e.g., e2e4)");
    Button btn = new Button(this);
    btn.setText("Play Move");
    btn.setOnClickListener(view -> BoardUtils.playMove(BoardUtils.stringFormat2Move(move.getText().toString())));
    rootLayout.addView(move);
    rootLayout.addView(btn);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    String roomId = getIntent().getStringExtra("roomId");
    if (roomId != null)
      FirebaseDatabase.getInstance().getReference("rooms").child(roomId).removeValue();
  }
}