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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PGMacDesign on 12/11/2016.
 */
public class UploadDownloadPictures extends AppCompatActivity implements View.OnClickListener {

    //Firebase
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private File fileToDownload;

    //UI
    private EditText picture_title_et;
    private Button upload_picture_button, download_picture_button;
    private TextView upload_picture_tv, download_picture_tv;
    private ImageView download_picture_iv;

    //Variables
    private String userChosenString;

    //Tags
    private static final int TAKE_PHOTO_TAG = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_download_pictures);

        initVariables();
        initUI();
        initFirebase();
    }

    private void initVariables(){
        this.userChosenString = null;
    }

    private void initUI(){

        this.picture_title_et = (EditText) this.findViewById(R.id.picture_title_et);
        this.upload_picture_button = (Button) this.findViewById(R.id.upload_picture_button);
        this.download_picture_button = (Button) this.findViewById(R.id.download_picture_button);
        this.upload_picture_tv = (TextView) this.findViewById(R.id.upload_picture_tv);
        this.download_picture_tv = (TextView) this.findViewById(R.id.download_picture_tv);
        this.download_picture_iv = (ImageView) this.findViewById(R.id.download_picture_iv);

        this.download_picture_button.setTransformationMethod(null);
        this.upload_picture_button.setTransformationMethod(null);

        this.download_picture_button.setOnClickListener(this);
        this.upload_picture_button.setOnClickListener(this);
    }

    /**
     * Setup firebase and get an instance
     */
    private void initFirebase(){
        this.storage = MyUtilities.getFirebaseStorageInstance();
        this.storageRef = storage.getReference();
        this.uploadTask = null;
    }


    /**
     * For Downloading pictures
     */
    private void downloadPicture(){
        this.storageRef = storageRef.getRoot()
                .child(MyUtilities.FILE_STRUCTURE_UPLOAD_DOWNLOAD_PHOTOS)
                .child(userChosenString);
        fileToDownload = null;
        try {
            fileToDownload = File.createTempFile("my_image", "jpg");
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        if(fileToDownload != null){
            this.storageRef.getFile(fileToDownload).addOnSuccessListener(
                            new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    download_picture_tv.setText("Download Successful");
                                    download_picture_iv.setImageURI(Uri.fromFile(fileToDownload));
                                }
                            }
                    ).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            download_picture_tv.setText("Download Failed");
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
        nameMap.put("user_chosen_title", userChosenString);

        this.storageRef = storageRef.getRoot()
                .child(MyUtilities.FILE_STRUCTURE_UPLOAD_DOWNLOAD_PHOTOS)
                .child(userChosenString);
        uploadTask = storageRef.putFile(Uri.fromFile(file),
                MyUtilities.buildFileMetadata(MyUtilities.CONTENT_TYPE_IMAGE, nameMap));

        uploadTask.addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                if(downloadUrl != null){
                                    upload_picture_tv.setText("Success! Photo Uploaded");
                                } else {
                                    upload_picture_tv.setText("Failure! Photo Not Uploaded");
                                }
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        }
                );
    }


    @Override
    public void onClick(View v) {

        //First check if the Edit text field is filled out. If it is not, toast and break
        String str = picture_title_et.getText().toString();
        if(str.isEmpty()){
            Toast.makeText(this, "Please enter a picture title", Toast.LENGTH_SHORT).show();
            return;
        }
        this.userChosenString = str;
        switch (v.getId()){
            case R.id.upload_picture_button:

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

            case R.id.download_picture_button:
                downloadPicture();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == this.RESULT_OK){
            if(requestCode == TAKE_PHOTO_TAG){
                try {
                    Uri uri = data.getData();
                    String newUri = MyUtilities.getPath(UploadDownloadPictures.this, uri);
                    File file = new File(newUri);
                    uploadPictureViaFile(file);
                } catch (Exception e){
                    upload_picture_tv.setText("Upload Failed!");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeUpload();
    }

    //These methods are here just for visual. This is how you would resume, pause, or stop an upload

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
