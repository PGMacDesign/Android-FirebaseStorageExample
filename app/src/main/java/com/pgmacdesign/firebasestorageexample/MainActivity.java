package com.pgmacdesign.firebasestorageexample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button open_upload_download_activity_button, view_dynamic_images_button,
            profile_picture_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI(){
        this.profile_picture_button = (Button) this.findViewById(
                R.id.profile_picture_button);
        this.open_upload_download_activity_button = (Button) this.findViewById(
                R.id.open_upload_download_activity_button);
        this.view_dynamic_images_button = (Button) this.findViewById(
                R.id.view_dev_chosen_images_button);

        this.profile_picture_button.setTransformationMethod(null);
        this.open_upload_download_activity_button.setTransformationMethod(null);
        this.view_dynamic_images_button.setTransformationMethod(null);

        this.profile_picture_button.setOnClickListener(this);
        this.open_upload_download_activity_button.setOnClickListener(this);
        this.view_dynamic_images_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            //This means we don't have permission to access storage, so we need to request if first
            requestPermissions();
            return;
        }

        Intent intent = null;
        switch (v.getId()){
            case R.id.profile_picture_button:
                intent = new Intent(MainActivity.this, ProfilePhoto.class);
                break;

            case R.id.open_upload_download_activity_button:
                intent = new Intent(MainActivity.this, UploadDownloadPictures.class);
                break;

            case R.id.view_dev_chosen_images_button:
                intent = new Intent(MainActivity.this, ViewDynamicImages.class);
                break;

        }

        if(intent != null){
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 22);
    }
}
