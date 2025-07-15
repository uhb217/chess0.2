package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

  public Rook(@NonNull Context ctx, Pos pos, Color color) {
    super(ctx, pos, color);
  }

  @Override
  public char charCode() {
    return 'r';
  }

  @Override
  protected int resId() {
    return color == Color.WHITE ? R.drawable.wr : R.drawable.br;
  }

  @Override
  public void move(int x, int y, boolean bySystem) {
    if (pos.y == (color == Board.getInstance().getColor() ? 7 : 0)){
      if (pos.x == (color == Color.WHITE ? 7 : 0))
          Board.getInstance().getKing(color).isKingsideAvailable = false;
      else if (pos.x == (color == Color.WHITE ? 0 : 7))
          Board.getInstance().getKing(color).isQueensideAvailable = false;
    }

    super.move(x, y, bySystem);
  }

  @Override
  public List<Pos> getLegalMoves(Piece[][] boardPos) {
    List<Pos> legalMoves = new ArrayList<>();
    int[] directions = {-1, 1};// Horizontal and vertical directions
    for (int d : directions) {
      // Move horizontally (left and right)
      int x = pos.x + d;
      while (x >= 0 && x <= 7) {
        Piece piece = boardPos[x][pos.y];
        if (piece == null)
          legalMoves.add(new Pos(x, pos.y));
        else {
          if (piece.color != this.color)
            legalMoves.add(new Pos(x, pos.y)); // Capture opponent piece
          break; // Stop if a piece is encountered
        }
        x += d;
      }
      // Move vertically (up and down)
      int y = pos.y + d;
      while (y >= 0 && y <= 7) {
        Piece piece = boardPos[pos.x][y];
        if (piece == null)
          legalMoves.add(new Pos(pos.x, y));
        else {
          if (piece.color != this.color)
            legalMoves.add(new Pos(pos.x, y)); // Capture opponent piece
          break; // Stop if a piece is encountered
        }
        y += d;
      }
    }

    return legalMoves;
  }

}
