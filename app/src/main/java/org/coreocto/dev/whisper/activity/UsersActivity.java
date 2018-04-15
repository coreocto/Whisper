package org.coreocto.dev.whisper.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
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
import org.coreocto.dev.whisper.bean.Settings;
import org.coreocto.dev.whisper.util.HapticUtil;
import org.coreocto.dev.whisper.util.UiUtil;

import java.util.Date;
import java.util.HashMap;

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

    private void startChat(String recipient) {
        Intent intent = new Intent(this, ChatActivity.class);
        if (recipient != null) {
            intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // your code.
        this.finishAffinity();
    }

    private boolean intentProcessed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mTvNoContact = (TextView) findViewById(R.id.tvNoContact);

        final Activity c = this;

        //being invoked by a received notification
        Intent intent = getIntent();
        if (intent != null) {
            final String messageFrom = intent.getStringExtra(Constants.EXTRA_FROM);
            if (messageFrom != null) {
                Query chkContactQry = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_CONTACT)
                        .orderByChild("recipient")
                        .equalTo(messageFrom);

                chkContactQry.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean found = false;

                        Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Object val = snapshot.getValue();
                            if (val instanceof String) {
                                String recipient = (String) snapshot.child("recipient").getValue();
                                Log.d(TAG, "recipient = " + recipient);
                                if (messageFrom.equalsIgnoreCase(recipient)) {
                                    found = true;
                                    break;
                                }
                            } else if (val instanceof HashMap) {
                                HashMap<String, Object> data = (HashMap<String, Object>) val;
                                String recipient = (String) data.get("recipient");
                                Log.d(TAG, "recipient = " + recipient);
                                if (messageFrom.equalsIgnoreCase(recipient)) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            FirebaseDatabase.getInstance()
                                    .getReference().child(Constants.FB_TABLE_CONTACT)
                                    .push()
                                    .setValue(new NewContact(firebaseUser.getEmail(), messageFrom, new Date().getTime()))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            startChat(messageFrom);
                                        }
                                    });
                        } else {
                            startChat(messageFrom);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, databaseError.getDetails());
                    }
                });
            }
            intent.removeExtra(Constants.EXTRA_FROM);
        }

        FloatingActionButton fabAddContact = (FloatingActionButton) findViewById(R.id.fabAddContact);
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewContact();
            }
        });
        fabAddContact.setVisibility(View.INVISIBLE);

        mLvContacts = (ListView) findViewById(R.id.lvContact);

        mLvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                doVibrate();

                final NewContact curObj = mListAdapter.getItem(position);

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                final String curUserEmail = firebaseUser.getEmail();

                Query newQuery = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_CONTACT).orderByChild("email").equalTo(curUserEmail);

                newQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        boolean hasData = dataSnapshot.getChildrenCount() > 0;

                        final String email = curObj.getRecipient();

                        if (hasData) {
                            startChat(email);
                        } else {
                            FirebaseDatabase.getInstance()
                                    .getReference().child(Constants.FB_TABLE_CONTACT)
                                    .push()
                                    .setValue(new NewContact(curUserEmail, email, new Date().getTime()))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            startChat(email);
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, databaseError.getDetails());
                    }
                });
            }
        });

        registerForContextMenu(mLvContacts);

        loadContacts(this);
    }

    private void showNewContact() {
        final Activity c = this;
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);

        final EditText etUserInputDialog = (EditText) mView.findViewById(R.id.userInputDialog);
        final TextView tvDialogTitle = (TextView) mView.findViewById(R.id.dialogTitle);

        tvDialogTitle.setText("New Contact");
        etUserInputDialog.setHint("Enter email here....");

        AlertDialog inputDialog = new AlertDialog.Builder(c)
                .setView(mView)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                        final String s = etUserInputDialog.getText().toString().trim();

                        if (s.isEmpty()) {

                        } else {

                            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                            final String curUserEmail = firebaseUser.getEmail();

                            //same email
                            if (s.equalsIgnoreCase(curUserEmail)) {
                                UiUtil.showModalError(c, "This app does not support self-talking yet!!");
                                return;
                            }
                            //

                            //check existence of user
                            Query chkUserQry = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_USER)
                                    .orderByChild("email")
                                    .equalTo(s);

                            chkUserQry.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.getChildrenCount() > 0) {

                                        Query chkContactQry = FirebaseDatabase.getInstance().getReference().child(Constants.FB_TABLE_CONTACT)
                                                .orderByChild("email")
                                                .equalTo(curUserEmail);

                                        chkContactQry.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                //check given email exists in contact list
                                                boolean found = false;

                                                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    String recipient = (String) snapshot.child("recipient").getValue();
                                                    Log.d(TAG, "recipient = " + recipient);
                                                    if (s.equalsIgnoreCase(recipient)) {
                                                        found = true;
                                                        break;
                                                    }
//                                                            NewContact curContact = snapshot.getValue(NewContact.class);
//                                                            if (curContact.getEmail()!=null && curContact.getEmail().equalsIgnoreCase(curUserEmail) && curContact.getRecipient()!=null && curContact.getRecipient().equalsIgnoreCase(s)){
//                                                                found = true;
//                                                                break;
//                                                            }
                                                }

                                                if (found) {
                                                    UiUtil.showModalError(c, "User: " + s + " already exists in your contact!!");
                                                    return;
                                                }

                                                FirebaseDatabase.getInstance()
                                                        .getReference().child(Constants.FB_TABLE_CONTACT)
                                                        .push()
                                                        .setValue(new NewContact(firebaseUser.getEmail(), etUserInputDialog.getText().toString(), new Date().getTime()))
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                startChat(s);
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.e(TAG, databaseError.getDetails());
                                            }
                                        });


                                    } else {
                                        UiUtil.showModalError(c, "User: " + s + " does not exists. Please check again!!");
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.getDetails());
                                }
                            });
                        }


                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        }).create();

        inputDialog.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
//        if (v.getId() == mLvContacts.getId()) {
//            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//
//            final NewContact curObj = mListAdapter.getItem(info.position);
//
//            menu.setHeaderTitle(curObj.getRecipient());
//
//            MenuItem menuItem = menu.add(Menu.NONE, 0, 0, "Delete");
//        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
//        Log.d(TAG, "item" + menuItem.getItemId() + " is selected");
//        if (menuItem.getItemId() == 0) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Confirm Delete")
//                    .setMessage("Are you sure to delete this contact?")
//                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            AuthUI.getInstance()
//                                    .signOut(UsersActivity.this)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            // user is now signed out
//                                            finish();
//                                        }
//                                    });
//                        }
//                    })
//                    .setNegativeButton("No", null)
//                    .show();
//        }
        return super.onContextItemSelected(menuItem);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    private void doVibrate() {
        if (Settings.getInstance(this).isVibrateOnClickEnabled()) {
            HapticUtil.vibrate(this, 100);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        doVibrate();

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_sign_out:
                new AlertDialog.Builder(this)
                        .setTitle("Confirm sign out")
                        .setMessage("Are you sure to sign out of the app?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AuthUI.getInstance()
                                        .signOut(UsersActivity.this)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // user is now signed out
                                                finish();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            case R.id.menu_new_contact:
                showNewContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}