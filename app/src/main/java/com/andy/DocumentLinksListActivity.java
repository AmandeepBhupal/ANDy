package com.andy;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DocumentLinksListActivity extends AppCompatActivity {

    DatabaseReference reference;
    ArrayList<DocumentLink> documentLinkArrayList;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_links);

        recyclerView = findViewById(R.id.rv);
        //Navigate to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String id_str = getIntent().getStringExtra("key");
        assert id_str != null;
        reference = FirebaseDatabase.getInstance().getReference().child(ConstantsKeyNames.DATA_FIREBASE_KEY)
                .child(id_str).child(ConstantsKeyNames.DOCUMENTS_FIREBASE_KEY);
        readData(reference);
    }

    public void readData(DatabaseReference ref) {
        if (ref != null) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        documentLinkArrayList = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            DocumentLink documentLink = new DocumentLink(ds.getKey(), ds.getValue(String.class));
                            documentLinkArrayList.add(documentLink);
                        }
                        DocumentLinkAdapter documentLinkAdapter = new DocumentLinkAdapter(documentLinkArrayList, DocumentLinksListActivity.this);
                        recyclerView.setAdapter(documentLinkAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DocumentLinksListActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(ConstantsKeyNames.ERROR_TAG, ConstantsKeyNames.NULL_VALUE_TAG);
        }
    }
}
