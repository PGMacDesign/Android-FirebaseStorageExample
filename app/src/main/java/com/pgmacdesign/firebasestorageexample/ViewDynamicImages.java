package com.pgmacdesign.firebasestorageexample;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * Created by PGMacDesign on 12/11/2016.
 */
public class ViewDynamicImages extends AppCompatActivity {

    //Firebase
    private FirebaseStorage storage;
    private StorageReference reference1, reference2, reference3, reference4;

    //UI
    private ImageView dynamic_image_iv1, dynamic_image_iv2, dynamic_image_iv3,
            dynamic_image_iv4;

    //Vars
    File fileToDownload1, fileToDownload2, fileToDownload3, fileToDownload4;

    //Final Vars
    private static final String IMAGE_1 = "dynamic_picture_1.jpg";
    private static final String IMAGE_2 = "dynamic_picture_2.jpg";
    private static final String IMAGE_3 = "dynamic_picture_3.jpg";
    private static final String IMAGE_4 = "dynamic_picture_4.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dynamic_images);

        initVariables();
        initUI();
        initFirebase();
        loadFourImages();
    }

    private void initVariables(){
        fileToDownload1 = fileToDownload2 = fileToDownload3 = fileToDownload4 = null;
    }

    private void initUI() {
        this.dynamic_image_iv1 = (ImageView) this.findViewById(R.id.dynamic_image_iv1);
        this.dynamic_image_iv2 = (ImageView) this.findViewById(R.id.dynamic_image_iv2);
        this.dynamic_image_iv3 = (ImageView) this.findViewById(R.id.dynamic_image_iv3);
        this.dynamic_image_iv4 = (ImageView) this.findViewById(R.id.dynamic_image_iv4);
    }

    /**
     * Setup firebase and get an instance
     */
    private void initFirebase() {
        this.storage = MyUtilities.getFirebaseStorageInstance();
        this.reference1 = storage.getReference().getRoot()
                .child(MyUtilities.FILE_STRUCTURE_DYNAMIC_PHOTOS)
                .child(IMAGE_1);
        this.reference2 = storage.getReference().getRoot()
                .child(MyUtilities.FILE_STRUCTURE_DYNAMIC_PHOTOS)
                .child(IMAGE_2);
        this.reference3 = storage.getReference().getRoot()
                .child(MyUtilities.FILE_STRUCTURE_DYNAMIC_PHOTOS)
                .child(IMAGE_3);
        this.reference4 = storage.getReference().getRoot()
                .child(MyUtilities.FILE_STRUCTURE_DYNAMIC_PHOTOS)
                .child(IMAGE_4);

    }

    private void loadFourImages(){

        try {
            fileToDownload1 = File.createTempFile("my_image1", "jpg");
            fileToDownload2 = File.createTempFile("my_image2", "jpg");
            fileToDownload3 = File.createTempFile("my_image3", "jpg");
            fileToDownload4 = File.createTempFile("my_image4", "jpg");
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        //Picture 1:
        if(fileToDownload1 != null) {
            this.reference1.getFile(fileToDownload1).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            dynamic_image_iv1.setImageURI(Uri.fromFile(fileToDownload1));
                        }
                    }
            );
        }

        //Picture 2:
        if(fileToDownload2 != null) {
            this.reference2.getFile(fileToDownload2).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            dynamic_image_iv2.setImageURI(Uri.fromFile(fileToDownload2));
                        }
                    }
            );
        }

        //Picture 3:
        if(fileToDownload3 != null) {
            this.reference3.getFile(fileToDownload3).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            dynamic_image_iv3.setImageURI(Uri.fromFile(fileToDownload3));
                        }
                    }
            );
        }

        //Picture 4:
        if(fileToDownload4 != null) {
            this.reference4.getFile(fileToDownload4).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            dynamic_image_iv4.setImageURI(Uri.fromFile(fileToDownload4));
                        }
                    }
            );
        }
    }

    /**
     * Another option for setting instead of using listeners. Shorter, quicker, and
     * cleaner, but, it will cache the images, which makes this less dynamic.
     */
    private void useGlideInstead(){
        Glide.with(this /* context */)
                .using(new FirebaseImageLoader())
                .load(reference4)
                .into(dynamic_image_iv4);
    }

}