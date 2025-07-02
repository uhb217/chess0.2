package net.uhb217.chess02.ux.utils;

public enum Color {
    WHITE(1),
    BLACK(-1);

    public final int code;

    Color(int code){
        this.code = code;
    }

    public Color opposite(){
        return this == WHITE ? BLACK : WHITE;
    }
}
