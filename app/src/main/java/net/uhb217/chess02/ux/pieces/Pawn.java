package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Board;
import net.uhb217.chess02.ux.Color;
import net.uhb217.chess02.ux.Pos;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece{
    public Pawn(@NonNull Context ctx, Pos pos, Color color) {
        super(ctx, pos, color);
    }

    @Override
    protected int resId() {
        return color == Color.WHITE ? R.drawable.wp : R.drawable.bp;
    }

    @Override
    public List<Pos> getLegalMoves() {
        Board board = Board.getInstance();
        int d = -color.code * board.getColor().code;//direction
        List<Pos> legalMoves = new ArrayList<>();
        if (board.getPiece(pos.x, pos.y + d) == null)
            legalMoves.add(new Pos(pos.x, pos.y + d));
        if (pos.y == (color == board.getColor()? 6 : 1) && board.getPiece(pos.x, pos.y + d) == null && board.getPiece(pos.x, pos.y + 2 * d) == null)
            legalMoves.add(new Pos(pos.x, pos.y + 2 * d));


        return legalMoves;
    }
}
