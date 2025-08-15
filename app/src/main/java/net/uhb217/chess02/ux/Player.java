package net.uhb217.chess02.ux;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.FirebaseUtils;

import java.io.Serializable;

public class Player implements Serializable {
  public final String username;
  public int rating = 1600; // Default rating
  private Color color;
  public String base64encodedIcon;

  public Player() {
    username = null;
  }


  public Player(String username, int rating, Color color, String base64encodedIcon) {
    this.username = username;
    this.rating = rating;
    this.color = color;
    this.base64encodedIcon = base64encodedIcon;
  }
  public Player(String username, int rating, String base64encodedIcon) {
    this(username, rating, null, base64encodedIcon);
  }

  public Player(String username, int rating) {
    this(username, rating, null, null);
  }

  public static Player Stockfish(int depth) {
    int rating;
    if (depth <= 0) rating = 0;

    if (depth <= 12) {
      rating = 2350 - (12 - depth) * 100;
    } else if (depth <= 20) {
      double ratio = (depth - 12) / 8.0;
      rating = (int) (2350 + ratio * (2850 - 2350));
    } else {
      rating = 2850 + (depth - 20) * 25;
    }
    return new Player("Stockfish", rating, Color.BLACK, null);
  }

  public static void fromFirebaseUsername(String username, PlayerCallback callback) {
    DatabaseReference ref = FirebaseDatabase.getInstance()
        .getReference("users")
        .child(username);
    ref.addListenerForSingleValueEvent(FirebaseUtils.ValueListener(snapshot -> {
      if (!snapshot.exists()) {
        callback.onPlayerFetched(null);
        return;
      }

      Integer rating = snapshot.child("rating").getValue(Integer.class);

      if (rating == null) {
        callback.onPlayerFetched(null);
        return;
      }

      Player player = new Player(username, rating);
      callback.onPlayerFetched(player);
    }));
  }

  public static void fromFirebaseUser(FirebaseUser user, PlayerCallback callback) {
    if (user == null) {
      callback.onPlayerFetched(null);
      return;
    }
    fromFirebaseUsername(user.getDisplayName(), callback);
  }

  public void updateRating(int opponentRating, int gameStatus) {
    int toAdd = 16 * (gameStatus - (1 / (1 + 10 ^ (opponentRating - rating))));

    int newRating = rating + toAdd;
    FirebaseDatabase.getInstance().getReference("players").child(username)
        .child("rating").setValue(newRating);
  }

  public interface PlayerCallback {
    void onPlayerFetched(Player player);
  }

  public Player setColor(Color color) {
    this.color = color;
    return this;
  }

  public Color getColor() {
    return color;
  }

  public Bitmap getIconBitmap() {
    if (base64encodedIcon == null)
      return null;
    byte[] decodedString = Base64.decode(base64encodedIcon, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
  }
}
