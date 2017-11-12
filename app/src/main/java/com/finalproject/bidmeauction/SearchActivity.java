package com.finalproject.bidmeauction;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SearchActivity extends AppCompatActivity {

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

    MaterialSearchView searchView;

    private String searchValue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseTime.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.blog_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SearchAdapter(getIntent().getExtras().getString("searchValue"));
        mRecyclerView.setAdapter(mAdapter);

        //Swipe to REFRESH
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        mySwipeRefreshLayout.setRefreshing(true);
                        mAdapter.notifyDataSetChanged();

                        if (mAdapter.getItemCount() < 1) {
                            noData.setVisibility(View.VISIBLE);
                        } else {
                            noData.setVisibility(View.GONE);
                        }
                        mySwipeRefreshLayout.setRefreshing(false);

                    }
                }
        );

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

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

                Toast.makeText(SearchActivity.this, "You searched " + s, Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }

        });

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("type")) {

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

                if (firebaseAuth.getCurrentUser() == null) {

                    Intent loginIntent = new Intent(SearchActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();

                }

            }
        };

    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
