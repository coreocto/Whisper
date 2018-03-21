package org.coreocto.dev.whisper.fcm;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.coreocto.dev.whisper.Constants;

/**
 * Created by John on 3/20/2018.
 */

public class WhisperInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "WhisperInsIdService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();    //this token is generated even the user has not login yet
        Log.d(TAG, "token: " + refreshedToken);

        writeFirebaseToken(refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(refreshedToken);
    }

    private void writeFirebaseToken(String token) {
        SharedPreferences pref = getSharedPreferences(Constants.APP_SHARED_PREF, MODE_PRIVATE);
        pref.edit().putString(Constants.APP_PREF_FIREBASE_TOKEN, token).commit();
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            Query userQuery = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_USER).orderByChild("email").equalTo(fbUser.getEmail());

            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 1) {
                        Log.d(TAG, "" + dataSnapshot.getChildren().iterator().next().getValue());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
//            FirebaseDatabase.getInstance()
//                    .getReference().child(Constants.FB_TABLE_USER)
//                    .set
//                    .push()
//                    .setValue(newMsg)
        }
    }

}
