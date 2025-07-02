package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
  public Knight(@NonNull Context ctx, Pos pos, Color color) {
    super(ctx, pos, color);
  }

  @Override
  protected int resId() {
    return color == Color.WHITE ? R.drawable.wn : R.drawable.bn;
  }

  @Override
  public List<Pos> getLegalMoves(Piece[][] board) {
    List<Pos> legalMoves = new ArrayList<>();

    // All possible L-shaped moves for a knight
    int[][] moves = {
        {1, 2}, {2, 1}, {2, -1}, {1, -2},
        {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}
    };

    for (int[] move : moves) {
      int x = pos.x + move[0];
      int y = pos.y + move[1];

      // Check if the move is within board boundaries
      if (x >= 0 && x <= 7 && y >= 0 && y <= 7) {
        Piece piece = board[x][y];
        // Add the move if the square is empty or occupied by an opponent's piece
        if (piece == null || piece.color != this.color)
          legalMoves.add(new Pos(x, y));

      }
    }

    return legalMoves;
  }
}
