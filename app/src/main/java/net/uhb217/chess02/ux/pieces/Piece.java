package net.uhb217.chess02.ux.pieces;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.utils.BoardUtils;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Point;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Piece extends AppCompatImageView {
  protected Pos pos;
  protected Color color;
  protected final int SIZE;

  public Piece(@NonNull Context ctx, Pos pos, Color color) {
    super(ctx);
    this.pos = pos;
    this.color = color;
    this.SIZE = ctx.getResources().getDisplayMetrics().widthPixels / 8;
    this.updatePos();
    setOnClickListener(this::onClick);
    setImageResource(resId());
  }

  protected abstract int resId();

  public abstract List<Pos> getLegalMoves(Piece[][] board);

  public List<Pos> getLegalMoves() {
    Board board = Board.getInstance();
    if (board.getColor() == board.getTurnColor())
      return List.of(); // No legal moves if it's not this piece's turn
    return this instanceof King ? getLegalMoves(board.getBoard()) :
        getLegalMoves(board.getBoard()).stream()
            .filter(p -> !isPinnedToKing(p)).collect(Collectors.toList());
  }

  public boolean isPinnedToKing(Pos newPos) {
    Board board = Board.getInstance();
    Piece[][] simulatedBoard = board.getBoardCopy();
    simulatedBoard[pos.x][pos.y] = null; // Remove the piece from its current position
    simulatedBoard[newPos.x][newPos.y] = this;
    return board.getKing(color).isInCheck(simulatedBoard);
  }

  public Color getColor() {
    return color;
  }

  protected void onClick(View view) {
    Board board = Board.getInstance();
    if (board.getClickedPiece() != this) {
      //display legal moves by points
      removeAllPoints();
      List<Pos> legalMoves = getLegalMoves();
      for (Pos legalMove : legalMoves)
        board.addView(new Point(getContext(), legalMove, this));
      board.setClickedPiece(this);
    } else {
      removeAllPoints();
      board.setClickedPiece(null);
    }
  }

  protected void updatePos() {
    setX(pos.x * SIZE);
    setY(pos.y * SIZE);
    setLayoutParams(new FrameLayout.LayoutParams(SIZE, SIZE));
  }
  public void move(Pos pos) {
    move(pos.x, pos.y);
  }
  public void  move(int x, int y){
    move(x, y, true);
  }

  /**
   * Moves the piece to the specified position and updates the board state.
   * Pawn completely overrides this.
   * In King and Rook its with extra logic for castling.
   * @param x the target x-coordinate
   * @param y the target y-coordinate
   * @param updateFirebase whether to update the Firebase database with this move(recursion prevention)
   */
  public void move(int x, int y, boolean updateFirebase) {
    Board board = Board.getInstance();
    if (updateFirebase)
      board.sendMoveToFirebase(BoardUtils.move2StringFormat(pos.x, pos.y, x, y));
    placeAt(x, y);
    removeAllPoints();
    board.setClickedPiece(null);
    board.enPassant = null; // Reset en passant target square after a move
    board.nextTurn();
  }

  /**
   * Places the piece at the specified position on the board and removes it from the old position.
   * @param x the target x-coordinate
   * @param y the target y-coordinate
   */
  public void placeAt(int x, int y) {
    Board board = Board.getInstance();
    if (board.getPiece(x, y) != null)
      board.removeView(board.getPiece(x, y));
    board.putPiece(null, pos.x, pos.y); // Remove from old position
    board.putPiece(this, x, y);
    pos = new Pos(x, y);
    updatePos();
  }

  protected void removeAllPoints() {
    Board board = Board.getInstance();
    for (int i = 0; i < board.getChildCount(); i++)
      if (board.getChildAt(i) instanceof Point)
        board.removeViewAt(i--);
  }
}
