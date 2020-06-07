package com.andy;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<TopicNotification> topicArrayList;
    private ArrayList<DocumentLink> documentArrayList;
    private Context context;
    CoordinatorLayout coordinatorLayout;
    FirebaseAuth mAuth;
    String uID;
    FirebaseDatabase firebaseDB;
    private FirebaseAnalytics mFirebaseAnalytics;
    public String likeDocumentString = "";

    DatabaseReference reference;
    Intent intent;

    FeedsAdapter(ArrayList<TopicNotification> topicArrayList, ArrayList<DocumentLink> docList, Context context, CoordinatorLayout coordinatorLayout) {
        this.topicArrayList = topicArrayList;
        this.documentArrayList = docList;
        this.context = context;
        this.coordinatorLayout = coordinatorLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feeds_card_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        viewHolder.title.setText(topicArrayList.get(i).getTopicTitle());
        viewHolder.getDocumentTitle().setText(documentArrayList.get(i).getDocumentName());
        viewHolder.docDescription.setText(documentArrayList.get(i).getDocumentDescription());
        //viewHolder.topicsImage.setBackgroundColor(currentStrokeColor);

        mAuth = FirebaseAuth.getInstance();

        firebaseDB = FirebaseDatabase.getInstance();
        reference = firebaseDB.getReference();
        if (mAuth.getCurrentUser().getUid() != null) {
            uID = mAuth.getCurrentUser().getUid();
        } else {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
            if (acct != null) {
                uID = acct.getId();
            }
        }
//        else{
//            uID=mAuth.getCurrentUser().getUid();
//            Log.d("UID",uID);
//        }
//        Toast.makeText(context, uID, Toast.LENGTH_SHORT).show();
        Log.d("TAGS", uID);


        checkTagExists(topicArrayList.get(i).getTopicTitle(), viewHolder.subscribe, viewHolder.unsubscribe, uID);

        viewHolder.subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtons(viewHolder.subscribe, viewHolder.unsubscribe);
                subscribe_tag(topicArrayList.get(i).getTopicTitle(), uID);
            }
        });

        viewHolder.unsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtons(viewHolder.unsubscribe, viewHolder.subscribe);
                unsubscribe_tag(topicArrayList.get(i).getTopicTitle(), uID);
            }
        });


        viewHolder.docDescription.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(documentArrayList.get(i).getDocumentLink());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("URI", uri.toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                String path = uri.getPath();
                // fall back solution if pdf is uploaded to firebase storage

                if (!path.isEmpty() && path.contains("PDF") || path.contains("pdf")) {
                    intent.setDataAndType(uri, "application/pdf");
                } else {
                    intent.setClass(context.getApplicationContext(), FeedsWebViewActivity.class);
                }

                Bundle bundle = new Bundle();

                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, documentArrayList.get(i).getDocumentName());
                //bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                context.startActivity(intent);

            }
        });

        updateLikeStatus(viewHolder, i);

        viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeButtonClicked(v, i);
            }
        });


        /*
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDocuments(i);
            }
        });

         */
    }

    public void updateLikeStatus(final ViewHolder viewHolder, int i) {
        final DatabaseReference like = reference.child(ConstantsKeyNames.DATA_FIREBASE_KEY).child(topicArrayList.get(i).getTopicTitle()).child("documents").child(documentArrayList.get(i).getDocumentName());

        like.addListenerForSingleValueEvent((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final DataSnapshot dsLikeCount = dataSnapshot.child("likecount");
                final DataSnapshot dsTitle = dataSnapshot.child("title");
                final DatabaseReference profileLike = FirebaseDatabase.getInstance().getReference().child(ConstantsKeyNames.PROFILE_FIREBASE_KEY).child(uID);

                profileLike.addListenerForSingleValueEvent((new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot dsLike = dataSnapshot.child("like");
                        if (dsLike != null) {
                            if (dsLike.child(dsTitle.getValue().toString()) != null) {
                                String status = "";
                                if (dsLike.child(dsTitle.getValue().toString()).getValue() != null) {
                                    status = dsLike.child(dsTitle.getValue().toString()).getValue().toString();
                                }
                                if (!status.isEmpty()) {
                                    viewHolder.likeButton.setImageResource(R.drawable.ic_thumb_up);
                                } else {
                                    viewHolder.likeButton.setImageResource(R.drawable.ic_thumb_down);
                                }
                                if (dsLikeCount.getValue() != null) {
                                    viewHolder.likeCount.setText(dsLikeCount.getValue().toString());
                                } else {
                                    viewHolder.likeCount.setText("0");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }));
    }

    public void likeButtonClicked(final View v, int i) {

        final DatabaseReference like = FirebaseDatabase.getInstance().getReference().child(ConstantsKeyNames.DATA_FIREBASE_KEY).child(topicArrayList.get(i).getTopicTitle()).child("documents").child(documentArrayList.get(i).getDocumentName());
        like.addListenerForSingleValueEvent((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot dsLikeCount = dataSnapshot.child("likecount");
                final DataSnapshot dsTitle = dataSnapshot.child("title");

                if (dsLikeCount.getValue() == null) {
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("likecount", "1");
                    like.updateChildren(childUpdates);
                    return;
                }
                final DataSnapshot dsLikeCountUpdated = dataSnapshot.child("likecount");

                likeDocumentString = "";
                final DatabaseReference profileLike = FirebaseDatabase.getInstance().getReference().child(ConstantsKeyNames.PROFILE_FIREBASE_KEY).child(uID);

                profileLike.addListenerForSingleValueEvent((new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot dsLike = dataSnapshot.child("like");
                        if (dsLike != null) {
                            if (dsLike.child(dsTitle.getValue().toString()) != null) {
                                String status = "";
                                if (dsLike.child(dsTitle.getValue().toString()).getValue() != null) {
                                    status = dsLike.child(dsTitle.getValue().toString()).getValue().toString();
                                }
                                if (!status.isEmpty()) {
                                    likeDocumentString = dsLike.child(dsTitle.getValue().toString()).getKey();
                                }
                            }
                        }

                        //final DataSnapshot temp = dsLikeCount;
                        updateLike(profileLike, like, dsTitle, dsLikeCountUpdated, v);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }));
    }

    public void updateLike(DatabaseReference profileLike, DatabaseReference like, DataSnapshot dsTitle, DataSnapshot dsLikeCount, View v) {

        if (!likeDocumentString.isEmpty()) {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/like/" + dsTitle.getValue(), null);
            profileLike.updateChildren(childUpdates);
            profileLike.child(likeDocumentString).removeValue();
            int likeCountInteger = 0;
            if (dsLikeCount.getValue() != null) {
                likeCountInteger = Integer.parseInt(dsLikeCount.getValue().toString());
            }
            likeCountInteger = likeCountInteger - 1;
            like.child("likecount").setValue(likeCountInteger);
            ViewHolder vh = new ViewHolder(v);
            ImageButton btn = vh.likeButton;
            btn.setImageResource(R.drawable.ic_thumb_down);

            final ViewParent parent = v.getParent();
            if (parent instanceof View) {
                final View vp = (View) parent;
                ViewHolder vl = new ViewHolder(vp);
                vl.likeCount.setText(Integer.toString(likeCountInteger));
            }
        } else {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/like/" + dsTitle.getValue(), "true");
            profileLike.updateChildren(childUpdates);
            int likeCountInteger = 0;
            if (dsLikeCount.getValue() != null) {
                likeCountInteger = Integer.parseInt(dsLikeCount.getValue().toString());
            }
            likeCountInteger = likeCountInteger + 1;
            like.child("likecount").setValue(likeCountInteger);
            ViewHolder vh = new ViewHolder(v);
            ImageButton btn = vh.likeButton;
            btn.setImageResource(R.drawable.ic_thumb_up);

            final ViewParent parent = v.getParent();
            if (parent instanceof View) {
                final View vp = (View) parent;
                ViewHolder vl = new ViewHolder(vp);
                vl.likeCount.setText(Integer.toString(likeCountInteger));
            }
        }
    }

    @Override
    public int getItemCount() {
        return topicArrayList.size();
    }

    @Override
    public void onClick(View v) {


    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView docDescription, likeCount;
        private TextView documentTitle;
        Button viewDocument, subscribe, unsubscribe;
        ImageButton likeButton;
        //Button likeButton;

        ImageView topicsImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            docDescription = itemView.findViewById(R.id.docDescription);
            documentTitle = itemView.findViewById(R.id.documentTitle);
            subscribe = itemView.findViewById(R.id.subscribe);
            unsubscribe = itemView.findViewById(R.id.unsubscribe);
            topicsImage = itemView.findViewById(R.id.topicsImageView);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeCount = itemView.findViewById(R.id.likeCount);
        }

        public TextView getDocumentTitle() {
            return documentTitle;
        }

        public void setDocumentTitle(TextView documentTitle) {
            this.documentTitle = documentTitle;
        }
    }

    /*
    public void viewDocuments(int position) {
        Intent intent = new Intent(context, DocumentLinksListActivity.class);
        intent.putExtra("key", topicArrayList.get(position).getKey());
        context.startActivity(intent);
    }

    */

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

