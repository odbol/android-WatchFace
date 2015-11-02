package com.example.android.wearable.watchface;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.wearable.watchface.models.WatchFacePalette;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;


import android.support.v7.graphics.Palette;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by tyler on 11/2/15.
 */
public class SelfiePaletteActivity extends WatchFaceCompanionConfigActivityBase {

    static final int REQUEST_IMAGE_CAPTURE = 11;
    private static final String TAG = "SelfiePaletteActivity";

    /**
     * Holds the WatchFacePalette objects so the user can choose a palette
     */
    static class PaletteAdapter extends ArrayAdapter<WatchFacePalette> {

        public PaletteAdapter(Context context, int resource) {
            super(context, resource, R.id.paletteLabel);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);

            final WatchFacePalette palette = getItem(position);

            view.setBackgroundColor(palette.getBackground());

            final TextView label = (TextView) view.findViewById(R.id.paletteLabel);
            label.setTextColor(palette.getText());
            label.setText("Palette " + position);

            return view;
        }
    }

    private ImageView addButton;
    private ImageView imageView;

    private ListView palettesList;
    private PaletteAdapter palettesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_palette);

        // intitalize all UI elements
        imageView = (ImageView)findViewById(R.id.imageView);

        palettesAdapter = new PaletteAdapter(this, R.layout.palette_item);

        palettesList = (ListView)findViewById(R.id.palettesList);
        palettesList.setAdapter(palettesAdapter);
        palettesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WatchFacePalette palette = palettesAdapter.getItem(position);

                choosePalette(palette);
            }
        });

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

            addPalettesFromBitmap(imageBitmap);
        }
    }

    private void addPalettesFromBitmap(Bitmap bitmap) {

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {

                Observable.from(p.getSwatches())
                        .limit(8)
                        .map(new Func1<Palette.Swatch, WatchFacePalette>() {
                            @Override
                            public WatchFacePalette call(Palette.Swatch swatch) {
                                return new WatchFacePalette(swatch);
                            }
                        })
                        .subscribe(new Action1<WatchFacePalette>() {
                            @Override
                            public void call(WatchFacePalette palette) {
                                palettesAdapter.add(palette);

                            }
                        });

            }
        });
    }


    private void choosePalette(WatchFacePalette palette) {
        Log.d(TAG, "sending palette to watch " + palette.getText());
        
        final int textColor = palette.getText();
        sendConfigUpdateMessage(KEY_BACKGROUND_COLOR, palette.getBackground());
        sendConfigUpdateMessage(KEY_HOURS_COLOR, textColor);
        sendConfigUpdateMessage(KEY_MINUTES_COLOR, textColor);
        sendConfigUpdateMessage(KEY_SECONDS_COLOR, textColor);
    }


}
