package net.uhb217.chess02.ux;

import static net.uhb217.chess02.ux.utils.Color.BLACK;
import static net.uhb217.chess02.ux.utils.Color.WHITE;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ui.BottomGameControls;
import net.uhb217.chess02.ui.PlayerInfoView;
import net.uhb217.chess02.ux.pieces.Bishop;
import net.uhb217.chess02.ux.pieces.King;
import net.uhb217.chess02.ux.pieces.Knight;
import net.uhb217.chess02.ux.pieces.Pawn;
import net.uhb217.chess02.ux.pieces.Piece;
import net.uhb217.chess02.ux.pieces.Queen;
import net.uhb217.chess02.ux.pieces.Rook;
import net.uhb217.chess02.ux.utils.BoardUtils;
import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ui.Dialogs;
import net.uhb217.chess02.ux.utils.FirebaseUtils;
import net.uhb217.chess02.ux.utils.MoveHistory;
import net.uhb217.chess02.ux.utils.Marker;
import net.uhb217.chess02.ux.utils.Pos;
import net.uhb217.chess02.ux.utils.StockfishApi;

import java.util.ArrayList;
import java.util.List;

public class Board extends FrameLayout {
  private static Board instance;
  private Piece[][] board = new Piece[8][8];
  private Piece clickedPiece = null;
  public Pos enPassant = null; // For en passant capture
  private Color color;
  private Color turnColor = WHITE; // Default turn color
  public boolean isGameOver = false;
  public boolean isDrawOffered = false;
  private int fullMoves = 1;
  public int halfMoves = 0; // Halfmove clock for fifty-move rule
  public final DatabaseReference db;
  private final int depth;
  private final ValueEventListener resignValueListener = FirebaseUtils.valueListener(snapshot -> {
    if (snapshot.exists()) gameOverCleanup(color, "Resignation");
  });

  public Board(Context ctx, Color color, @NotNull String roomId) {
    super(ctx);
    this.db = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
    this.depth = -1;

    standardSetup(ctx, color);

    startListeningForOpponentActions();
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
    this.depth = depth;

    standardSetup(ctx, color);
  }

