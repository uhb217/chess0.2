package net.uhb217.chess02.ux;

import static net.uhb217.chess02.ux.Color.BLACK;
import static net.uhb217.chess02.ux.Color.WHITE;

import android.content.Context;
import android.widget.FrameLayout;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.pieces.Bishop;
import net.uhb217.chess02.ux.pieces.King;
import net.uhb217.chess02.ux.pieces.Knight;
import net.uhb217.chess02.ux.pieces.Pawn;
import net.uhb217.chess02.ux.pieces.Piece;
import net.uhb217.chess02.ux.pieces.Queen;
import net.uhb217.chess02.ux.pieces.Rook;

public class Board extends FrameLayout {
    private static Board instance;
    private Piece[][] board = new Piece[8][8];
    private Piece clickedPiece = null;
    private Color color;
    public Board(Context ctx, Color color) {
        super(ctx);
        this.color = color;
        int screenWidth = ctx.getResources().getDisplayMetrics().widthPixels;
        setLayoutParams(new LayoutParams(screenWidth, screenWidth));
        setBackground(ctx.getDrawable(R.drawable.chessboard));
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
        if (color == WHITE) {
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
    public void movePieceInTheArray(int x1, int y1, int x2, int y2){
        board[x2][y2] = board[x1][y1];
        board[x1][y1] = null;
    }
    public static Board getInstance() {
        return instance;
    }
    public Piece getPiece(Pos pos){
        return board[pos.x][pos.y];
    }
    public Piece getPiece(int x, int y){
        return board[x][y];
    }

    public Color getColor() {
        return this.color;
    }
    public Piece getClickedPiece() {
        return clickedPiece;
    }

    public void setClickedPiece(Piece clickedPiece) {
        this.clickedPiece = clickedPiece;
    }
}
