package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
  public Bishop(@NonNull Context ctx, Pos pos, Color color) {
    super(ctx, pos, color);
  }

  @Override
  public char charCode() {
    return 'b';
  }

  @Override
  protected int resId() {
    return color == Color.WHITE ? R.drawable.wb : R.drawable.bb;
  }

  @Override
  public List<Pos> getLegalMoves(Piece[][] boardPos) {
    List<Pos> legalMoves = new ArrayList<>();
    int[] directions = {-1, 1}; // Used for diagonal directions

    // Explore all four diagonal directions
    for (int dx : directions)
      for (int dy : directions) {
        int x = pos.x + dx;
        int y = pos.y + dy;

        while (x >= 0 && x <= 7 && y >= 0 && y <= 7) {
          Piece piece = boardPos[x][y];
          if (piece == null)
            // Empty square, valid move
            legalMoves.add(new Pos(x, y));
          else {
            // Capture if it's an opponent's piece
            if (piece.color != this.color)
              legalMoves.add(new Pos(x, y));

            // Stop further movement in this direction
            break;
          }
          x += dx;
          y += dy;
        }
      }
    return legalMoves;
  }
}
