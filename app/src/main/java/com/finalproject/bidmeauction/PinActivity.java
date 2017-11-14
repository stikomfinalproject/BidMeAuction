package com.finalproject.bidmeauction;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PinActivity extends AppCompatActivity {

    private EditText pinNumber;
    private Button pinBtn;

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        pinNumber = (EditText) findViewById(R.id.pin_number);
        pinBtn = (Button) findViewById(R.id.pin_btn);

        pinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User userModel = dataSnapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class);

                        checkPin(userModel);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    private void checkPin(User userModel) {
        if(pinNumber.getText().toString().equals(userModel.getPin())){
            Intent mainIntent = new Intent(PinActivity.this, MainActivity.class);
            mainIntent.putExtra("success_pin", "success");
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }
    }
}
