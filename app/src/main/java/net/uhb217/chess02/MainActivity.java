package net.uhb217.chess02;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.FirebaseDatabase;

import net.uhb217.chess02.ui.PlayerInfoView;
import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.Player;
import net.uhb217.chess02.ux.utils.MoveHistory;

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
    opponentPlayer = (Player) getIntent().getSerializableExtra("opponentPlayer");
//    mainPlayer = new Player("uhb217", 1600, Color.BLACK);
//    opponentPlayer = new Player("Opponent", 1600, Color.BLACK);

    rootLayout = findViewById(R.id.board_container);
    String roomId = getIntent().getStringExtra("roomId");
    if (roomId.length() == 6)
      board = new Board(this, mainPlayer.getColor(),getIntent().getStringExtra("roomId"));
    else
      board = new Board(this, mainPlayer.getColor(),Integer.parseInt(roomId));

    topPlayerInfoView = new PlayerInfoView(this, opponentPlayer);
    bottomPlayerInfoView = new PlayerInfoView(this, mainPlayer);

    rootLayout.addView(topPlayerInfoView);
    rootLayout.addView(board);
    rootLayout.addView(bottomPlayerInfoView);

    bottomGameControlsSetup();


    Button btn = new Button(this);
    btn.setText("Play Move");
    btn.setOnClickListener(view -> Log.d("fen: ", board.toFEN()));
    rootLayout.addView(btn);

  }
  private void bottomGameControlsSetup(){
    findViewById(R.id.back_btn).setOnClickListener(view -> Board.getInstance().fromFEN(MoveHistory.INSTANCE.moveBack().getFen()));
    findViewById(R.id.forward_btn).setOnClickListener(view -> Board.getInstance().fromFEN(MoveHistory.INSTANCE.moveForward().getFen()));
    findViewById(R.id.resign_btn).setOnClickListener(view -> {});
    findViewById(R.id.draw_btn).setOnClickListener(view -> {});

  }
  @Override
  protected void onDestroy() {
    super.onDestroy();
    String roomId = getIntent().getStringExtra("roomId");
    if (roomId != null)
      FirebaseDatabase.getInstance().getReference("rooms").child(roomId).removeValue();
  }
}