package net.uhb217.chess02;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.Color;
import net.uhb217.chess02.ux.pieces.Pawn;
import net.uhb217.chess02.ux.Pos;

public class MainActivity extends AppCompatActivity {
    private Board board;
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
        board = new Board(this, Color.BLACK);
//        board.addView(new Pawn(this,new Pos(1,1), Color.BLACK));

        ((LinearLayout)findViewById(R.id.main)).addView(board);
    }

    public Board getBoard() {
        return board;
    }
}