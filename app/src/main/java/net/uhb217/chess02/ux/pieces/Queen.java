package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Color;
import net.uhb217.chess02.ux.Pos;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece{
    public Queen(@NonNull Context ctx, Pos pos, Color color) {
        super(ctx, pos, color);
    }

    @Override
    protected int resId() {
        return color == Color.WHITE ? R.drawable.wq : R.drawable.bq;
    }

    @Override
    public List<Pos> getLegalMoves(Piece[][] board) {
        List<Pos> legalMoves = new ArrayList<>();
        int[] directions = {-1, 0, 1}; // Directions for horizontal, vertical, and diagonal movement

        // Loop through all directions
        for (int dx : directions)
            for (int dy : directions) {
                // Skip if both dx and dy are 0 (no movement)
                if (dx == 0 && dy == 0) continue;

                int x = pos.x + dx;
                int y = pos.y + dy;

                // Move in the given direction until blocked
                while (x >= 0 && x <= 7 && y >= 0 && y <= 7) {
                    Piece piece = board[x][y];
                    if (piece == null)
                        legalMoves.add(new Pos(x, y)); // Empty square, valid move
                    else {
                        if (piece.color != this.color)
                            legalMoves.add(new Pos(x, y)); // Capture opponent's piece
                        break; // Stop if any piece is encountered
                    }
                    x += dx;
                    y += dy;
                }
            }

        return legalMoves;
    }
}
