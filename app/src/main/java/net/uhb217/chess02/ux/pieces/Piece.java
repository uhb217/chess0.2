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

import java.util.List;

public abstract class Piece extends AppCompatImageView {
    protected Pos pos;
    protected Color color;
    protected final int SIZE;
    public Piece(@NonNull Context ctx, Pos pos, Color color) {
        super(ctx);
        this.pos = pos;
        this.color = color;
        this.SIZE = ctx.getResources().getDisplayMetrics().widthPixels / 8;
        this.draw();
        setOnClickListener(this::onClick);
        setImageResource(resId());
    }
    protected abstract int resId();
    public abstract List<Pos> getLegalMoves();
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
    private void draw() {
        setX(pos.x * SIZE);
        setY(pos.y * SIZE);
        setLayoutParams(new FrameLayout.LayoutParams(SIZE, SIZE));
    }
    public void move(Context ctx, Pos pos){
        move(ctx, pos.x, pos.y);
    }
    public void move(Context ctx, int x, int y){
        Board board = Board.getInstance();
        if (board.getPiece(x, y) != null)
            ((FrameLayout) getParent()).removeView(board.getPiece(x, y));
        board.movePieceInTheArray(pos.x, pos.y, x, y);
        pos = new Pos(x, y);
        draw();
        removeAllPoints();
        board.setClickedPiece(null);
    }
    private void removeAllPoints(){
        Board board = Board.getInstance();
        for (int i = 0; i < board.getChildCount(); i++)
            if (board.getChildAt(i) instanceof Point)
                board.removeViewAt(i--);
    }
}
