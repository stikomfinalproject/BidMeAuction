package com.finalproject.bidmeauction;

import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    private DatabaseReference mDatabase;

    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseTime;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    SwipeRefreshLayout mySwipeRefreshLayout;

    //Navigation Menu
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigationView;
    //Navigation Header
    private TextView mNavTeksName;
    private TextView mNavTeksSaldo;
    private ImageView mNavProfileImage;

    public TextView noData;

    private String afterPin = null;

    MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseTime.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        checkUserExist();


        if(getIntent().getExtras() != null){
            afterPin = getIntent().getExtras().getString("success_pin");
            if(afterPin == null){
                mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("pin")){
                            Intent pinIntent = new Intent(MainActivity.this, PinActivity.class);
                            startActivity(pinIntent);
                            finish();
                        }else
                        {
                            Intent setupPinIntent = new Intent(MainActivity.this, SetupPinActivity.class);
                            startActivity(setupPinIntent);
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }else{
            mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("pin")){
                        Intent pinIntent = new Intent(MainActivity.this, PinActivity.class);
                        startActivity(pinIntent);
                        finish();
                    }else
                    {
                        Intent setupPinIntent = new Intent(MainActivity.this, SetupPinActivity.class);
                        startActivity(setupPinIntent);
                        finish();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.blog_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MainAdapter();
        mRecyclerView.setAdapter(mAdapter);

        //Swipe to REFRESH
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    mySwipeRefreshLayout.setRefreshing(true);
                    mAdapter.notifyDataSetChanged();

                    if(mAdapter.getItemCount() <1){
                        noData.setVisibility(View.VISIBLE);
                    }
                    else{
                        noData.setVisibility(View.GONE);
                    }
                    mySwipeRefreshLayout.setRefreshing(false);

                }
            }
        );

        searchView = (MaterialSearchView)findViewById(R.id.search_view);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                //If closed Search View , lstView will return default

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                searchIntent.putExtra("searchValue", s);
                startActivity(searchIntent);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }

        });

        //Navigation Menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("type")){

                    if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("type").getValue().toString().equals("admin")) {

                        Menu nav_Menu = navigationView.getMenu();
                        nav_Menu.findItem(R.id.nav_add_admin).setVisible(true);

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        noData = (TextView) findViewById(R.id.main_no_data);

        noData.setVisibility(View.GONE);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapter.notifyDataSetChanged();
                navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        navigationView.removeOnLayoutChangeListener(this);
                        /*if(mAdapter.getItemCount() <1){
                            noData.setVisibility(View.VISIBLE);
                        }
                        else{
                            noData.setVisibility(View.GONE);
                        }*/
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Access to navigation header
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        mNavTeksName = (TextView) headerView.findViewById(R.id.nav_teks_name);
        mNavTeksSaldo = (TextView) headerView.findViewById(R.id.nav_teks_saldo);
        mNavProfileImage = (ImageView) headerView.findViewById(R.id.nav_profile_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){

                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();

                }

            }
        };

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser() != null) {
                    if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                        checkUserExist();

                    } else if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("name") && dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("image")) {
                        mNavTeksName.setText(dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("name").getValue().toString());
                        Picasso.with(getApplicationContext()).load(dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("image").getValue().toString()).transform(new CircleTransform()).into(mNavProfileImage);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Exit BidMe");
        builder.setMessage("Are you sure you want to exit ?");
        builder.setPositiveButton("Ofcourse",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent exitIntent = new Intent(MainActivity.this, ExitActivity.class);
                        exitIntent.addFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                        exitIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(exitIntent);

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

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

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

    private void checkUserExist() {

        if(mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                        finish();

                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {


        if (item.getItemId() == R.id.nav_logout){

            mAuth.signOut();

        }

        if (item.getItemId() == R.id.nav_account){

            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);

        }

        if (item.getItemId() == R.id.nav_booked){

            Intent bookedIntent = new Intent(MainActivity.this, BookedActivity.class);
            startActivity(bookedIntent);

        }

        if (item.getItemId() == R.id.nav_history){

            Toast.makeText(MainActivity.this, "Not available yet", Toast.LENGTH_SHORT).show();

        }

        if (item.getItemId() == R.id.nav_upcoming){

            Intent upcomingIntent = new Intent(MainActivity.this, UpcomingActivity.class);
            startActivity(upcomingIntent);

        }

        if (item.getItemId() == R.id.nav_add_admin){

            Intent registerAdminIntent = new Intent(MainActivity.this, RegisterAdminActivity.class);
            startActivity(registerAdminIntent);

        }

        if (item.getItemId() == R.id.nav_setting){

            Toast.makeText(MainActivity.this, "Not available yet", Toast.LENGTH_SHORT).show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("type")) {
                    if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("type").getValue().toString().equals("admin")) {

                        MenuItem itemAddAuction = menu.findItem(R.id.action_add);
                        itemAddAuction.setVisible(true);

                    }
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

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        if (item.getItemId() == R.id.action_add){

            Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(postIntent);

        }

        if (item.getItemId() == R.id.action_current_time){

            Intent postIntent = new Intent(MainActivity.this, MainActivity.class);
            postIntent.addFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
            startActivity(postIntent);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

}
