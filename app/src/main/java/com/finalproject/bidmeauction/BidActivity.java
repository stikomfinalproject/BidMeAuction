package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.StringTokenizer;

public class BidActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseTime;

    private String mPost_key = null;

    private TextView mBidUsername;
    private TextView mBidBid;
    private EditText mBidTeks;
    private Button mBidBtn;

    private Button mBidQuick;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    private int bid_total = 0;
    private int bid_plus = 0;
    private String bid_name = "";

    DecimalFormatSymbols symbols;
    DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid);

        symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        decimalFormat = new DecimalFormat("Rp #,###", symbols);

        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mPost_key = getIntent().getExtras().getString("blog_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");

        mDatabase.keepSynced(true);

        mBidTeks = (EditText) findViewById(R.id.bid_teks);
        mBidBid = (TextView) findViewById(R.id.bid_bid);
        mBidUsername = (TextView) findViewById(R.id.bid_username);
        mBidBtn = (Button) findViewById(R.id.bid_btn);
        mBidQuick = (Button) findViewById(R.id.bid_quick);

        mDatabaseUser.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                bid_name = dataSnapshot.child("name").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Blog model = dataSnapshot.getValue(Blog.class);
                checkHighestBid(model);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBidQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performQuickBid();
            }

        });

        mBidTeks.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mBidBtn.performClick();
                    return true;
                }
                return false;
            }
        });

        mBidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performBid();
            }
        });

        mBidTeks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                int cursorPosition = mBidTeks.getSelectionEnd();
                String originalStr = mBidTeks.getText().toString();

                //To restrict only two digits after decimal place
                mBidTeks.setFilters(new InputFilter[]{new MoneyValueFilter(2)});

                try {
                    mBidTeks.removeTextChangedListener(this);
                    String value = mBidTeks.getText().toString();

                    if (value != null && !value.equals("")) {
                        if (value.startsWith(".")) {
                            mBidTeks.setText("0.");
                        }
                        if (value.startsWith("0") && !value.startsWith("0.")) {
                            mBidTeks.setText("");
                        }
                        String str = mBidTeks.getText().toString().replaceAll(",", "");
                        if (!value.equals(""))
                            mBidTeks.setText(getDecimalFormattedString(str));

                        int diff = mBidTeks.getText().toString().length() - originalStr.length();
                        mBidTeks.setSelection(cursorPosition + diff);
                    }
                    mBidTeks.addTextChangedListener(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mBidTeks.addTextChangedListener(this);
                }
            }

        });


        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void performQuickBid() {
        bid_plus = bid_total / 20;

        DatabaseReference newPost = mDatabase.child(mPost_key);

        newPost.child("bid").setValue(bid_total + bid_plus);

        newPost.child("bidname").setValue(bid_name);

        newPost.child("biduid").setValue(mCurrentUser.getUid());

        mBidTeks.setText("");
    }

    private void performBid() {
        String bidTeks = mBidTeks.getText().toString().replace(",","");
        if (bidTeks.equals("")) {
            Toast.makeText(BidActivity.this, "Input your amount", Toast.LENGTH_SHORT).show();
        } else if (Integer.parseInt(bidTeks) <= bid_total) {
            Toast.makeText(BidActivity.this, "Your Ammount is less than the highest bid", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference newPost = mDatabase.child(mPost_key);
            newPost.child("bid").setValue(Integer.parseInt(bidTeks));
            newPost.child("bidname").setValue(bid_name);
            newPost.child("biduid").setValue(mCurrentUser.getUid());
        }

        mBidTeks.setText("");
    }

    private void checkHighestBid(Blog model) {
        bid_total = model.getBid();

        int bidBid = model.getBid();

        String rupiahFormat = decimalFormat.format(bidBid);
        mBidBid.setText(rupiahFormat);

        mBidUsername.setText(model.getBidname());

        if (model.getBiduid().equals(mCurrentUser.getUid())) {
            mBidTeks.setEnabled(false);
            mBidBtn.setEnabled(false);
            mBidQuick.setEnabled(false);
        } else {
            mBidTeks.setEnabled(true);
            mBidBtn.setEnabled(true);
            mBidQuick.setEnabled(true);
        }

        bid_plus = bid_total / 20;

        mBidQuick.setText("QUICK BID - " + String.valueOf(bid_total + bid_plus));
    }


    public static String getDecimalFormattedString(String value) {
        if (value != null && !value.equalsIgnoreCase("")) {
            StringTokenizer lst = new StringTokenizer(value, ".");
            String str1 = value;
            String str2 = "";
            if (lst.countTokens() > 1) {
                str1 = lst.nextToken();
                str2 = lst.nextToken();
            }
            String str3 = "";
            int i = 0;
            int j = -1 + str1.length();
            if (str1.charAt(-1 + str1.length()) == '.') {
                j--;
                str3 = ".";
            }
            for (int k = j; ; k--) {
                if (k < 0) {
                    if (str2.length() > 0)
                        str3 = str3 + "." + str2;
                    return str3;
                }
                if (i == 3) {
                    str3 = "," + str3;
                    i = 0;
                }
                str3 = str1.charAt(k) + str3;
                i++;
            }
        }
        return "";
    }

    class MoneyValueFilter extends DigitsKeyListener {
        private int digits;

        public MoneyValueFilter(int i) {
            super(false, true);
            digits = i;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence out = super.filter(source, start, end, dest, dstart, dend);

            // if changed, replace the source
            if (out != null) {
                source = out;
                start = 0;
                end = out.length();
            }

            int len = end - start;

            // if deleting, source is empty
            // and deleting can't break anything
            if (len == 0) {
                return source;
            }

            int dlen = dest.length();

            // Find the position of the decimal .
            for (int i = 0; i < dstart; i++) {
                if (dest.charAt(i) == '.') {
                    // being here means, that a number has
                    // been inserted after the dot
                    // check if the amount of digits is right
                    return getDecimalFormattedString((dlen - (i + 1) + len > digits) ? "" : String.valueOf(new SpannableStringBuilder(source, start, end)));
                }
            }

            for (int i = start; i < end; ++i) {
                if (source.charAt(i) == '.') {
                    // being here means, dot has been inserted
                    // check if the amount of digits is right
                    if ((dlen - dend) + (end - (i + 1)) > digits)
                        return "";
                    else
                        break; // return new SpannableStringBuilder(source,
                    // start, end);
                }
            }

            // if the dot is after the inserted part,
            // nothing can break
            return getDecimalFormattedString(String.valueOf(new SpannableStringBuilder(source, start, end)));
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}


