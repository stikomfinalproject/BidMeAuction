package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class BlogSingleActivity extends AppCompatActivity {

    private RecyclerView mKomenList;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseKomen;
    private DatabaseReference mDatabaseBook;
    private DatabaseReference mDatabaseBid;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseLike;
    private FirebaseStorage mFirebaseStorage;



    private String mPost_key = null;

    private ImageView mBlogSingleImage;
    private TextView mBlogSingleTitle;
    private TextView mBlogSingleDesc;

    private EditText mKomenTeks;

    private Button mKomenBtn;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mProgress;

    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mPost_key = getIntent().getExtras().getString("blog_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseKomen = FirebaseDatabase.getInstance().getReference().child("Komen");
        mDatabaseBid = FirebaseDatabase.getInstance().getReference().child("Bid");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseBook = FirebaseDatabase.getInstance().getReference().child("Book");
        //mDatabaseTests = FirebaseDatabase.getInstance().getReference().child("Ping");
        mFirebaseStorage = FirebaseStorage.getInstance();

        mDatabase.keepSynced(true);
        mDatabaseKomen.keepSynced(true);

        mKomenList = (RecyclerView) findViewById(R.id.komen_list);
        mKomenList.setLayoutManager(new LinearLayoutManager(this));

        mBlogSingleDesc = (TextView) findViewById(R.id.singleBlogDescription);
        mBlogSingleImage = (ImageView) findViewById(R.id.singleBlogView);
        mBlogSingleTitle = (TextView) findViewById(R.id.singleBlogTitle);
        mKomenTeks = (EditText) findViewById(R.id.komenTeks);
        mKomenBtn = (Button) findViewById(R.id.komenBtn);

        mKomenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                startKomenting();
                mKomenTeks.setText("");
                //mDatabaseAddKomen.child("desc").setValue();

            }
        });

        //Toast.makeText(BlogSingleActivity.this, post_key, Toast.LENGTH_LONG).show();

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();

                mBlogSingleTitle.setText(post_title);
                mBlogSingleDesc.setText(post_desc);
                Picasso.with(BlogSingleActivity.this).load(post_image).into(mBlogSingleImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true); // THIS ALSO SETS setStackFromBottom to true
// mLayoutManager.setStackFromEnd(true);
        mKomenList.setLayoutManager(mLayoutManager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void startKomenting() {


        final String desc_val = mKomenTeks.getText().toString().trim();


        mProgress.setMessage("Posting Your Komen...");
        mProgress.setCancelable(false);


        if(!TextUtils.isEmpty(desc_val)) {

            mProgress.show();

            final DatabaseReference newPost = mDatabaseKomen.child(mPost_key).push();

            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    newPost.child("username").setValue(dataSnapshot.child(mCurrentUser.getUid()).child("name").getValue().toString());
                    newPost.child("desc").setValue(desc_val);
                    newPost.child("waktu").setValue(ServerValue.TIMESTAMP);

                    /* BELAJAR READ WAKTU BUAT AUCTION
                    newPost.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newPost.child("waktu").setValue((long)dataSnapshot.child("waktu").getValue()+86400000);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });*/

                    Toast.makeText(BlogSingleActivity.this, "Success Commenting", Toast.LENGTH_LONG).show();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {



                }
            });

            mProgress.dismiss();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Komen, KomenViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Komen, KomenViewHolder>(

                Komen.class, R.layout.komen_row, KomenViewHolder.class, mDatabaseKomen.child(mPost_key).orderByChild("waktu")

        ) {
            @Override
            protected void populateViewHolder(final KomenViewHolder viewHolder, final Komen model, int position) {

                viewHolder.setDesc(model.getDesc());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setWaktu(model.getWaktu());

                /* BELAJAR DAPETIN WAKTU DARI SERVER
                mDatabaseTests.setValue(ServerValue.TIMESTAMP);
                mDatabaseTests.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(new Date(model.getWaktu()).before(new Date((long)dataSnapshot.getValue()))){
                            Log.v("LLLOOOOGGGG","KE PRINT LOH MANTAP KAN");



                        }

                        Log.v("LLLOOOOGGGG",String.valueOf(new Date((long)dataSnapshot.getValue())));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/





                final String komen_key = getRef(position).getKey();

                mDatabaseUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (model.getUsername().equals(dataSnapshot.child(mCurrentUser.getUid()).child("name").getValue().toString()))
                        {

                            viewHolder.mDeleteBtn.setVisibility(1);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                viewHolder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(BlogSingleActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle("Delete Post");
                        builder.setMessage("Are you sure you want to delete this post ?");
                        builder.setPositiveButton("Ofcourse",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        mDatabaseKomen.addValueEventListener(new ValueEventListener() {

                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                mDatabaseKomen.child(mPost_key).child(komen_key).removeValue();

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                });
                        builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                });


            }
        };

        mKomenList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class KomenViewHolder extends RecyclerView.ViewHolder{

        View mView;

        TextView mDeleteBtn;

        public KomenViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mDeleteBtn = (TextView) mView.findViewById(R.id.deleteBtn);
        }

        public void setUsername(String username){

            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);

        }

        public void setWaktu(Long waktu){

            TextView post_datetime = (TextView) mView.findViewById(R.id.post_datetime);
            post_datetime.setText(String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy / hh:mm a", new Date(waktu))));

        }

        public void setDesc(String desc){

            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.single_blog_menu, menu);

        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(mCurrentUser.getUid()).child("type").getValue().toString().equals("admin")) {
                    MenuItem itemDelete = menu.findItem(R.id.action_delete);
                    itemDelete.setVisible(true);
                    MenuItem itemEdit = menu.findItem(R.id.action_edit);
                    itemEdit.setVisible(true);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if(item.getItemId() == R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Delete Post");
            builder.setMessage("Are you sure you want to delete this post ?");
            builder.setPositiveButton("Ofcourse",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(dataSnapshot.child(mPost_key).child("image").getValue().toString());

                                    photoRef.delete();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mDatabase.child(mPost_key).removeValue();
                            mDatabaseKomen.child(mPost_key).removeValue();
                            mDatabaseBid.child(mPost_key).removeValue();
                            mDatabaseLike.child(mPost_key).removeValue();
                            mDatabaseBook.child(mPost_key).removeValue();

                            finish();
                            Intent mainIntent = new Intent(BlogSingleActivity.this, MainActivity.class);
                            startActivity(mainIntent);

                        }
                    });
            builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();


        }

        if(item.getItemId() == R.id.action_edit){
            Intent editPostIntent = new Intent(BlogSingleActivity.this, EditPostActivity.class);
            editPostIntent.putExtra("blog_id", mPost_key);
            startActivity(editPostIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
