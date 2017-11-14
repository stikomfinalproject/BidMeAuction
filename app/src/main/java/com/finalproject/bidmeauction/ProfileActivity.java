package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageImage;
    private FirebaseStorage mFirebaseStorage;

    private TextView mProfileEmail;
    private TextView mProfileUsername;
    private TextView mProfilePhone;
    private TextView mProfileAddress;
    private Button mChangePasswordBtn;

    private ImageButton mProfileImage;

    private ProgressDialog mProgress;

    private Uri mImageUri = null;
    private static final int GALLERY_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileEmail = (TextView) findViewById(R.id.profile_email);
        mProfileUsername = (TextView) findViewById(R.id.profile_username);
        mProfilePhone = (TextView) findViewById(R.id.profile_phone);
        mProfileAddress = (TextView) findViewById(R.id.profile_address);
        mProfileImage = (ImageButton) findViewById(R.id.profile_image);
        mChangePasswordBtn = (Button) findViewById(R.id.change_password_btn);
        mProgress = new ProgressDialog(this);

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mFirebaseStorage = FirebaseStorage.getInstance();

        mAuth = FirebaseAuth.getInstance();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showUserProfile(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfileImage();
            }
        });

        mProfileUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });

        mProfileAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAddress();
            }
        });

        mProfilePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhone();
            }
        });

        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if(user.getProviderId().equals("google.com")){
                mChangePasswordBtn.setVisibility(View.GONE);
            }
        }

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void showUserProfile(DataSnapshot dataSnapshot) {
        mProfileEmail.setText(mAuth.getCurrentUser().getEmail());
        if(dataSnapshot.hasChild("name")){
            mProfileUsername.setText(dataSnapshot.child("name").getValue().toString());
        }
        else{
            mProfileUsername.setText("click here to set your name");
        }
        if(dataSnapshot.hasChild("image")) {
            Picasso.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).transform(new ProfileActivity.CircleTransform()).into(mProfileImage);
        }
        if(dataSnapshot.hasChild("address")) {
            mProfileAddress.setText(dataSnapshot.child("address").getValue().toString());
        }else{
            mProfileAddress.setText("click here to set your address");
        }

        if(dataSnapshot.hasChild("phone")){
            mProfilePhone.setText(dataSnapshot.child("phone").getValue().toString());
        }else{
            mProfilePhone.setText("click here to set your phone");
        }
    }

    private void changeProfileImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void changeName() {
        final EditText mTeksUser = new EditText(ProfileActivity.this);

        mTeksUser.setHint(mProfileUsername.getText());

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your new name")
                .setMessage("Additional information goes here")
                .setView(mTeksUser)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String username = mTeksUser.getText().toString();
                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("name").setValue(username);
                        Toast.makeText(ProfileActivity.this,"Success changing your name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void changeAddress() {
        final EditText mTeksUser = new EditText(ProfileActivity.this);

        mTeksUser.setHint(mProfileAddress.getText());

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your new address")
                .setMessage("Additional information goes here")
                .setView(mTeksUser)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String address = mTeksUser.getText().toString();
                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("address").setValue(address);
                        Toast.makeText(ProfileActivity.this,"Success changing your address", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void changePhone() {
        final EditText mTeksUser = new EditText(ProfileActivity.this);
        mTeksUser.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        mTeksUser.setHint(mProfileAddress.getText());

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your new phone number")
                .setMessage("Additional information goes here")
                .setView(mTeksUser)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String address = mTeksUser.getText().toString();
                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("phone").setValue(address);
                        Toast.makeText(ProfileActivity.this,"Success changing your phone number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void changePassword() {
        Context context = ProfileActivity.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText oldPassword = new EditText(context);
        oldPassword.setHint("Old Password");
        layout.addView(oldPassword);

        final EditText newPassword1 = new EditText(context);
        newPassword1.setHint("New Password");
        layout.addView(newPassword1);

        final EditText newPassword2 = new EditText(context);
        newPassword2.setHint("Repeat New Password");
        layout.addView(newPassword2);

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your old password")
                .setMessage("Additional information goes here")
                .setView(layout)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        mProgress.setMessage("checking Old Password");
                        mProgress.show();
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        AuthCredential credential = EmailAuthProvider
                                .getCredential(mAuth.getCurrentUser().getEmail(), oldPassword.getText().toString());

                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                        if (task.isSuccessful()) {

                                            if(newPassword1.getText().toString().equals(newPassword2.getText().toString())) {

                                                user.updatePassword(newPassword1.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ProfileActivity.this, "Success updating password", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(ProfileActivity.this, "Unknown Error in updating password", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            }else{
                                                Toast.makeText(ProfileActivity.this, "New password does not match", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                        }
                                        mProgress.dismiss();
                                    }
                                });


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                mProgress.setMessage("Uploading");
                mProgress.setCancelable(false);
                mProgress.show();

                final StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProfileImage.setImageURI(mImageUri);
                        final DatabaseReference current_user_db = mDatabaseUsers.child(mAuth.getCurrentUser().getUid());

                        current_user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(dataSnapshot.child("image").getValue().toString());

                                photoRef.delete();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        String downloadUri = taskSnapshot.getDownloadUrl().toString();

                        current_user_db.child("image").setValue(downloadUri);

                        mProgress.dismiss();

                    }
                });


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
