package org.coreocto.dev.whisper.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.coreocto.dev.whisper.Constants;
import org.coreocto.dev.whisper.R;
import org.coreocto.dev.whisper.bean.NewMessage;
import org.coreocto.dev.whisper.bean.Settings;
import org.coreocto.dev.whisper.util.HapticUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private static final String TAG = "ChatActivity";
    private LinearLayout mChatLayout;
    private ImageButton mSend;
    private EditText mMessageArea;
    private ScrollView mScrollView;

    List<NewMessage> mMessageList = new ArrayList<>();

    private String targetRecipient = null;

    private static final int REQ_CODE_SPEECH_INPUT = 123;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

                    String recogText = result.get(0);

                    final NewMessage newMsg = new NewMessage(fbUser.getEmail(), targetRecipient, recogText, new Date().getTime(), 0);

                    FirebaseDatabase.getInstance()
                            .getReference().child(Constants.FB_TABLE_MSG)
                            .push()
                            .setValue(newMsg);
                }
                break;
            }
        }
    }

    private void doVibrate() {
        if (Settings.getInstance(this).isVibrateOnClickEnabled()) {
            HapticUtil.vibrate(this, 100);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatLayout = (LinearLayout) findViewById(R.id.layout1);
        mSend = (ImageButton) findViewById(R.id.btnSend);
        mMessageArea = (EditText) findViewById(R.id.messageArea);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        Intent intent = getIntent();
        this.targetRecipient = intent.getStringExtra(Constants.EXTRA_RECIPIENT);

        if (targetRecipient != null) {
            this.setTitle(targetRecipient);
        }

        tts = new TextToSpeech(this, this);

        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                doVibrate();

                final String messageText = mMessageArea.getText().toString().trim();

                Settings settings = Settings.getInstance(ChatActivity.this);
                if (settings.isSttEnabled()) {
                    if (!messageText.isEmpty()) {
                        final NewMessage newMsg = new NewMessage(fbUser.getEmail(), targetRecipient, messageText, new Date().getTime(), 0);

                        FirebaseDatabase.getInstance()
                                .getReference().child(Constants.FB_TABLE_MSG)
                                .push()
                                .setValue(newMsg)
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    addMessageBox(messageText, 1);
//                                }
//                            })
                        ;
                    } else {
                        promptSpeechInput();
                    }
                } else {
                    if (!messageText.isEmpty()) {
                        final NewMessage newMsg = new NewMessage(fbUser.getEmail(), targetRecipient, messageText, new Date().getTime(), 0);

                        FirebaseDatabase.getInstance()
                                .getReference().child(Constants.FB_TABLE_MSG)
                                .push()
                                .setValue(newMsg);
                    }
                }

                mMessageArea.getText().clear();
            }
        });

        mMessageArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int len = charSequence.length();
                Settings settings = Settings.getInstance(ChatActivity.this);
                if (settings.isSttEnabled()) {
                    if (len > 0) {
                        mSend.setImageResource(R.drawable.if_mail_1055030);
                    } else {
                        mSend.setImageResource(R.drawable.ico_mic);
                    }
                } else {
                    mSend.setImageResource(R.drawable.if_mail_1055030);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Settings settings = Settings.getInstance(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, settings.getSttLang());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Settings settings = Settings.getInstance(this);
        if (settings.isSttEnabled()) {
            mSend.setImageResource(R.drawable.ico_mic);
        } else {
            mSend.setImageResource(R.drawable.if_mail_1055030);
        }

        final String curUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        final DatabaseReference tableMsgRef = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_MSG);
        tableMsgRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Object val = dataSnapshot.getValue();
                if (val instanceof NewMessage) {
                    NewMessage message = (NewMessage) val;
                    if (!mMessageList.contains(message)) {
                        mMessageList.add(message);
                        if (message.getFrom() != null && message.getFrom().equalsIgnoreCase(curUserEmail)) {
                            addMessageBox(message.getContent(), 1);
                        } else if (message.getTo() != null && message.getTo().equalsIgnoreCase(curUserEmail)) {
                            addMessageBox(message.getContent(), 2);
                        }
                    }
                    if (message.getFrom() != null && message.getFrom().equalsIgnoreCase(curUserEmail)) {
                        mMessageList.add(message);
                        addMessageBox(message.getContent(), 1);
                    } else if (message.getTo() != null && message.getTo().equalsIgnoreCase(curUserEmail)) {
                        mMessageList.add(message);
                        addMessageBox(message.getContent(), 2);
                    }
                } else if (val instanceof HashMap) {
                    //if the message being added is not from current user, it becomes a hashmap
                    HashMap<String, String> data = (HashMap<String, String>) val;
                    NewMessage message = new NewMessage();
                    message.setContent(data.get("content"));
                    message.setTo(data.get("to"));
                    message.setFrom(data.get("from"));
                    //message.setStatus(Integer.parseInt(data.get("status")));
                    if (data.containsKey("createdt")) {
                        try {
                            message.setCreateDt(Long.parseLong(data.get("createdt")));
                        } catch (NumberFormatException nfe) {
                            Log.e(TAG, "error when parsing createdt", nfe);
                        }
                    }

                    if (!mMessageList.contains(message)) {
                        mMessageList.add(message);
                        if (message.getFrom() != null && message.getFrom().equalsIgnoreCase(curUserEmail)) {
                            addMessageBox(message.getContent(), 1);
                        } else if (message.getTo() != null && message.getTo().equalsIgnoreCase(curUserEmail)) {
                            addMessageBox(message.getContent(), 2);
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();

//        final String curUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        mMessageList.clear();

//        final DatabaseReference tableMsgRef = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_MSG);

        //load old conversations
//        tableMsgRef.orderByChild("from").equalTo(curUserEmail)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            NewMessage message = snapshot.getValue(NewMessage.class);
//                            if (message.getTo()!=null && message.getTo().equalsIgnoreCase(targetRecipient)){
//                                mMessageList.add(message);
//                            }
//                        }
//
//                        tableMsgRef.orderByChild("to").equalTo(curUserEmail)
//                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                            NewMessage message = snapshot.getValue(NewMessage.class);
//                                            if (message.getFrom()!=null && message.getFrom().equalsIgnoreCase(targetRecipient)){
//                                                mMessageList.add(message);
//                                            }
//                                        }
//
//                                        Collections.sort(mMessageList, new NewMessageSorter());
//
//                                        for (NewMessage m : mMessageList) {
//                                            addMessageBox(m.getContent(), m.getFrom().equals(curUserEmail) ? 1 : 2);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//                                        Log.e(TAG, databaseError.getMessage());
//                                    }
//                                });
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.e(TAG, databaseError.getMessage());
//                    }
//                });
        //end
    }

    @Override
    public void onInit(int status) {

        final Settings settings = Settings.getInstance(this);

        if (status == TextToSpeech.SUCCESS) {
            Locale locale = null;
            if (settings.getTtsLang().equals("en-US")) {
                locale = new Locale("en", "US");
            } else if (settings.getTtsLang().equals("yue-Hant-HK")) {
                locale = new Locale("yue-Hant", "HK");
            } else if (settings.getTtsLang().equals("cmn-Hans-HK")) {
                locale = new Locale("zh", "CN");
            }
            tts.setLanguage(locale);
            tts.setOnUtteranceProgressListener(ttsUtteranceProgressListener);
        }
    }

    TextToSpeech tts = null;

    @Override
    public void onClick(View view) {
        if (view instanceof TextView) {

            doVibrate();

            TextView txtView = (TextView) view;

            final Settings settings = Settings.getInstance(this);

            if (settings.isTtsEnabled()) {
                if (tts.isSpeaking()) {
                    tts.shutdown();
                }
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString());
                tts.speak(txtView.getText().toString(), TextToSpeech.QUEUE_FLUSH, map);
            }
        }
    }

    public void addMessageBox(String message, int type) {

        Settings settings = Settings.getInstance(this);

        float fontSizeRatio = 1;

        try {
            fontSizeRatio = Float.parseFloat(settings.getUiFontSize());
        } catch (NumberFormatException e) {
            Log.e(TAG, "error when parsing font size", e);
        }

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
        textView.setOnClickListener(this);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15 * fontSizeRatio);
        mChatLayout.addView(textView);
        mScrollView.fullScroll(View.FOCUS_DOWN);
    }

    private UtteranceProgressListener ttsUtteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String s) {
            Log.d(TAG, "onStart, s:" + s);
        }

        @Override
        public void onDone(String s) {
            Log.d(TAG, "onDone, s:" + s);
        }

        @Override
        public void onError(String s) {
            Log.d(TAG, "onError, s:" + s);
        }
    };
}