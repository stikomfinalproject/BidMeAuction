package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetupPinActivity extends AppCompatActivity {

    private EditText pinNumber;
    private Button pinBtn;
    private TextView pinTitle;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    private String currentPin = "";

    private ProgressDialog mProgress;

    private boolean repeat = false; //false = first time, true = first input, if fail on second input back to false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_pin);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        pinNumber = (EditText) findViewById(R.id.pin_number);
        pinBtn = (Button) findViewById(R.id.pin_btn);
        pinTitle = (TextView) findViewById(R.id.pin_title);

        pinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(pinNumber.getText().length() >= 4 && pinNumber.getText().length() <= 8) {
                    if (repeat) {
                        if (pinNumber.getText().toString().equals(currentPin)) {

                            setupNewPin();

                        } else {
                            pinTitle.setText("Try Again");
                            repeat = false;
                            currentPin = "";
                            pinNumber.setText("");
                            Toast.makeText(SetupPinActivity.this, "Pin does not match, input your pin again", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        currentPin = pinNumber.getText().toString();
                        repeat = true;
                        pinNumber.setText("");
                        pinTitle.setText("repeat your pin");
                        Toast.makeText(SetupPinActivity.this, "Repeat your pin", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(SetupPinActivity.this, "Minimum 4 number, Maximum 8 number", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void setupNewPin() {

        mProgress.setMessage("Pin match, Requesting server to store the pin");
        mProgress.setCancelable(false);
        mProgress.show();

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("pin").setValue(currentPin).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Intent pinIntent = new Intent(SetupPinActivity.this, PinActivity.class);
                pinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(pinIntent);

                finish();

                Toast.makeText(SetupPinActivity.this, "Success setup pin", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
