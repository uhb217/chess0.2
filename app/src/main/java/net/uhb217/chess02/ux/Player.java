package net.uhb217.chess02.ux;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.Contract;

import java.util.Map;

public class Player {
    public final String username;
    public int rating = 1600; // Default rating

    public Player(String username, int rating) {
        this.username = username;
        this.rating = rating;
    }

    public static Player empty(){
        return new Player("Opponent", 1600);
    }
    public static void fromFirebaseUser(FirebaseUser user, PlayerCallback callback) {
        if (user == null) {
            callback.onPlayerFetched(null);
            return;
        }

        String username = user.getDisplayName();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(username);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onPlayerFetched(null);
            }
        });
    }
    public interface PlayerCallback {
        void onPlayerFetched(Player player);
    }


}
