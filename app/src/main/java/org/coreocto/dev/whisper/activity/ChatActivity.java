package org.coreocto.dev.whisper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.coreocto.dev.whisper.Constants;
import org.coreocto.dev.whisper.R;
import org.coreocto.dev.whisper.bean.NewMessage;
import org.coreocto.dev.whisper.util.NewMessageSorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    private EditText mMessageArea;
    private ScrollView mScrollView;

    List<NewMessage> mMessageList = new ArrayList<>();

    private String targetRecipient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout) findViewById(R.id.layout2);
        sendButton = (ImageView) findViewById(R.id.btnSendTxt);
        //(ImageView) findViewById(R.id.btnStartSpeech);
        mMessageArea = (EditText) findViewById(R.id.messageArea);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        Intent intent = getIntent();
        this.targetRecipient = intent.getStringExtra(Constants.EXTRA_RECIPIENT);

        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageText = mMessageArea.getText().toString();

                final NewMessage newMsg = new NewMessage(fbUser.getEmail(), targetRecipient, messageText, new Date().getTime());

                FirebaseDatabase.getInstance()
                        .getReference().child(Constants.FB_TABLE_MSG)
                        .push()
                        .setValue(newMsg)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                addMessageBox(messageText, 1);
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();

        final String curUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        mMessageList.clear();

        FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_MSG).orderByChild("from").equalTo(curUserEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mMessageList.add(snapshot.getValue(NewMessage.class));
                        }

                        FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_MSG).orderByChild("to").equalTo(curUserEmail)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            mMessageList.add(snapshot.getValue(NewMessage.class));
                                        }

                                        Collections.sort(mMessageList, new NewMessageSorter());

                                        for (NewMessage m : mMessageList) {
                                            addMessageBox(m.getContent(), m.getFrom().equals(curUserEmail) ? 1 : 2);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, databaseError.getMessage());
                                    }
                                });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, databaseError.getMessage());
                    }
                });
    }

    public void addMessageBox(String message, int type) {
        TextView textView = new TextView(ChatActivity.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if (type == 1) {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        } else {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        mScrollView.fullScroll(View.FOCUS_DOWN);
    }
}