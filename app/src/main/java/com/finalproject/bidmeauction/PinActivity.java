package com.finalproject.bidmeauction;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    private static boolean finish = false;

    private User userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        pinNumber = (EditText) findViewById(R.id.pin_number);
        pinBtn = (Button) findViewById(R.id.pin_btn);

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(User.class);
                if (userModel.getPin() == null){
                    Intent setupPinIntent = new Intent(PinActivity.this, SetupPinActivity.class);
                    startActivity(setupPinIntent);
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        pinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPin(userModel);
            }
        });

    }

    private void checkPin(User userModel) {
        if(pinNumber.getText().toString().equals(userModel.getPin())){
            Intent mainIntent = new Intent(PinActivity.this, MainActivity.class);
            mainIntent.putExtra("success_pin", "success");
            startActivity(mainIntent);
        }
    }

    public static boolean isFinish(){
        return finish;
    }
}
