package com.pgmacdesign.firebasestorageexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PGMacDesign on 12/11/2016.
 */
public class ProfilePhoto extends AppCompatActivity implements View.OnClickListener{

    //Firebase
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private File fileToDownload;

    //UI
    private ImageView profile_photo;
    private Button download_profile_picture, upload_profile_picture;
    private EditText last_name_et, first_name_et;
    private TextView upload_profile_picture_tv, download_profile_picture_tv;

    //Variables
    private String userFirstName, userLastName;

    //Tags
    private static final int TAKE_PHOTO_TAG = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_photo);

        initVariables();
        initUI();
        initFirebase();
    }


    private void initVariables(){
        this.userFirstName = this.userLastName = null;
    }

    private void initUI(){

        this.profile_photo = (ImageView) this.findViewById(R.id.profile_photo);
        this.download_profile_picture = (Button) this.findViewById(R.id.download_profile_picture);
        this.upload_profile_picture = (Button) this.findViewById(R.id.upload_profile_picture);
        this.last_name_et = (EditText) this.findViewById(R.id.last_name_et);
        this.first_name_et = (EditText) this.findViewById(R.id.first_name_et);
        this.upload_profile_picture_tv = (TextView) this.findViewById(R.id.upload_profile_picture_tv);
        this.download_profile_picture_tv = (TextView) this.findViewById(R.id.download_profile_picture_tv);

        this.download_profile_picture.setTransformationMethod(null);
        this.upload_profile_picture.setTransformationMethod(null);

        this.download_profile_picture.setOnClickListener(this);
        this.upload_profile_picture.setOnClickListener(this);
    }

    private void initFirebase(){
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReference();
        this.uploadTask = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == TAKE_PHOTO_TAG){
                try {
                    Uri uri = data.getData();
                    String newUri = MyUtilities.getPath(ProfilePhoto.this, uri);
                    File file = new File(newUri);
                    uploadPictureViaFile(file);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

        //First check if the Edit text fields are filled out. If they are not, toast and break
        String str = first_name_et.getText().toString();
        String str1 = last_name_et.getText().toString();
        if(str1.isEmpty() || str.isEmpty()){
            Toast.makeText(this, "Please enter a first and last name for your profile picture",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        this.userFirstName = str;
        this.userLastName = str1;

        switch (v.getId()){
            case R.id.download_profile_picture:
                downloadPicture();
                break;

            case R.id.upload_profile_picture:
                if (Build.VERSION.SDK_INT < 19){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(
                            intent, "Choose a Picture to Upload"), TAKE_PHOTO_TAG);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*"); //image/jpeg
                    startActivityForResult(intent, TAKE_PHOTO_TAG);
                }
                break;
        }
    }

    /**
     * For Downloading pictures
     */
    private void downloadPicture(){
        this.storageRef = storageRef.getRoot()
                .child(MyUtilities.FILE_STRUCTURE_PROFILE_PHOTOS)
                .child(userFirstName + "_" + userLastName);
        fileToDownload = null;
        try {
            fileToDownload = File.createTempFile("my_image", "jpg");
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        download_profile_picture_tv.setText("");
        if(fileToDownload != null){
            this.storageRef.getFile(fileToDownload).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            download_profile_picture_tv.setText("Success! Your photo has been downloaded");
                            profile_photo.setImageURI(Uri.fromFile(fileToDownload));
                        }
                    }
            )
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            download_profile_picture_tv.setText("Failure! Could not download photo");
                            e.printStackTrace();
                        }
                    });
        }
    }


    /**
     * Upload a picture via a file path
     * @param file File to download the image to
     */
    private void uploadPictureViaFile(File file){
        if(file == null){
            return;
        }

        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("firstName", userFirstName);
        nameMap.put("lastName", userLastName);

        upload_profile_picture_tv.setText("");

        this.storageRef = storageRef.getRoot()
                .child(MyUtilities.FILE_STRUCTURE_PROFILE_PHOTOS)
                .child(userFirstName + "_" + userLastName);
        uploadTask = storageRef.putFile(Uri.fromFile(file),
                MyUtilities.buildFileMetadata(MyUtilities.CONTENT_TYPE_IMAGE, nameMap));

        uploadTask.addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        if(downloadUrl != null){
                            upload_profile_picture_tv.setText("Success!");
                        } else {
                            upload_profile_picture_tv.setText("Failure!");
                        }
                    }
                }).addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        }
        ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                long bytesTransferred = taskSnapshot.getBytesTransferred();
                long bytesTotal = taskSnapshot.getTotalByteCount();
                long percentTransferred = (bytesTransferred / bytesTotal);
                //Print the percent Transferred into progressUpdate of a progress dialog.
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeUpload();
    }


    private void resumeUpload(){
        if(uploadTask != null){
            uploadTask.resume();
        }
    }

    private void pauseUpload(){
        if(uploadTask != null){
            uploadTask.pause();
        }
    }

    private void cancelUpload(){
        if(uploadTask != null){
            uploadTask.cancel();
        }
    }

}
