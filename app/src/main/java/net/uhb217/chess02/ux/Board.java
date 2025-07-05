package net.uhb217.chess02.ux;

import static net.uhb217.chess02.ux.utils.Color.WHITE;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.pieces.Bishop;
import net.uhb217.chess02.ux.pieces.King;
import net.uhb217.chess02.ux.pieces.Knight;
import net.uhb217.chess02.ux.pieces.Pawn;
import net.uhb217.chess02.ux.pieces.Piece;
import net.uhb217.chess02.ux.pieces.Queen;
import net.uhb217.chess02.ux.pieces.Rook;
import net.uhb217.chess02.ux.utils.BoardUtils;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.Dialogs;
import net.uhb217.chess02.ux.utils.FirebaseUtils;
import net.uhb217.chess02.ux.utils.Pos;

import java.util.ArrayList;
import java.util.List;

public class Board extends FrameLayout {
  private static Board instance;
  private Piece[][] board = new Piece[8][8];
  private Piece clickedPiece = null;
  public Pos enPassant = null; // For en passant capture
  private Color color;
  private Color turnColor = WHITE; // Default turn color
  public final DatabaseReference db;

  public Board(Context ctx, Color color,@NotNull String roomId) {
    super(ctx);
    this.db = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
    this.color = color;
    int screenWidth = ctx.getResources().getDisplayMetrics().widthPixels; // Subtracting 4 for padding
    LayoutParams params = new LayoutParams(screenWidth, screenWidth);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    setLayoutParams(params);

    setBackground(ctx.getDrawable(color == WHITE ? R.drawable.white_board : R.drawable.black_board));
    instance = this;
    initializeBoard();
    startListeningForOpponentMoves();
  }

  private void initializeBoard() {
    board = new Piece[8][8];
    // Place pawns
    for (int i = 0; i < 8; i++) {
      board[i][6] = new Pawn(getContext(), new Pos(i, 6), color);
      board[i][1] = new Pawn(getContext(), new Pos(i, 1), color.opposite());
    }

    // Place back rows
    placeBackRow(7, color);
    placeBackRow(0, color.opposite());

    for (int i = 0; i < 8; i++)
      for (int j = 0; j < 8; j++)
        if (board[i][j] != null)
          this.addView(board[i][j]);
  }

  private void placeBackRow(int row, Color color) {
    board[0][row] = new Rook(getContext(), new Pos(0, row), color);
    board[1][row] = new Knight(getContext(), new Pos(1, row), color);
    board[2][row] = new Bishop(getContext(), new Pos(2, row), color);

    // Keep queen on her own color (D file), king on E
    if (this.color == WHITE) {
      board[3][row] = new Queen(getContext(), new Pos(3, row), color);
      board[4][row] = new King(getContext(), new Pos(4, row), color);
    } else {
      board[3][row] = new King(getContext(), new Pos(3, row), color);
      board[4][row] = new Queen(getContext(), new Pos(4, row), color);
    }

    board[5][row] = new Bishop(getContext(), new Pos(5, row), color);
    board[6][row] = new Knight(getContext(), new Pos(6, row), color);
    board[7][row] = new Rook(getContext(), new Pos(7, row), color);
  }

  public void movePieceInTheArray(int x1, int y1, int x2, int y2) {
    board[x2][y2] = board[x1][y1];
    board[x1][y1] = null;
  }

  public void putPiece(Piece piece, int x, int y) {
    board[x][y] = piece;
  }

  public static Board getInstance() {
    return instance;
  }

  public Piece getPiece(Pos pos) {
    return board[pos.x][pos.y];
  }

  public Piece getPiece(int x, int y) {
    return board[x][y];
  }

  public King getKing(Color color) {
    for (Piece[] row : board)
      for (Piece piece : row)
        if (piece instanceof King && piece.getColor() == color)
          return (King) piece;
    return null; // Should never happen if the board is initialized correctly
  }

  public List<Piece> getPieces(Color color) {
    List<Piece> pieces = new ArrayList<>();
    for (Piece[] row : board)
      for (Piece piece : row)
        if (piece != null && piece.getColor() == color)
          pieces.add(piece);
    return pieces;
  }

  private boolean isStaleMate() {
    for (Piece piece : getPieces(turnColor))
      if (!piece.getLegalMoves().isEmpty())
        return false; // If any piece has legal moves, return true
    return true; // No pieces with legal moves
  }

  public Color getColor() {
    return this.color;
  }

  public Piece[][] getBoard() {
    return board;
  }

  public Piece[][] getBoardCopy() {
    Piece[][] copy = new Piece[8][8];
    for (int i = 0; i < 8; i++)
      for (int j = 0; j < 8; j++)
        if (board[i][j] != null)
          copy[i][j] = board[i][j];
    return copy;
  }

  public Piece getClickedPiece() {
    return clickedPiece;
  }

  public void setClickedPiece(Piece clickedPiece) {
    this.clickedPiece = clickedPiece;
  }

  public void nextTurn() {
    turnColor = turnColor.opposite();
    //check if the game is over
    String gameOver = null;
    King enemyKing = getKing(turnColor);
    if (enemyKing.isInCheck() && enemyKing.getLegalMoves().isEmpty())
      gameOver = "Checkmate!";
    else if (isStaleMate())
      gameOver = "Stalemate!";
    if (gameOver != null) {
      Dialogs.showGameOverDialog(getContext(), turnColor.opposite().name(), gameOver, null);
    }

  }

  public Color getTurnColor() {
    return turnColor;
  }
  public void sendMoveToFirebase(String move){
    if (db == null)
      throw new IllegalStateException("Database reference is not initialized.");

    db.child("moves").get().addOnSuccessListener(dataSnapshot -> {
      if (dataSnapshot.exists()){
        List<String> moves = new ArrayList<>();
        for (DataSnapshot child : dataSnapshot.getChildren())
          moves.add(child.getValue(String.class));
        moves.add(move);
        db.child("moves").setValue(moves);
      }else
        db.child("moves").setValue(List.of(move));
    });
  }
  private void startListeningForOpponentMoves() {
    if (db == null)
      throw new IllegalStateException("Database reference is not initialized.");

    db.child("moves").addValueEventListener(FirebaseUtils.ValueListener(snapshot -> {
      if (snapshot.exists() && turnColor == color.opposite()) {
        List<String> moves = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren())
          moves.add(child.getValue(String.class));
        if (!moves.isEmpty()) {
          String lastMove = moves.get(moves.size() - 1);
          if (!lastMove.isEmpty())
            BoardUtils.playMove(BoardUtils.stringFormat2Move(lastMove));
        }
      }
    }));
  }
}
