package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class EditPostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private EditText mPostBid;

    private Button mSubmitBtn;

    private Uri mImageUri = null;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    private static final int GALLERY_REQUEST = 1;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabaseUser;

    private FirebaseStorage mFirebaseStorage;

    private DatePicker mDateRoom;
    private TimePicker mTimeRoom;

    private String mPost_key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mPost_key = getIntent().getExtras().getString("blog_id");

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mFirebaseStorage = FirebaseStorage.getInstance();

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);

        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostDesc = (EditText) findViewById(R.id.descField);
        mPostBid = (EditText) findViewById(R.id.bidField);

        mSubmitBtn = (Button) findViewById(R.id.submitBtn);

        mDateRoom = (DatePicker) findViewById(R.id.dateRoom);
        mTimeRoom = (TimePicker) findViewById(R.id.timeRoom);

        mProgress = new ProgressDialog(this);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Blog modelAuction = dataSnapshot.child(mPost_key).getValue(Blog.class);

                Picasso.with(getApplicationContext()).load(dataSnapshot.child(mPost_key).child("image").getValue().toString()).into(mSelectImage);

                mPostTitle.setText(modelAuction.getTitle());
                mPostDesc.setText(modelAuction.getDesc());
                mPostBid.setText(String.valueOf(modelAuction.getBid()));
                Calendar waktu = Calendar.getInstance();
                waktu.setTimeInMillis(modelAuction.getWaktu());
                mDateRoom.updateDate(waktu.get(Calendar.YEAR), waktu.get(Calendar.MONTH), waktu.get(Calendar.DAY_OF_MONTH));
                if (Build.VERSION.SDK_INT >= 23 ){
                    mTimeRoom.setHour(waktu.get(Calendar.HOUR_OF_DAY));
                    mTimeRoom.setMinute(waktu.get(Calendar.MINUTE));
                }
                else{
                    mTimeRoom.setCurrentHour(waktu.get(Calendar.HOUR_OF_DAY));
                    mTimeRoom.setCurrentMinute(waktu.get(Calendar.MINUTE));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startEditPost();

            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void startEditPost() {

        mProgress.setMessage("Posting to Blog ...");
        mProgress.setCancelable(false);
        int jam = 0;
        int menit = 0;

        if (Build.VERSION.SDK_INT >= 23) {
            jam = mTimeRoom.getHour();
            menit = mTimeRoom.getMinute();
        } else {
            jam = mTimeRoom.getCurrentHour();
            menit = mTimeRoom.getCurrentMinute();
        }


        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDesc.getText().toString().trim();
        final double bid_val = Double.parseDouble(mPostBid.getText().toString().trim());
        final Calendar room_time = new GregorianCalendar(mDateRoom.getYear(), mDateRoom.getMonth(), mDateRoom.getDayOfMonth(), jam, menit);
        final long waktu = room_time.getTimeInMillis();


        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val)) {

            mProgress.show();

            final DatabaseReference newPost = mDatabase.child(mPost_key);

            if(mImageUri == null){

                mDatabaseUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        newPost.child("title").setValue(title_val);
                        newPost.child("desc").setValue(desc_val);
                        newPost.child("uid").setValue(mCurrentUser.getUid());
                        newPost.child("waktu").setValue(waktu);
                        newPost.child("bid").setValue(bid_val);
                        newPost.child("bidname").setValue(dataSnapshot.child("name").getValue());
                        newPost.child("auction_id").setValue(newPost.getKey());
                        newPost.child("biduid").setValue(mCurrentUser.getUid());
                        newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Intent blogSingleIntent = new Intent(EditPostActivity.this, BlogSingleActivity.class);
                                blogSingleIntent.putExtra("blog_id", mPost_key);
                                blogSingleIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(blogSingleIntent);

                            }

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {


                    }
                });

                mProgress.dismiss();
            }
            else {
                StorageReference filepath = mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") final
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();

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

                        mDatabaseUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                newPost.child("title").setValue(title_val);
                                newPost.child("desc").setValue(desc_val);
                                newPost.child("image").setValue(downloadUrl);
                                newPost.child("uid").setValue(mCurrentUser.getUid());
                                newPost.child("waktu").setValue(waktu);
                                newPost.child("bid").setValue(bid_val);
                                newPost.child("bidname").setValue(dataSnapshot.child("name").getValue());
                                newPost.child("auction_id").setValue(newPost.getKey());
                                newPost.child("biduid").setValue(mCurrentUser.getUid());
                                newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            finish();
                                            Intent blogSingleIntent = new Intent(EditPostActivity.this, BlogSingleActivity.class);
                                            blogSingleIntent.putExtra("blog_id", mPost_key);
                                            blogSingleIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(blogSingleIntent);

                                        }

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {


                            }
                        });

                        mProgress.dismiss();

                    }
                });
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16, 9)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                mSelectImage.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
