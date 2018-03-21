package org.coreocto.dev.whisper.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
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
import org.coreocto.dev.whisper.bean.NewContact;

import java.util.Date;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";
    ProgressDialog pd;
    private FirebaseListAdapter<NewContact> mListAdapter;
    private ListView mLvContacts = null;
    private TextView mTvNoContact = null;

    public void loadContacts(final Activity activity) {
//        ListView mLvContacts = (ListView) findViewById(R.id.lv_contacts);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            final String curUserEmail = firebaseUser.getEmail();

            //Suppose you want to retrieve "chats" in your Firebase DB:
            Query query = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_CONTACT).orderByChild("email").equalTo(curUserEmail);

            //DO NOT use addValueEventListener/addListenerForSingleValueEvent here, it will prevent the FirebaseListAdapter from getting the list

//            query.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "ChildrenCount = " + dataSnapshot.getChildrenCount());
//                    if (dataSnapshot.getChildrenCount() > 0) {
//                        mTvNoContact.setVisibility(View.VISIBLE);
//                        mLvContacts.setVisibility(View.GONE);
//                    } else {
//                        mTvNoContact.setVisibility(View.GONE);
//                        mLvContacts.setVisibility(View.VISIBLE);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "ChildrenCount = " + dataSnapshot.getChildrenCount());
//                    if (dataSnapshot.getChildrenCount() > 0) {
//                        mTvNoContact.setVisibility(View.VISIBLE);
//                        mLvContacts.setVisibility(View.GONE);
//                    } else {
//                        mTvNoContact.setVisibility(View.GONE);
//                        mLvContacts.setVisibility(View.VISIBLE);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

            //The error said the constructor expected FirebaseListOptions - here you create them:
            FirebaseListOptions<NewContact> options = new FirebaseListOptions.Builder<NewContact>()
                    .setQuery(query, NewContact.class)
                    .setLayout(android.R.layout.simple_list_item_1)
                    .setLifecycleOwner(this)
                    .build();

            mListAdapter = new FirebaseListAdapter<NewContact>(options) {
                @Override
                protected void populateView(View v, NewContact model, int position) {
                    TextView textView = (TextView) v.findViewById(android.R.id.text1);

                    textView.setText(model.getRecipient());
                }
            };

            mLvContacts.setAdapter(mListAdapter);
        }
    }

    private void startChat() {
        Intent intent = new Intent(this, ChatActivity.class);
//                        intent.putExtra(Constants.EXTRA_RECIPIENT_EMAIL, email);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // your code.
        this.finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mTvNoContact = (TextView) findViewById(R.id.tvNoContact);

        FloatingActionButton fabAddContact = (FloatingActionButton) findViewById(R.id.fabAddContact);
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: provide a dialog to enter information for new contact
                FirebaseDatabase.getInstance()
                        .getReference().child(Constants.FB_TABLE_CONTACT)
                        .push()
                        .setValue(new NewContact(firebaseUser.getEmail(), "dummy@dummy.com", new Date().getTime()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startChat();
                            }
                        });
            }
        });

        mLvContacts = (ListView) findViewById(R.id.lvContact);

        mLvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final NewContact curObj = mListAdapter.getItem(position);

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                final String curUserEmail = firebaseUser.getEmail();

                Query newQuery = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_CONTACT).orderByChild("email").equalTo(curUserEmail);

                newQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        boolean hasData = dataSnapshot.getChildrenCount() > 0;

                        String email = curObj.getRecipient();

                        if (hasData) {
                            startChat();
                        } else {
                            FirebaseDatabase.getInstance()
                                    .getReference().child(Constants.FB_TABLE_CONTACT)
                                    .push()
                                    .setValue(new NewContact(curUserEmail, email, new Date().getTime()))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            startChat();
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        loadContacts(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mListAdapter != null) {
            mListAdapter.startListening();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mListAdapter != null) {
            mListAdapter.stopListening();
        }
    }
}