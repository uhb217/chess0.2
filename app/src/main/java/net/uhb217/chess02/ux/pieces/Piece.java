package net.uhb217.chess02.ux.pieces;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.Color;
import net.uhb217.chess02.ux.Point;
import net.uhb217.chess02.ux.Pos;

import java.util.ArrayList;
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
    public List<Pos> getLegalMoves(){
        if (this.color != Board.getInstance().getTurnColor())
            return List.of(); // No legal moves if it's not this piece's turn
        return this instanceof King?  getLegalMoves(Board.getInstance().getBoard()) :
                getLegalMoves(Board.getInstance().getBoard()).stream()
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
    public void move(Pos pos){
        move(pos.x, pos.y);
    }
    public void move(int x, int y){
        Board board = Board.getInstance();
        place(x, y);
        removeAllPoints();
        board.setClickedPiece(null);
        board.enPassant = null; // Reset en passant target square after a move
        board.nextTurn();
    }
    public void place(int x, int y) {
        Board board = Board.getInstance();
        if (board.getPiece(x, y) != null)
            board.removeView(board.getPiece(x, y));
        board.putPiece(null, pos.x, pos.y); // Remove from old position
        board.putPiece(this, x, y);
        pos = new Pos(x, y);
        updatePos();
    }
    protected void removeAllPoints(){
        Board board = Board.getInstance();
        for (int i = 0; i < board.getChildCount(); i++)
            if (board.getChildAt(i) instanceof Point)
                board.removeViewAt(i--);
    }
}
