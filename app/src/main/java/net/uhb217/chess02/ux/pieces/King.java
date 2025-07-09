package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
  private boolean hasMoved = false; // Track if the king has moved for castling purposes

  public King(@NonNull Context ctx, Pos pos, Color color) {
    super(ctx, pos, color);
  }

  @Override
  public char charCode() {
    return 'k';
  }

  @Override
  protected int resId() {
    return color == Color.WHITE ? R.drawable.wk : R.drawable.bk;
  }

  @Override
  public void move(int x, int y, boolean bySystem) {
    int d = Board.getInstance().getColor() == Color.WHITE ? 1 : -1;
    if (pos.x + 2 * d == x)//kingSide castle
      Board.getInstance().getPiece(new Pos(pos.x + 3 * d, y)).placeAt(x - d, y); // Move the rook during castling
    else if (pos.x - 2 * d == x)//queenSide castle
      Board.getInstance().getPiece(new Pos(pos.x - 4 * d, y)).placeAt(x + d, y); // Move the rook during castling
    super.move(x, y, bySystem);
    hasMoved = true; // Mark the king as moved when it is moved

  }

  @Override
  public List<Pos> getLegalMoves(Piece[][] boardPos) {
    List<Pos> legalMoves = new ArrayList<>();
    int[] directions = {-1, 0, 1};

    for (int dx : directions) {
      for (int dy : directions) {
        if (dx == 0 && dy == 0) continue; // Skip the current position

        int x = pos.x + dx;
        int y = pos.y + dy;

        // Check if the move is within bounds
        if (x >= 0 && x <= 7 && y >= 0 && y <= 7) {
          Pos target = new Pos(x, y);
          Piece piece = boardPos[target.x][target.y];

          // Add the move if the target square is empty or occupied by an opponent piece
          if (piece == null || piece.color != this.color) {
            if (!isInCheck(target))
              legalMoves.add(target);
          }
        }
      }
    }
    int d = Board.getInstance().getColor() == Color.WHITE ? 1 : -1;
    if (canCastle(true))
      legalMoves.add(new Pos(pos.x + 2 * d, pos.y)); // King-side castling
    if (canCastle(false))
      legalMoves.add(new Pos(pos.x - 2 * d, pos.y)); // Queen-side castling


    return legalMoves;
  }

  /**
   * Same as {@link #canCastle(boolean)} but ignores the pieces in the way of the king and rook for the fen.
   *
   * @param kingSide true for king-side castling, false for queen-side castling
   * @return true if castling is possible, false otherwise
   */
  public boolean canCastle4fen(boolean kingSide) {
    Board board = Board.getInstance();
    Piece rook = board.getPiece(new Pos((board.getColor() == Color.WHITE ? kingSide ? 7 : 0 : kingSide ? 0 : 7), pos.y));
    if (this.hasMoved) return false;
    if (!(rook instanceof Rook)) return false;
    return !((Rook) rook).isMoved();
  }

  private boolean canCastle(boolean kingSide) {
    Board board = Board.getInstance();
    int d = (kingSide ? 1 : -1) * (board.getColor() == Color.WHITE ? 1 : -1);
    Piece rook = board.getPiece(new Pos((board.getColor() == Color.WHITE ? kingSide ? 7 : 0 : kingSide ? 0 : 7), pos.y));
    if (hasMoved) return false; // Cannot castle if the king has moved
    if (rook instanceof Rook) {
      if (((Rook) rook).isMoved()) return false; // Cannot castle if the rook has moved
      if (!kingSide)
        if (board.getPiece(pos.x + 3 * d, pos.y) != null)
          return false; // Cannot castle if the path is blocked or the king would be in check
      for (int i = 1; i <= 2; i++) {
        Pos target = new Pos(pos.x + i * d, pos.y);
        if (board.getPiece(target) != null || isInCheck(target))
          return false; // Cannot castle if the path is blocked or the king would be in check
      }
      return true; // Can castle if the king and rook have not moved, and the path is clear
    } else return false; // Cannot castle if there is no rook
  }

  public boolean isInCheck() {
    // Check if the king is currently in check
    return isInCheck(Board.getInstance().getBoard(), pos);
  }

  private boolean isInCheck(Pos newKingPos) {
    // Check if the move puts the king in check by simulating the move
    Piece[][] simulatedBoard = Board.getInstance().getBoardCopy();
    simulatedBoard[pos.x][pos.y] = null; // Remove the king from its current position
    simulatedBoard[newKingPos.x][newKingPos.y] = this; // Place the king in the target position
    return isInCheck(simulatedBoard, newKingPos);
  }

  private boolean isInCheck(Piece[][] board, Pos newKingPos) {
    for (Piece[] pieceRow : board)
      for (Piece enemyPiece : pieceRow) {
        if (enemyPiece == null || enemyPiece.color == this.color || enemyPiece instanceof King)
          continue;
        if (enemyPiece instanceof Pawn && Pos.contains(((Pawn) enemyPiece).getCaptureMoves(board), newKingPos))
          return true; // King is in check by an enemy pawn
        else if (Pos.contains(enemyPiece.getLegalMoves(board), newKingPos))
          return true; // King is in check by an enemy piece
      }
    return false; // King is not in check
  }

  public boolean isInCheck(Piece[][] board) {
    return isInCheck(board, this.pos);
  }

  public boolean isMoved() {
    return hasMoved;
  }
}
