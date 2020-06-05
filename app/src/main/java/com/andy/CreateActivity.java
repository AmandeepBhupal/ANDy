package com.andy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;

public class CreateActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Uri externalFile = null; //URLs that are meant for local storage
    private Uri url;
    private ImageView close;

    private TextView textTitle;
    private EditText docTitle;
    private EditText docContent;
    private Button attach,save,submit;
    private Object timestamp = ServerValue.TIMESTAMP;
    private String UID;

    String currentPhotoPath;

    //Firebase from the pdf YT video
    private StorageReference storageReference;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    GoogleSignInClient mGoogleSignInClient;

    //Progress dialog
    ProgressDialog progressDialog;

    //boolean to check if it is PDF or Gallery
    boolean decideAttachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //initializing objects
        textTitle = (TextView)findViewById(R.id.textView);
        docTitle = (EditText)findViewById(R.id.enterTitle);
        docContent = (EditText)findViewById(R.id.textDesc);
        attach = (Button)findViewById(R.id.attach);
        submit = (Button)findViewById(R.id.submit);

        //return an object of FireBase Storage
        storageReference = FirebaseStorage.getInstance().getReference(); // used for uploading files
        database = FirebaseDatabase.getInstance();// used to store the URLs of uploaded files
        reference = database.getReference();

        //spinner class
        final Spinner spinnerForAttach = (Spinner) findViewById(R.id.spinnerForAttach);
        final Spinner spinnerForTags = (Spinner) findViewById(R.id.spinnerForTags);

        spinnerForAttach.setOnItemSelectedListener(this);
        spinnerForTags.setOnItemSelectedListener(this);
        //Drop down attachments
        List<String> docType = new ArrayList<String>();
        docType.add("Select Item to Attach");
        docType.add("GALLERY");
        docType.add("CAMERA");
        docType.add("PDF");

        //Drop down tags
        List<String> tagType = new ArrayList<String>();
        tagType.add("Select Tag");
        tagType.add("topic1");
        tagType.add("topic2");
        tagType.add("topic3");
        tagType.add("topic4");
        tagType.add("topic5");

        //Creating an adapter for spinner
        ArrayAdapter<String> dataAdapterforAttach = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,docType);
        ArrayAdapter<String> dataAdapterforTag = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,tagType);

        //Drop down layout style - list view with radio button
        dataAdapterforAttach.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapterforTag.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Attaching data Adapter to spinner
        spinnerForAttach.setAdapter(dataAdapterforAttach);
        spinnerForTags.setAdapter(dataAdapterforTag);

        //Code for attaching pdf or camera
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String value = String.valueOf(spinnerForAttach.getSelectedItem());//gets which item is selected
                //if statement checks if permission is granted
                if(ContextCompat.checkSelfPermission(CreateActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                    if (value.equals("PDF")) {
                        decideAttachment = true;
                        attachpdf();
                    } else if(value.equals("GALLERY")) {
                        decideAttachment = false;
                        attachFromGallery();
                    }else{
                        askCameraPermissions();
                        attachFromCamera();
                    }
                }else //else requests permissions and invokes another method which checks the request number
                    ActivityCompat.requestPermissions(CreateActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
            }
        });

        //submit button to send the files to the firebase
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //you have to check other conditions as well,
                //for now this code will help upload the pdf and img.
                doThis();
                String valueForAttach = String.valueOf(spinnerForAttach.getSelectedItem());
                String valueForTags = String.valueOf(spinnerForTags.getSelectedItem());

                if (valueForTags != "Select Tag") {
                    if (!docTitle.getText().toString().isEmpty()
                            || !docContent.getText().toString().isEmpty()) {

                        if (externalFile != null)
                            uploadFile(externalFile, valueForTags,valueForAttach);
                        else {
                            reference.child("Topics").child(valueForTags).child("documents").child(docTitle.getText().toString()).setValue(docContent.getText().toString());
                            reference.child("Profile").child(UID).child("documents").child(docTitle.getText().toString()).setValue(docContent.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CreateActivity.this, "No attachment added", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(CreateActivity.this, "Data Succesfully added", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CreateActivity.this,MainActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    } else
                        Toast.makeText(CreateActivity.this, "Fields empty ", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(CreateActivity.this,"Select a topic",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},101);
        }else{
            openCamera();
        }
    }

    //CHeck if the UId is the same
    public void doThis() {
        //logoutBtn = findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        //Gets the UID of the current user
        if (mAuth.getCurrentUser().getUid() != null) {
            UID = mAuth.getCurrentUser().getUid();
            Log.d("UID", UID);
        } else {
            try {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                UID = acct.getId();
                Log.d("UID", UID);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }


    }

    //actual functioning of uploading the file but it will get additional code for uploading title description and image as well
    private void uploadFile(final Uri externalFile, final String valueForTags, final String valueForAttach) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Uploading File");
        progressDialog.setProgress(0);
        progressDialog.show();
        final StorageReference pdfRef;

        final String filename = System.currentTimeMillis()+"";

        if(valueForAttach == "PDF")
            pdfRef = storageReference.child("pdf/" + filename);
        else
            pdfRef = storageReference.child("image/" + filename);

        pdfRef.putFile(externalFile)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override // verify whether successfully uploaded
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        if(taskSnapshot.getMetadata()!=null){
                            if (taskSnapshot.getMetadata().getReference()!=null){
                                Task<Uri> path = taskSnapshot.getStorage().getDownloadUrl();
                                path.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imagepath = uri.toString();
                                        reference.child("Topics").child(valueForTags).child("documents").child(docTitle.getText().toString()).setValue(imagepath);
                                        reference.child("Profile").child(UID).child("documents").child(docTitle.getText().toString()).setValue(imagepath)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(CreateActivity.this,"File Successfully uploaded",Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                        Intent intent = new Intent(CreateActivity.this,MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override //checks whether failed to upload
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateActivity.this,"Not successful upload",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override //provides the progress of upload
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                //track the progress
                //dividing the bytes transferred with the total size of the file into 100
                int currentProgress =(int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);

            }
        });
    }

    // This is invoked when permission is requested
    //it compares the requestCode, to ensure the correct request is granted for the correct functionality
    // second parameter is the list of permissions we need which is mentioned in the request permission method.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==9&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(decideAttachment)
                attachpdf();
            else
                attachFromGallery();
        }else
            Toast.makeText(CreateActivity.this,"Please provide permissions",Toast.LENGTH_LONG).show();

        if(requestCode==101&&grantResults[1]==PackageManager.PERMISSION_GRANTED){
            openCamera();
        }else
            Toast.makeText(CreateActivity.this,"Please provide permissions",Toast.LENGTH_SHORT).show();
    }

    //attach image from gallery
    private void attachFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,90);
    }

    private void openCamera(){
        dispatchTakePictureIntent();
    }

    //capture image from camera
    private void attachFromCamera(){

    }

    //attach pdf
    private void attachpdf() {
        //offer user to select file from File manager
        //use intent to do so

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT); //this will be used to fetch files
        startActivityForResult(intent,86); //invokes a new method
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //checks whether user has selected a file or not
        //with resultCode : user may not select the file, we are checking if they opened and closed the file manager
        //data : checks whether any data is selected
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null||requestCode == 90&&resultCode==RESULT_OK&&data!=null) {
            externalFile = data.getData(); //returns the Uri of selected file local
        } else {
            Toast.makeText(CreateActivity.this, "Please select the file", Toast.LENGTH_LONG).show();
        }
        if(requestCode == 69 && resultCode == RESULT_OK && data != null){
            File f = new File(currentPhotoPath);
            Log.d("tag", "Absolute URL"+ Uri.fromFile(f));

            Intent mediaScanIntent = new Intent (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

        }

    }

    private File createImageFile() throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storagDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storagDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 69);
            }
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(),"Selected: " + item,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}