package org.coreocto.dev.whisper.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.coreocto.dev.whisper.Constants;
import org.coreocto.dev.whisper.R;
import org.coreocto.dev.whisper.bean.NewUser;
import org.coreocto.dev.whisper.util.DateTimeUtil;
import org.coreocto.dev.whisper.util.UiUtil;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        SignInButton btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        btnSignIn.setSize(SignInButton.SIZE_WIDE);
        btnSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    // Start sign in/sign up activity
                    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), RC_SIGN_IN);
                } else {
                    // NewUser is already signed in. Therefore, display a welcome Toast
                    UiUtil.showToast(SignInActivity.this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    checkUserRecord(true);
                }
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), RC_SIGN_IN);
        } else {
            // NewUser is already signed in. Therefore, display a welcome Toast
            UiUtil.showToast(SignInActivity.this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            checkUserRecord(true);
        }

        //checkUserRecord(false);
    }

    private String readFirebaseToken() {
        SharedPreferences pref = getSharedPreferences(Constants.APP_SHARED_PREF, MODE_PRIVATE);
        return pref.getString("firebase_token", null);
    }

    private void checkUserRecord(final boolean autoLogin) {

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser != null) {

            final String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            Query newQuery = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_USER).orderByChild("email").equalTo(userEmail);

            newQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    boolean hasData = (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0);

                    String phoneToken = readFirebaseToken();

                    if (!hasData) {
                        FirebaseDatabase.getInstance()
                                .getReference().child(Constants.FB_TABLE_USER)
                                .push()
                                .setValue(new NewUser(userEmail, DateTimeUtil.getCurrentTime(), phoneToken)
                                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startUsersActivity();
                            }
                        });
                    } else if (autoLogin) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String oldVal = (String) snapshot.child("token").getValue();
                            if (phoneToken != null && !phoneToken.equals(oldVal)) {    //prevent unnecessary updates
                                snapshot.getRef().child("token").setValue(phoneToken);
                            }
                        }
                        startUsersActivity();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled", databaseError.toException());
                }
            });
        }
    }

    private void startUsersActivity() {
        Intent intent = new Intent(this, UsersActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                checkUserRecord(true);
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }
}