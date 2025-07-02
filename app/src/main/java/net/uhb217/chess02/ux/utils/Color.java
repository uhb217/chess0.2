package net.uhb217.chess02.ux.utils;

public enum Color {
  WHITE(1),
  BLACK(-1);

  public final int code;

  Color(int code) {
    this.code = code;
  }

  public static Color fromCode(int code) {
    return code == 1 ? WHITE : BLACK;
  }

  public Color opposite() {
    return this == WHITE ? BLACK : WHITE;
  }
}
