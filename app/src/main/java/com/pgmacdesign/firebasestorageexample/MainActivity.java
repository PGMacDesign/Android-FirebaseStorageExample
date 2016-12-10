package com.pgmacdesign.firebasestorageexample;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Firebase
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private StorageMetadata metadata;
    private OnProgressListener onProgressListener;
    private OnPausedListener onPausedListener;
    private OnSuccessListener onSuccessListenerUpload, onSuccessListenerDownload;
    private OnFailureListener onFailureListener;

    private File fileToDownload;

    private Button upload_picture, upload_picture_local, download_picture;
    private ImageView image_view_for_download;
    private TextView text_display;

    private static final String FILE_STRUCTURE_TOP_LEVEL = "images";
    private static final String FILE_STRUCTURE_USERS = FILE_STRUCTURE_TOP_LEVEL + "/users";
    private static final String BUCKET_URL = "gs://fir-testingapp-57fd1.appspot.com";

    private static final String SAMPLE_IMAGE_URL =
            "https://imgs.xkcd.com/comics/compiling.png";
    private static final String TAG = "Firebase Test App: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initFirebase();
    }

    private void initUI(){
        this.image_view_for_download = (ImageView) this.findViewById(R.id.image_view_for_download);
        this.upload_picture = (Button) this.findViewById(R.id.upload_picture);
        this.upload_picture_local = (Button) this.findViewById(R.id.upload_picture_local);
        this.download_picture = (Button) this.findViewById(R.id.download_picture);
        this.text_display = (TextView) this.findViewById(R.id.text_display);

        this.download_picture.setOnClickListener(this);
        this.upload_picture.setOnClickListener(this);
        this.upload_picture_local.setOnClickListener(this);
    }

    private void initFirebase(){
        this.auth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReferenceFromUrl(BUCKET_URL);
        this.uploadTask = null; //Set dynamically by type uploading
        this.metadata = null; //Set dynamically by method

        initFirebaseListeners();
    }

    private void initFirebaseListeners(){
        this.onProgressListener = new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                        (taskSnapshot.getTotalByteCount());
                logData("Progress: " + progress + "%");
            }
        };

        this.onPausedListener = new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                        (taskSnapshot.getTotalByteCount());
                logData("Paused at: " + progress + "%");
            }
        };

        this.onSuccessListenerUpload = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if(downloadUrl != null){
                    logData(downloadUrl.toString());
                    text_display.setText("Success!");
                } else {
                    logData("Download URL was null");
                    text_display.setText("Failure!");
                }
            }
        };

        this.onSuccessListenerDownload = new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                long totalByteCount = taskSnapshot.getTotalByteCount();
                //For referencing the size of the file
                logData("Size of downloaded file = " + totalByteCount + " bytes.");

                if(fileToDownload != null){
                    logData(fileToDownload.toString());
                    text_display.setText("Success!");
                    image_view_for_download.setImageURI(Uri.fromFile(fileToDownload));
                    // TODO: 2016-12-09 here is where I left off
                    // STOPSHIP: 2016-12-09  https://firebase.google.com/docs/storage/android/download-files
                } else {
                    logData("File could not download");
                    text_display.setText("Failure!");
                }
            }
        };

        this.onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                text_display.setText("Failure!");
            }
        };
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upload_picture:
                uploadPhoto();
                break;

            case R.id.upload_picture_local:
                Intent pickImageIntent;
                if(Build.VERSION.SDK_INT < 20) {
                    pickImageIntent = new Intent();
                    pickImageIntent.setType("image/*");
                    pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    pickImageIntent.addCategory(Intent.CATEGORY_OPENABLE);
                } else {
                    pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore
                            .Images.Media.EXTERNAL_CONTENT_URI);
                }
                startActivityForResult(Intent.createChooser(pickImageIntent, "Select Picture"), 123);

                break;

            case R.id.download_picture:
                downloadPicture();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == this.RESULT_OK){
            if(requestCode == 123){
                try {
                    System.out.println("DATA = " + data.toString());
                    System.out.println("DATA URI = " + data.getData());
                    System.out.println("DATASTRING = " + data.getDataString());

                    Uri uri = data.getData();
                    logData("uri before fix = " + uri);
                    String newUri = getRealPathFromUri(MainActivity.this, uri);
                    logData("uri after fix = " + newUri);
                    File file = new File(newUri);
                    uploadPictureViaFile(file);
                    // TODO: 2016-12-05 code here for upload
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private StorageMetadata buildFileMetadata(){

        // Create file metadata including the content type
        metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("Patrick", "abc123")
                .setCustomMetadata("Patrick2", "321cba")
                .build();

        return metadata;
    }


    private void logData(Object obj){
        Log.d(TAG, obj + "");
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






    /////////////////////////////////////////
    // This is for Downloading Pictures /////
    /////////////////////////////////////////

    private void downloadPicture(){
        this.storageRef = storageRef.getRoot().child(FILE_STRUCTURE_USERS);
        fileToDownload = null;
        try {
            fileToDownload = File.createTempFile("images", "jpg");
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        if(fileToDownload != null){
            this.storageRef.getFile(fileToDownload)
                    .addOnSuccessListener(onSuccessListenerDownload)
                    .addOnFailureListener(onFailureListener);
        }
    }

    /////////////////////////////////////////
    // This is for Uploading Pictures ///////
    /////////////////////////////////////////
    /**
     * Upload a picture via a file path
     */
    private void uploadPictureViaFile(File file){

        if(file == null){
            return;
        }

        this.storageRef = storageRef.getRoot().child(FILE_STRUCTURE_USERS);
        uploadTask = storageRef.putFile(Uri.fromFile(file), buildFileMetadata());

        // Register observers to listen for when the download is done or if it fails
        uploadTask
                .addOnFailureListener(onFailureListener)
                .addOnSuccessListener(onSuccessListenerUpload);


    }

    /**
     * Upload the actual photo
     */
    private void uploadPhoto(){
        this.storageRef = storageRef.getRoot().child(FILE_STRUCTURE_USERS);
        DownloadImageFromWeb async = new DownloadImageFromWeb(this, SAMPLE_IMAGE_URL);
        async.execute();
    }



    /////////////////////////////////////////
    // This is for Utilities ////////////////
    /////////////////////////////////////////

    /**
     * Convert the Uri:
     * Style content://media/external/images/media/nameOfPicture
     * -- TO --
     * Style Finished: /storage/emulated/0/Pictures/use01.jpg
     * @param context
     * @param contentUri
     * @return
     */
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Download a file from the web. This defaults to a photo (png)
     */
    private class DownloadImageFromWeb extends AsyncTask<Void, Integer, File> {

        private Context context;
        private String imageUrl;
        private long lengthOfTimeToDelay, startTime, endTime;

        /**
         * Download an image from the web into a file and send that file back via the listener
         * @param context
         * @param imageUrl String image Url
         */
        public DownloadImageFromWeb(Context context, String imageUrl) {
            this.context = context;
            this.imageUrl = imageUrl;
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 22);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(Void... params) {
            /*
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
            DownloadManager downloadManager = (DownloadManager) getSystemService
                    (Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
            */

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, new Date().getTime() + "_firebaseTesting.jpg");
            int count;
            try {
                file.createNewFile(); //.getParentFile().mkdirs()
                URL url = new URL(imageUrl);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(file);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    int publishNum = ((int)((total*100/lenghtOfFile)));
                    publishProgress(publishNum);
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                return file;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            //Start the upload here:
            uploadPictureViaFile(file);
        }
    }






}
