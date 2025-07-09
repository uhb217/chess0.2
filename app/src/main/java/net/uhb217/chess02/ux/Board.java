package net.uhb217.chess02.ux;

import static net.uhb217.chess02.ux.utils.Color.BLACK;
import static net.uhb217.chess02.ux.utils.Color.WHITE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.RoomActivity;
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
import net.uhb217.chess02.ux.utils.StockfishApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Board extends FrameLayout {
  private static Board instance;
  private Piece[][] board = new Piece[8][8];
  private Piece clickedPiece = null;
  public Pos enPassant = null; // For en passant capture
  private Color color;
  private Color turnColor = WHITE; // Default turn color
  private int fullMoves = 1;
  public int halfMoves = 0; // Halfmove clock for fifty-move rule
  public final DatabaseReference db;
  private final int depth;

  public Board(Context ctx, Color color, @NotNull String roomId) {
    super(ctx);
    this.db = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
    this.color = color;
    this.depth = -1;
    int screenWidth = ctx.getResources().getDisplayMetrics().widthPixels; // Subtracting 4 for padding
    LayoutParams params = new LayoutParams(screenWidth, screenWidth);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    setLayoutParams(params);

    setBackground(ctx.getDrawable(color == WHITE ? R.drawable.white_board : R.drawable.black_board));
    instance = this;
    initializeBoard();
    startListeningForOpponentMoves();//TODO: move tests
  }

  /**
   * Constructor for creating a new board for playing against Stockfish.
   *
   * @param ctx
   * @param color
   * @param depth
   */
  public Board(Context ctx, Color color, int depth) {
    super(ctx);
    this.db = null;
    this.color = color;
    this.depth = depth;
    int screenWidth = ctx.getResources().getDisplayMetrics().widthPixels; // Subtracting 4 for padding
    LayoutParams params = new LayoutParams(screenWidth, screenWidth);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    setLayoutParams(params);

    setBackground(ctx.getDrawable(color == WHITE ? R.drawable.white_board : R.drawable.black_board));
    instance = this;
    initializeBoard();
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

  private boolean hasNoLegalMoves(Color color) {
    List<Piece> pieces = getPieces(color);
    for (Piece piece : pieces)
      if (piece.hasLegalMoves())
        return false;
    return true;
  }

  private boolean isSufficientMaterial() {
    int pawns = 0;
    int knights = 0;
    int bishops = 0;
    int rooks = 0;
    int queens = 0;
    for (Color color : Color.values()) {
      for (Piece[] row : board)
        for (Piece piece : row)
          if (piece != null && piece.getColor() == color)
            switch (piece.getClass().getSimpleName()) {
              case "Pawn":
                pawns++;
                break;
              case "Knight":
                knights++;
                break;
              case "Bishop":
                bishops++;
                break;
              case "Rook":
                rooks++;
                break;
              case "Queen":
                queens++;
                break;
            }
      if (!(pawns >= 1 || queens >= 1 || rooks >= 1 || knights >= 2 || bishops >= 2 || (bishops > 0 && knights > 0)))
        return false;
      pawns = 0;
      knights = 0;
      bishops = 0;
      rooks = 0;
      queens = 0;
    }
    return true;
  }

  public void nextTurn(boolean bySystem) {
    String gameOver = null;
    King opponentKing = getKing(color.opposite());
    if (opponentKing.isInCheck() && hasNoLegalMoves(color.opposite())) gameOver = "Checkmate";
    else if (hasNoLegalMoves(color.opposite())) gameOver = "Stalemate";
    else if (!isSufficientMaterial()) gameOver = "Draw";

    if (gameOver != null)
      Dialogs.showGameOverDialog(getContext(), gameOver, "", new Dialogs.GameOverCallback() {
        @Override
        public void onRematch() {

        }

        @Override
        public void onExit() {
          Intent intent = new Intent(getContext(), RoomActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          getContext().startActivity(intent);
        }
      });
    turnColor = turnColor.opposite();
    if (depth != -1 && !bySystem)
      StockfishApi.INSTANCE.playBestMove(toFEN(), depth);
  }

  public Color getTurnColor() {
    return turnColor;
  }

  public void sendMoveToFirebase(String move) {
    if (db == null && depth == -1)
      throw new IllegalStateException("Database reference is not initialized.");
    else if (db == null)
      return; // Ignore move if depth is not -1 (Playing against Stockfish)
    db.child("moves").get().addOnSuccessListener(dataSnapshot -> {
      if (dataSnapshot.exists()) {
        List<String> moves = new ArrayList<>();
        for (DataSnapshot child : dataSnapshot.getChildren())
          moves.add(child.getValue(String.class));
        moves.add(move);
        db.child("moves").setValue(moves);
      } else
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
            BoardUtils.playMove(BoardUtils.UCI2Move(lastMove));
        }
      }
    }));
  }

  public String toFEN() {
    StringBuilder fen = new StringBuilder();
    int spaceCount = 0;
    //if color is white, start from top else from bottom
    for (int y = (color == WHITE ? 0 : 7); (color == WHITE ? y < 8 : y >= 0); y += color.code) {
      for (int x = 0; x < 8; x++) {
        Piece piece = board[x][y];
        if (piece == null)
          spaceCount++;
        else {
          if (spaceCount > 0) {
            fen.append(spaceCount);
            spaceCount = 0;
          }
          fen.append(piece.getColor() == WHITE ? Character.toUpperCase(piece.charCode()) : piece.charCode());
        }
      }
      if (spaceCount > 0) {
        fen.append(spaceCount);
        spaceCount = 0;
      }
      fen.append("/");
    }
    fen.deleteCharAt(fen.length() - 1); // Remove last "/"
    fen.append(" ");
    fen.append(turnColor == WHITE ? "w" : "b");
    fen.append(" ");
    for (Color kingColor : Color.values()) {
      King king = getKing(kingColor);
      if (king != null && king.canCastle4fen(true))
        fen.append(kingColor == WHITE ? "K" : "k");
      if (king != null && king.canCastle4fen(false))
        fen.append(kingColor == WHITE ? "Q" : "q");
    }
    if (fen.charAt(fen.length() - 1) == ' ')
      fen.append("-");
    fen.append(" ");
//    if (enPassant != null)//TODO: check if en passant is still valid
//      for (Piece pawn : getPieces(turnColor).stream().filter(piece -> piece instanceof Pawn).collect(Collectors.toList())) {
//        if (((Pawn) pawn).hasEnPassantMove()) {
//          fen.append(BoardUtils.pos2Square(enPassant));
//          break;
//        }
//      }
    if (fen.charAt(fen.length() - 1) == ' ') // If no en passant target square
      fen.append("-");
    fen.append(" ");
    fen.append(halfMoves).append(" ").append(fullMoves);

    return String.valueOf(fen);
  }
}
