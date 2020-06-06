package com.andy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Picture;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Testing GIT Shashi


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    DatabaseReference reference;
    FirebaseAuth mAuth;
    String uID;
    //ArrayList<String> topicArrayList;
    Vector<String> topicsVectorFromTags = new Vector<>();
    Vector<String> topicsVectorFromTopics = new Vector<>();
    ArrayList<String> topicArrayList = new ArrayList<>();
    Vector<DocumentLink> inputTopicsDocuments = new Vector<>();
    Vector<Topics> inputTopics = new Vector<>();
    CoordinatorLayout coordinatorLayout;
    SwipeRefreshLayout pullToRefresh;
    RecyclerView recyclerView;
    Iterable<DataSnapshot> allTags;
    FloatingActionButton fab;


    public FeedsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedsFragment newInstance(String param1, String param2) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feeds, container, false);
        getActivity().setTitle("Feeds");

        reference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());

        if (acct != null) {
            uID = acct.getId();
            Log.d("UID", uID);
        } else {
            uID = mAuth.getUid();
            Log.d("UID", uID);
        }


        // get the reference of RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getData(reference);

        coordinatorLayout = view.findViewById(R.id.coordinatorFeed);
        pullToRefresh = view.findViewById(R.id.pullToRefreshFeed);


        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(reference); // your code
                pullToRefresh.setRefreshing(false);
            }
        });

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getContext(), CreateActivity.class);
                startActivity(i);

            }
        });

        return view;
    }


    private void getData(DatabaseReference reference) {
        if (reference != null) {

            reference.child("Profile").child(uID).child("tags")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            allTags = dataSnapshot.getChildren();
                            Log.d("Debug", allTags.toString());
                            topicsVectorFromTags = new Vector<>();
                            for (DataSnapshot tag : allTags) {
                                String topic = tag.getValue().toString();
                                topicsVectorFromTags.add(topic);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


            reference.child("Topics").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    topicsVectorFromTopics.clear();
                    inputTopicsDocuments.clear();
                    allTags = dataSnapshot.getChildren();
                    Log.d("Debug", allTags.toString());
                    topicsVectorFromTopics = new Vector<>();
                    Vector<TopicNotification> topicsVector = new Vector<>();
                    for (DataSnapshot t1 : allTags) {
                        String topic = t1.getValue().toString();

                        topicsVectorFromTopics.add(topic); // for debugging only and future use
                        String id = t1.child("topicid").getValue().toString();
                        String topicDesc = t1.child("topicdesc").getValue().toString();

                        //Just for debugging
                        if (id.compareTo("Sensors") != 0) {
                            continue;
                        }
                        //Just for debugging end

                        Log.i("TOPICID", id);
                        Log.i("TAGS", topicsVectorFromTags.toString());
                        Iterator docChildren = t1.child("documents").getChildren().iterator();

                        while (docChildren.hasNext() && !id.isEmpty() && topicsVectorFromTags.contains(id)) {
                            DataSnapshot childIter = ((DataSnapshot) docChildren.next());

                            String documentKey = childIter.child("url").getValue().toString();
                            String documentTitle = childIter.child("title").getValue().toString();
                            String documentURL = childIter.child("url").getValue().toString();
                            String documentDesc = childIter.child("desc").getValue().toString();
                            String documentTimestamp = childIter.child("timestamp").getValue().toString();


                            if (!documentKey.isEmpty() && !documentDesc.isEmpty()) {
                                DocumentLink documentLink = new DocumentLink(documentTitle, documentURL, documentDesc, documentTimestamp);
                                inputTopicsDocuments.add(documentLink);

                                TopicNotification v1 = new TopicNotification(topicDesc, id, documentKey);
                                topicsVector.add(v1);

                                Log.i("DOCUMENT_KEY", documentKey);
                                Log.i("DOCUMENT_VALUE", documentTitle);
                            }
                        }
                    }

                    if (!topicsVector.isEmpty()) {
                        ArrayList<TopicNotification> topicArrayList = new ArrayList<TopicNotification>(topicsVector);
                        ArrayList<DocumentLink> arrayList = new ArrayList<DocumentLink>(inputTopicsDocuments);
                        FeedsAdapter feedsAdapter = new FeedsAdapter(topicArrayList, arrayList, getContext(), coordinatorLayout);
                        recyclerView.setAdapter(feedsAdapter);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {
            Log.e(ConstantsKeyNames.ERROR_TAG, ConstantsKeyNames.NULL_VALUE_TAG);
        }

    }

}
