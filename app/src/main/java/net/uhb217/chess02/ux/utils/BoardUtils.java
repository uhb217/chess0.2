package net.uhb217.chess02.ux.utils;

import android.content.Context;
import android.util.Log;

import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.pieces.Bishop;
import net.uhb217.chess02.ux.pieces.Knight;
import net.uhb217.chess02.ux.pieces.Piece;
import net.uhb217.chess02.ux.pieces.Queen;
import net.uhb217.chess02.ux.pieces.Rook;

public class BoardUtils {
  public static void playMove(Move move){
    Board board = Board.getInstance();
    while (MoveHistory.INSTANCE.canMoveForward())
      board.fromFEN(MoveHistory.INSTANCE.moveForward());

    if (move == null || move.from == null || move.to == null)
      throw new IllegalArgumentException("Move or its positions cannot be null");

    Piece movingPiece = board.getPiece(move.from);
    if (movingPiece == null) return;

    if (move.premotion == '?')
      movingPiece.move(move.to.x, move.to.y,true);
    else {
      movingPiece.placeAt(move.to.x, move.to.y);
      board.enPassant = null;
      Piece newPiece = newPieceFromChar(move.premotion, movingPiece.getContext(), move.to, movingPiece.getColor());
      if (newPiece != null) {
        newPiece.placeAt(move.to.x, move.to.y);
        board.addView(newPiece);
        board.nextTurn(true);
      }
    }
  }


  /**
   * @param move in UCI format
   * @return new Move
   * @see Move
   */
  public static Move UCI2Move(String move) {
    char premotion = '?';
    if (move.length() != 4 || !Character.isLetter(move.charAt(0)) || !Character.isDigit(move.charAt(1)) ||
        !Character.isLetter(move.charAt(2)) || !Character.isDigit(move.charAt(3))){
      if (move.length() == 5 && Character.isLetter(move.charAt(4)))
        premotion = move.charAt(4);
      else
        throw new IllegalArgumentException("Invalid move format: " + move);
    }

    boolean white = Board.getInstance().getColor() == Color.WHITE;

    int fromX = white ? move.charAt(0) - 'a' : 'h' - move.charAt(0);
    int fromY = white ? 8 - (move.charAt(1) - '0') : move.charAt(1) - '0' - 1;
    int toX   = white ? move.charAt(2) - 'a' : 'h' - move.charAt(2);
    int toY   = white ? 8 - (move.charAt(3) - '0') : move.charAt(3) - '0' - 1;
    return new Move(new Pos(fromX, fromY), new Pos(toX, toY), premotion);
  }
  public static String move2UCI(Pos from, Pos to) {
    return move2UCI(from.x, from.y, to.x, to.y);
  }

  /**
   * @param fromX
   * @param fromY
   * @param toX
   * @param toY
   * @return move in UCI format
   */
  public static String move2UCI(int fromX, int fromY, int toX, int toY) {
    return pos2Square(new Pos(fromX, fromY)) + pos2Square(new Pos(toX, toY));
  }
  public static String pos2Square(Pos pos) {
    boolean white = Board.getInstance().getColor() == Color.WHITE;
    char file = white ? (char) (pos.x + 'a') : (char) ('h' - pos.x);
    int rank = white ? 8 - pos.y : pos.y + 1;
    return "" + file + rank;
  }
  public static Piece newPieceFromChar(char pieceChar, Context ctx, Pos pos, Color color){
    Piece newPiece = null;
    switch (pieceChar) {
      case 'q':
        newPiece = new Queen(ctx, pos, color);
        break;
      case 'r':
        newPiece = new Rook(ctx, pos, color);
        break;
      case 'b':
        newPiece = new Bishop(ctx, pos, color);
        break;
      case 'n':
        newPiece = new Knight(ctx, pos, color);
        break;
    }
    return newPiece;
  }
  public static class Move{
    public final Pos from;
    public final Pos to;
    public final char premotion;
    public Move(Pos from, Pos to, char premotion) {
      this.from = from;
      this.to = to;
      this.premotion = premotion;
    }
    public Move(Pos from, Pos to) {
      this(from, to, '?');
    }

  }
}
