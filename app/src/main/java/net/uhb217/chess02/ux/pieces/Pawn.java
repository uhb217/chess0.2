package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.utils.BoardUtils;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Dialogs;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
  private final int d;

  public Pawn(@NonNull Context ctx, Pos pos, Color color) {
    super(ctx, pos, color);
    this.d = -color.code * Board.getInstance().getColor().code;//direction
  }

  @Override
  protected int resId() {
    return color == Color.WHITE ? R.drawable.wp : R.drawable.bp;
  }

  @Override
  public void move(int x, int y, boolean updateFirebase) {
    Board board = Board.getInstance();
    int lastX = pos.x;
    int lastY = pos.y;
    if (pos.x != x && board.getPiece(x, y) == null) {
      board.removeView(board.getPiece(x, y - d));
      board.movePieceInTheArray(x, y, x, y - d);
    }
    placeAt(x, y);
    removeAllPoints();
    board.setClickedPiece(null);
    board.enPassant = null;
    if (lastY + 2 * d == y)
      board.enPassant = new Pos(x, lastY + d); // Set en passant target square
    if (y == (color == Board.getInstance().getColor() ? 0 : 7)) {//premotion
      Dialogs.showPromotionDialog(getContext(), color, pieceChar -> {
        Piece newPiece = BoardUtils.newPieceFromChar(pieceChar, getContext(), new Pos(x, y), color);
        if (newPiece != null) {
          if (updateFirebase) board.sendMoveToFirebase(BoardUtils.move2StringFormat(lastX,lastY,x, y) + pieceChar);
          newPiece.placeAt(x, y);
          board.addView(newPiece);
          board.nextTurn();
        }
      });
    } else
      board.nextTurn();
    if (updateFirebase)
      board.sendMoveToFirebase(BoardUtils.move2StringFormat(lastX,lastY,x, y));
  }

  @Override
  public List<Pos> getLegalMoves(Piece[][] board) {
    Color boardColor = Board.getInstance().getColor();
    List<Pos> legalMoves = new ArrayList<>();

    if (board[pos.x][pos.y + d] == null)
      legalMoves.add(new Pos(pos.x, pos.y + d));
    if (pos.y == (color == boardColor ? 6 : 1) && board[pos.x][pos.y + 2 * d] == null)
      legalMoves.add(new Pos(pos.x, pos.y + 2 * d));
    if (getCaptureMoves(board) != null)
      legalMoves.addAll(getCaptureMoves(board));

    // En passant capture(not implemented in getCaptureMoves because its not effects the king moves)
    Pos enPassant = Board.getInstance().enPassant;
    if (enPassant != null) {
      if (pos.x < 7 && enPassant.equals(pos.x + 1, pos.y + d))
        legalMoves.add(new Pos(pos.x + 1, pos.y + d));
      if (pos.x > 0 && enPassant.equals(pos.x - 1, pos.y + d))
        legalMoves.add(new Pos(pos.x - 1, pos.y + d));
    }
    return legalMoves;
  }

  public List<Pos> getCaptureMoves(Piece[][] board) {
    List<Pos> captureMoves = new ArrayList<>();

    if (pos.x < 7 && pos.y + d >= 0 && pos.y + d <= 7 && board[pos.x + 1][pos.y + d] != null && board[pos.x + 1][pos.y + d].color != color)
      captureMoves.add(new Pos(pos.x + 1, pos.y + d));
    if (pos.x > 0 && pos.y + d >= 0 && pos.y + d <= 7 && board[pos.x - 1][pos.y + d] != null && board[pos.x - 1][pos.y + d].color != color)
      captureMoves.add(new Pos(pos.x - 1, pos.y + d));

    return captureMoves;
  }


}