  private void standardSetup(Context ctx, Color color) {
    this.color = color;
    int screenWidth = ctx.getResources().getDisplayMetrics().widthPixels; // Subtracting 4 for padding
    LayoutParams params = new LayoutParams(screenWidth, screenWidth);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    setLayoutParams(params);

    setBackground(ctx.getDrawable(color == WHITE ? R.drawable.white_board : R.drawable.black_board));
    instance = this;
    initializeBoard();
    MoveHistory.INSTANCE.push(toFEN());
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
    King opponentKing = getKing(turnColor.opposite());
    if (opponentKing.isInCheck() && hasNoLegalMoves(turnColor.opposite())) gameOver = "Checkmate";
    else if (hasNoLegalMoves(turnColor.opposite())) gameOver = "Stalemate";
    else if (!isSufficientMaterial()) gameOver = "Draw";

    if (gameOver != null)
      gameOverCleanup(turnColor, gameOver);
    else {
      turnColor = turnColor.opposite();
      if (depth != -1 && !bySystem)
        StockfishApi.INSTANCE.playBestMove(toFEN(), depth, ((Activity) getContext()).findViewById(R.id.lottie));
    }
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

  private void startListeningForOpponentActions() {
    if (db == null)
      throw new IllegalStateException("Database reference is not initialized.");

    db.child("moves").addValueEventListener(FirebaseUtils.valueListener(snapshot -> {
      if (snapshot.exists()) {
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
    db.child("resign").addValueEventListener(resignValueListener);
    db.child("draw").addValueEventListener(FirebaseUtils.valueListener(snapshot -> {
      if (snapshot.exists()) {
        int value = snapshot.getValue(Integer.class);
        if (value == 1 && !isDrawOffered)
          Dialogs.INSTANCE.drawOfferDialog(getContext(), db);
        else if (value == 2) {
          Dialogs.INSTANCE.dismissWaitForDrawResponseDialog();
          gameOverCleanup(null, "");
        } else if (value == 3) {
          Dialogs.INSTANCE.dismissWaitForDrawResponseDialog();
          isDrawOffered = false;
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
      if (king != null && king.isKingsideAvailable)
        fen.append(kingColor == WHITE ? "K" : "k");
      if (king != null && king.isQueensideAvailable)
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

  public void fromFEN(String fen) {
    if (fen == null) {
      Log.d("Board", "fromFEN: FEN is null");
      return;
    }
    clearBoard();

    String[] fenParts = fen.split(" ");
//    if (fenParts.length != 6) throw new IllegalArgumentException("Invalid FEN string.");
    String[] rows = fenParts[0].split("/");

    // Based on the board's color (WHITE/BLACK), we need to adjust how we read the FEN
    int startY = (color == WHITE) ? 0 : 7;
    int increment = color.code; // Using the color's code value for increment

    int y = startY;
    for (String row : rows) {
      int x = 0;
      for (int i = 0; i < row.length() && x < 8; i++) {
        char c = row.charAt(i);
        if (Character.isDigit(c)) {
          x += Character.getNumericValue(c);
        } else {
          Color pieceColor = Character.isUpperCase(c) ? WHITE : BLACK;
          Piece piece = null;
          Pos pos = new Pos(x, y);

          switch (Character.toLowerCase(c)) {
            case 'p':
              piece = new Pawn(getContext(), pos, pieceColor);
              break;
            case 'r':
              piece = new Rook(getContext(), pos, pieceColor);
              break;
            case 'n':
              piece = new Knight(getContext(), pos, pieceColor);
              break;
            case 'b':
              piece = new Bishop(getContext(), pos, pieceColor);
              break;
            case 'q':
              piece = new Queen(getContext(), pos, pieceColor);
              break;
            case 'k':
              piece = new King(getContext(), pos, pieceColor);
              break;
          }

          if (piece != null) {
            board[x][y] = piece;
            addView(piece);
            x++;
          }
        }
      }
      y += increment;
    }

    // doesnt Set turn color because its not changing the game game state, only the view
    // Handle castling rights if present. if both option to
    King whiteKing = getKing(WHITE);
    King blackKing = getKing(BLACK);
    String castling = fenParts[2];
    blackKing.isKingsideAvailable = castling.contains("K");
    blackKing.isQueensideAvailable = castling.contains("Q");
    whiteKing.isKingsideAvailable = castling.contains("k");
    whiteKing.isQueensideAvailable = castling.contains("q");

  }

  private void clearBoard() {
    //remove all points
    for (int i = 0; i < getChildCount(); i++)
      if (getChildAt(i) instanceof Marker)
        removeViewAt(i--);
    // Remove all pieces from the board
    for (int rank = 0; rank < 8; rank++)
      for (int file = 0; file < 8; file++) {
        if (board[file][rank] != null)
          this.removeView(board[file][rank]);
        board[file][rank] = null;
      }
  }

  public void resign() {
    if (depth == -1) {
      db.child("resign").removeEventListener(resignValueListener);
      db.child("resign").setValue(true);
    }
    gameOverCleanup(color.opposite(), "Resignation");
  }

  public void offerDraw() {
    db.child("draw").setValue(1);
    isDrawOffered = true;
    Dialogs.INSTANCE.waitForDrawResponseDialog(getContext());
  }

  public void gameOverCleanup(Color winner, String reason) {
    Activity activity = (Activity) getContext();
    isGameOver = true;
    BottomGameControls.INSTANCE.disable(activity);

    if (depth == -1) {
      PlayerInfoView top = activity.findViewById(R.id.top_player_info_view);
      PlayerInfoView bottom = activity.findViewById(R.id.bottom_player_info_view);
      double gameStatus = winner == null ? 0.5 : winner == color ? 1 : 0;
      bottom.updatePlayerStats(top.getRating(), gameStatus);
    }

    Dialogs.INSTANCE.showGameOverDialog(getContext(), winner, reason);
  }

}
