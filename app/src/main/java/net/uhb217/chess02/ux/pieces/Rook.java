package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
  private boolean hasMoved = false; // Track if the rook has moved for castling purposes

  public Rook(@NonNull Context ctx, Pos pos, Color color) {
    super(ctx, pos, color);
  }

  @Override
  protected int resId() {
    return color == Color.WHITE ? R.drawable.wr : R.drawable.br;
  }

  @Override
  public void move(int x, int y) {
    super.move(x, y);
    this.hasMoved = true; // Mark the rook as moved when it is moved
  }

  @Override
  public List<Pos> getLegalMoves(Piece[][] board) {
    List<Pos> legalMoves = new ArrayList<>();
    int[] directions = {-1, 1};// Horizontal and vertical directions
    for (int d : directions) {
      // Move horizontally (left and right)
      int x = pos.x + d;
      while (x >= 0 && x <= 7) {
        Piece piece = board[x][pos.y];
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
        Piece piece = board[pos.x][y];
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

  public void setHasMoved(boolean hasMoved) {
    this.hasMoved = hasMoved;
  }

  public boolean isMoved() {
    return hasMoved;
  }
}
