package net.uhb217.chess02.ux.utils;

import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.pieces.Piece;

public class BoardUtils {
  public static void playMove(Move move){
    Board board = Board.getInstance();
    if (move == null || move.from == null || move.to == null)
      throw new IllegalArgumentException("Move or its positions cannot be null");

    Piece movingPiece = board.getPiece(move.from);
    if (movingPiece == null) return; // No piece at the 'from' position
    if (!Pos.contains(movingPiece.getLegalMoves(), move.to)) return;

    movingPiece.move(move.to.x, move.to.y,false);
  }
  public static Move stringFormat2Move(String move) {
    if (move.length() != 4 || !Character.isLetter(move.charAt(0)) || !Character.isDigit(move.charAt(1)) ||
        !Character.isLetter(move.charAt(2)) || !Character.isDigit(move.charAt(3)))
      throw new IllegalArgumentException("Invalid move format: " + move);

    boolean white = Board.getInstance().getColor() == Color.WHITE;

    int fromX = white ? move.charAt(0) - 'a' : 'h' - move.charAt(0);
    int fromY = white ? 8 - (move.charAt(1) - '0') : move.charAt(1) - '0' - 1;
    int toX   = white ? move.charAt(2) - 'a' : 'h' - move.charAt(2);
    int toY   = white ? 8 - (move.charAt(3) - '0') : move.charAt(3) - '0' - 1;
    return new Move(new Pos(fromX, fromY), new Pos(toX, toY));
  }
  public static String move2StringFormat(Pos from, Pos to) {
    return move2StringFormat(from.x, from.y, to.x, to.y);
  }
  public static String move2StringFormat(int fromX, int fromY, int toX, int toY) {
    boolean white = Board.getInstance().getColor() == Color.WHITE;

    char fromFile = white ? (char) (fromX + 'a') : (char) ('h' - fromX);
    int fromRank = white ? 8 - fromY : fromY + 1;

    char toFile = white ? (char) (toX + 'a') : (char) ('h' - toX);
    int toRank = white ? 8 - toY : toY + 1;

    return "" + fromFile + fromRank + toFile + toRank;
  }

  public static class Move{
    public final Pos from;
    public final Pos to;

    public Move(Pos from, Pos to) {
      this.from = from;
      this.to = to;
    }

  }
}
