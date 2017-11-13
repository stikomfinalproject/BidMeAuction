package com.finalproject.bidmeauction;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by RAIIKA on 5/1/2017.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<Blog> mItems;

    private DatabaseReference mDatabaseBlog;
    private DatabaseReference mDatabaseTime;

    private CardView mCardView;

    private Button mJoinBtn;

    private Long currentTime = (long) 0;

    public MainAdapter() {
        super();
        mItems = new ArrayList<Blog>();

        mDatabaseBlog = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");

        mDatabaseBlog.keepSynced(true);
        mDatabaseTime.keepSynced(true);

        mDatabaseTime.setValue(ServerValue.TIMESTAMP);
        mDatabaseTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentTime = Long.parseLong(dataSnapshot.getValue().toString());

                mDatabaseBlog.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        mItems.clear();
                        for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Blog blog = postSnapshot.getValue(Blog.class);

                            if(postSnapshot.child("waktu").getValue() != null) {
                                if (new Date((long) postSnapshot.child("waktu").getValue()).before(new Date(currentTime))) {
                                    mItems.add(blog);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.blog_row, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MainAdapter.ViewHolder viewHolder, int position) {

        final Blog model = mItems.get(position);

        final String post_key = model.getAuction_id();

        viewHolder.setTitle(model.getTitle());
        viewHolder.setDesc(model.getDesc());
        viewHolder.setImage(model.getImage());

        viewHolder.setUsername(model.getUsername());
        viewHolder.mJoinRoomBtn.setVisibility(View.VISIBLE);
        viewHolder.mJoinRoomBtn.setText("Join");

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_LONG).show();

                Intent singleBlogIntent = new Intent(v.getContext(), BlogSingleActivity.class);
                singleBlogIntent.putExtra("blog_id", post_key);
                v.getContext().startActivity(singleBlogIntent);

            }
        });

        /*/BELAJAR DAPETIN WAKTU DARI SERVER
        mDatabaseTime.setValue(ServerValue.TIMESTAMP);
        mDatabaseTime.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(new Date(model.getWaktu()).after(new Date((long)dataSnapshot.getValue()))){

                    viewHolder.mJoinRoomBtn.setText("Book");
                    available[0] = false;

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        viewHolder.mJoinRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if(available[0]){

                Intent roomIntent = new Intent(v.getContext(), BidActivity.class);
                roomIntent.putExtra("blog_id", post_key);
                v.getContext().startActivity(roomIntent);

                /*}
                else{


                }*/

            }
        });

    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        View mView;

        Button mJoinRoomBtn;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mJoinRoomBtn = (Button) mView.findViewById(R.id.joinRoomBtn);

        }

        public void setUsername(String username){

            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText("by : " + username);

        }

        public void setTitle(String title){/*
            String readmore = "";
            if(title.length() > 20){
                readmore = "...";
            }
            String upToNCharacters = title.substring(0, Math.min(title.length(), 20));
*/

            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
            //post_title.setText(upToNCharacters + readmore);

        }

        public void setDesc(String desc){
            /*String readmore = "";
            if(desc.length() > 20){
                readmore = "...";
            }
            String upToNCharacters = desc.substring(0, Math.min(desc.length(), 20));*/

            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
            //post_desc.setText(upToNCharacters + readmore);

        }

        public void setImage(String image){

            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(post_image.getContext().getApplicationContext()).load(image).into(post_image);

        }

    }

}
