package com.example.android.wearable.watchface;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.wearable.companion.WatchFaceCompanion;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;


import android.support.v7.graphics.Palette;
/**
 * Created by tyler on 11/2/15.
 */
public class SelfiePaletteActivity extends WatchFaceCompanionConfigActivityBase {

    static final int REQUEST_IMAGE_CAPTURE = 11;


    private ImageView addButton;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_palette);

        TextView label = (TextView)findViewById(R.id.label);
        ComponentName name = getIntent().getParcelableExtra(
                WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);
        if (name != null) {
            label.setText(label.getText() + " (" + name.getClassName() + ")");
        }

        imageView = (ImageView)findViewById(R.id.imageView);

        addButton = (ImageView)findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAddButton();
            }
        });
    }


    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            setUpAllPickers(null);
        }
    }

    private void setUpAllPickers(DataMap config) {

    }




    private void clickAddButton() {
        dispatchTakePictureIntent();
    }

    /***
     * Launch the camera to take a selfie!
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // get the selfie!
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            getPaletteFromBitmap(imageBitmap);
        }
    }

    private void getPaletteFromBitmap(Bitmap imageBitmap) {

    }

}
