package net.uhb217.chess02.ux.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class FirebaseUtils {

  public static ValueEventListener ValueListener(ValueCallback callback) {
    return new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        callback.onValueChanged(snapshot);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.e("FirebaseUtils", "Database error: " + error.getMessage());
      }
    };
  }
  public interface ValueCallback{
    void onValueChanged(DataSnapshot snapshot);
  }

}
