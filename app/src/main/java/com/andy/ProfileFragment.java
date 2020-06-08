package com.andy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    DatabaseReference reference, topics_reference;
    TextView name, email, tagsTextMessage,tagsDocumentMessage;
    ImageView profile_picture;
    ChipGroup userTopics, userDocument;
    FirebaseAuth mAuth;
    String uID;
    FirebaseDatabase firebaseDB;
    Button logout;
    GoogleSignInClient mGoogleSignInClient;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        profile_picture = view.findViewById(R.id.profile_image);
        userTopics = view.findViewById(R.id.chipGroup);
        tagsTextMessage = view.findViewById(R.id.tagsText);
        userDocument = view.findViewById(R.id.chipGroupDocument);
        tagsDocumentMessage = view.findViewById(R.id.documentText);
        logout = view.findViewById(R.id.logout);

        // TODO: "Sid123" is hardcoded, please update it accordingly
        mAuth= FirebaseAuth.getInstance();

        firebaseDB= FirebaseDatabase.getInstance();
        reference=firebaseDB.getReference();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        if(mAuth.getCurrentUser().getUid()!=null){
            uID=mAuth.getCurrentUser().getUid();
        }
        else{
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
            if(acct!=null) {
                uID = acct.getId();
                //Log.d("UID", uID);
            }
        }

       // Toast.makeText(getActivity().getApplicationContext(), uID, Toast.LENGTH_SHORT).show();
        Log.d("TAGS",uID);

        reference = FirebaseDatabase.getInstance().getReference()
                .child(ConstantsKeyNames.PROFILE_FIREBASE_KEY)
                .child(uID);

        getActivity().setTitle("User Profile");
        topics_reference = FirebaseDatabase.getInstance().getReference().child(ConstantsKeyNames.DATA_FIREBASE_KEY);

       // Toast.makeText(getActivity().getApplicationContext(), "Profile ---", Toast.LENGTH_SHORT).show();

        getData(reference);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        return view;
    }

    public void getData(final DatabaseReference reference) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mAuth.getCurrentUser().getUid()!=null){
                    try {
                        name.setText(dataSnapshot.child(ConstantsKeyNames.NAME_FIREBASE_KEY).getValue().toString());
                        email.setText(dataSnapshot.child(ConstantsKeyNames.EMAIL_FIREBASE_KEY).getValue().toString());

                    }
                    catch (Exception e){
                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
                        if(acct!=null) {
                            uID = acct.getId();
                            //Log.d("UID", uID);
                            name.setText(acct.getDisplayName());
                            //String personGivenName = acct.getGivenName();
                            //String personFamilyName = acct.getFamilyName();
                            email.setText(acct.getEmail());
                            //String personId = acct.getId();
                            //googleProfilePicture = acct.getPhotoUrl().toString();
                            String image_url = acct.getPhotoUrl().toString();
                            Picasso.get().load(image_url).into(profile_picture);
                        }
                    }
                }



                DatabaseReference documentReference = reference.child(ConstantsKeyNames.DOCUMENTS_FIREBASE_KEY);
                setUserDocuments(documentReference);

                DatabaseReference tagReference = reference.child(ConstantsKeyNames.TAG_FIREBASE_KEY);
                setUserTags(tagReference);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ConstantsKeyNames.ERROR_TAG, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setUserTags(DatabaseReference reference) {
        final List<String> userTagsList = new ArrayList<>();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()==0){
                    tagsTextMessage.setVisibility(View.VISIBLE);
                } else {
                    tagsTextMessage.setVisibility(View.GONE);
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    userTagsList.add(ds.getValue().toString());
                    addChip(ds.getKey().toString(),userTopics);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ConstantsKeyNames.ERROR_TAG, Toast.LENGTH_SHORT).show();
            }
        });
    }
    final List<String> userDocumentList = new ArrayList<>();
    public void setUserDocuments(DatabaseReference reference) {
        //final List<String> userDocumentList = new ArrayList<>();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()==0){
                    tagsDocumentMessage.setVisibility(View.VISIBLE);
                } else {
                    tagsDocumentMessage.setVisibility(View.GONE);
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    userDocumentList.add(ds.getValue().toString());
                    addChip(ds.getKey().toString(),userDocument);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ConstantsKeyNames.ERROR_TAG, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addChip(final String pItem, final ChipGroup pChipGroup) {
        final Chip lChip = new Chip(getActivity());
        lChip.setText(pItem);
        lChip.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        lChip.setChipBackgroundColor(getResources().getColorStateList(R.color.primary_light));
        pChipGroup.addView(lChip, pChipGroup.getChildCount() - 1);
        lChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog(pItem, pChipGroup, lChip);
            }
        });
    }

    private void alertDialog(final String title, final ChipGroup chipgroup, final Chip chip) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
//        final DataSnapshot dsTitle = dataSnapshot.child("title");
        dialog.setMessage("You sure you want to delete " + title + "?");
        dialog.setPositiveButton("Delete"  ,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        getProfileTopicsData(reference.child(ConstantsKeyNames.DOCUMENTS_FIREBASE_KEY),title);
                        getTopicsData(topics_reference, title);
                        Toast.makeText(getContext(),"Deleted  " + title,Toast.LENGTH_LONG).show();
                        chipgroup.removeView(chip);
                    }
                });
        dialog.setNegativeButton("cancel"  ,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(),"cancel is clicked",Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    private void getTopicsData(DatabaseReference reference, final String title) {
        if (reference != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            DataSnapshot document_snapshot = ds.child(ConstantsKeyNames.DOCUMENTS_FIREBASE_KEY);
                            if(document_snapshot.hasChild(title)){
                               document_snapshot.child(title).getRef().removeValue();
                            }
                        }
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
    }
    private void getProfileTopicsData(DatabaseReference reference, final String title) {
        if (reference != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dataSnapshot.child(title).getRef().removeValue();
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
    }
}

