package com.andy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    DatabaseReference reference;
    ArrayList<TopicNotification> topicArrayList;
    RecyclerView recyclerView;
    SearchView searchView;
    CoordinatorLayout coordinatorLayout;
    SwipeRefreshLayout pullToRefresh;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        FeedsFragment.feedsFragmentCalledReset();


    }

    @Override
    public void onResume() {
        super.onResume();
        FeedsFragment.feedsFragmentCalledReset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        getActivity().setTitle("Search Topics");

        reference = FirebaseDatabase.getInstance().getReference().child(ConstantsKeyNames.DATA_FIREBASE_KEY);

        recyclerView = view.findViewById(R.id.rv);
        searchView = view.findViewById(R.id.searchView);
        coordinatorLayout = view.findViewById(R.id.coordinator);
        pullToRefresh = view.findViewById(R.id.pullToRefresh);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(reference); // your code
                pullToRefresh.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getData(reference);
    }

    private void getData(DatabaseReference reference) {
        if (reference != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        topicArrayList = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            TopicNotification topic = new TopicNotification(ds.child(ConstantsKeyNames.TOPIC_DESCRIPTION_FIREBASE_KEY).getValue().toString(),
                                    ds.child(ConstantsKeyNames.TOPIC_TITLE_FIREBASE_KEY).getValue().toString(),
                                    ds.getKey());
                            topicArrayList.add(topic);
                        }
                        TopicAdapter topicAdapter = new TopicAdapter(topicArrayList, getContext(), coordinatorLayout);
                        recyclerView.setAdapter(topicAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity().getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(ConstantsKeyNames.ERROR_TAG, ConstantsKeyNames.NULL_VALUE_TAG);
        }
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    search(s);
                    return true;
                }
            });
        }
    }

    private void search(String query) {
        ArrayList<TopicNotification> resultTopicArrayList = new ArrayList<>();
        for (TopicNotification object : topicArrayList) {
            if (object.getTopicTitle().toLowerCase().contains((query.toLowerCase()))) {
                resultTopicArrayList.add(object);
            }
        }
        TopicAdapter topicAdapter = new TopicAdapter(resultTopicArrayList, getActivity(),coordinatorLayout);
        recyclerView.setAdapter(topicAdapter);
    }
}
