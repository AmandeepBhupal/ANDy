package com.andy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {

    private ArrayList<TopicNotification> topicArrayList;
    private Context context;
    CoordinatorLayout coordinatorLayout;
    FirebaseAuth mAuth;
    String uID;
    FirebaseDatabase firebaseDB;
    DatabaseReference reference;

    TopicAdapter(ArrayList<TopicNotification> topicArrayList, Context context, CoordinatorLayout coordinatorLayout) {
        this.topicArrayList = topicArrayList;
        this.context = context;
        this.coordinatorLayout = coordinatorLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.topics_card_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        //Random rnd = new Random();
       // int currentStrokeColor = Color.argb(90, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        viewHolder.title.setText(topicArrayList.get(i).getTopicTitle());
        viewHolder.description.setText(topicArrayList.get(i).getTopicDescription());
        //viewHolder.topicsImage.setBackgroundColor(currentStrokeColor);
        mAuth= FirebaseAuth.getInstance();

        firebaseDB= FirebaseDatabase.getInstance();
        reference=firebaseDB.getReference();
        if(mAuth.getCurrentUser().getUid()!=null){
            uID=mAuth.getCurrentUser().getUid();
        }
        else{
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
            if(acct!=null) {
                uID = acct.getId();
                //Log.d("UID", uID);
            }
        }
//        else{
//            uID=mAuth.getCurrentUser().getUid();
//            Log.d("UID",uID);
//        }
//        Toast.makeText(context, uID, Toast.LENGTH_SHORT).show();
        Log.d("TAGS",uID);


        checkTagExists(topicArrayList.get(i).getTopicTitle(), viewHolder.subscribe, viewHolder.unsubscribe,uID);

        viewHolder.subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtons(viewHolder.subscribe, viewHolder.unsubscribe);
                subscribe_tag(topicArrayList.get(i).getTopicTitle(),uID);
            }
        });

        viewHolder.unsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtons(viewHolder.unsubscribe, viewHolder.subscribe);
                unsubscribe_tag(topicArrayList.get(i).getTopicTitle(),uID);
            }
        });

        viewHolder.viewDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDocuments(i);
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDocuments(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return topicArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        Button viewDocument, subscribe, unsubscribe;
        ImageView topicsImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            viewDocument = itemView.findViewById(R.id.view);
            subscribe = itemView.findViewById(R.id.subscribe);
            unsubscribe = itemView.findViewById(R.id.unsubscribe);
            topicsImage = itemView.findViewById(R.id.topicsImageView);
        }
    }

    public void viewDocuments(int position) {
        Intent intent = new Intent(context, DocumentLinksListActivity.class);
        intent.putExtra("key", topicArrayList.get(position).getKey());
        context.startActivity(intent);
    }

    public void toggleButtons(Button x, Button y) {
        x.setVisibility(View.INVISIBLE);
        y.setVisibility(View.VISIBLE);
    }

    public void subscribe_tag(final String title, String uID) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Subscribed " + title, Snackbar.LENGTH_LONG);
        snackbar.show();
        DatabaseReference profile_tag_reference;

        profile_tag_reference = FirebaseDatabase.getInstance().getReference()
                .child(ConstantsKeyNames.PROFILE_FIREBASE_KEY)
                .child(uID)
                .child(ConstantsKeyNames.TAG_FIREBASE_KEY);

        profile_tag_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().child(title).setValue(title);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unsubscribe_tag(final String title, final String uID) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Unsubscribed " + title, Snackbar.LENGTH_LONG);
        snackbar.show();
        DatabaseReference profile_tag_reference;
        profile_tag_reference = FirebaseDatabase.getInstance().getReference()
                .child(ConstantsKeyNames.PROFILE_FIREBASE_KEY)
                .child(uID)
                .child(ConstantsKeyNames.TAG_FIREBASE_KEY).child(title);

        profile_tag_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkTagExists(final String title, final Button subscribe, final Button unsubscribe, final String uID) {
        DatabaseReference profile_tag_reference;
        profile_tag_reference = FirebaseDatabase.getInstance().getReference()
                .child(ConstantsKeyNames.PROFILE_FIREBASE_KEY)
                .child(uID)
                .child(ConstantsKeyNames.TAG_FIREBASE_KEY);

        profile_tag_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(title).exists()) {
                    subscribe.setVisibility(View.GONE);
                    unsubscribe.setVisibility(View.VISIBLE);
                } else {
                    unsubscribe.setVisibility(View.GONE);
                    subscribe.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
