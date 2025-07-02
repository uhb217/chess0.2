package net.uhb217.chess02;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import net.uhb217.chess02.ui.PlayerInfoView;
import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.Player;
import net.uhb217.chess02.ux.utils.Color;

public class MainActivity extends AppCompatActivity {
    private Board board;
    LinearLayout rootLayout;
    PlayerInfoView topPlayerInfoView, bottomPlayerInfoView;
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
        rootLayout = findViewById(R.id.main);
        board = new Board(this,Color.WHITE);
        topPlayerInfoView = new PlayerInfoView(this, Player.empty());
        Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser(), player -> {
            if (player != null)
                bottomPlayerInfoView = new PlayerInfoView(this, player);
            else {
                bottomPlayerInfoView = new PlayerInfoView(this, Player.empty());
                Log.e("Player", "Failed to fetch player data");
            }
        });
        rootLayout.addView(topPlayerInfoView);
        rootLayout.addView(board);
    }

}