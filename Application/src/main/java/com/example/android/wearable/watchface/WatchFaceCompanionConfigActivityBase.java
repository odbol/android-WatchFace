package com.example.android.wearable.watchface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by tyler on 11/2/15.
 */
public class WatchFaceCompanionConfigActivityBase extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<DataApi.DataItemResult> {
    // TODO: use the shared constants (needs covering all the samples with Gradle build model)
    protected static final String KEY_BACKGROUND_COLOR = "BACKGROUND_COLOR";
    protected static final String KEY_HOURS_COLOR = "HOURS_COLOR";
    protected static final String KEY_MINUTES_COLOR = "MINUTES_COLOR";
    protected static final String KEY_SECONDS_COLOR = "SECONDS_COLOR";
    private static final String TAG = "DigitalWatchFaceConfig";
    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";
    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null) {
            onConnectedToPeer(mPeerId);
        } else {
            // in case they just launched the app, instead of going through the Wear app first to configure,
            // look for an available watch.
            Wearable.NodeApi
                    .getConnectedNodes(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {

                    if (result.getStatus().isSuccess()) {

                        final List<Node> nodes = result.getNodes();
                        if (nodes.size() > 0) {
                            mPeerId = nodes.get(0).getId();

                            onConnectedToPeer(mPeerId);

                            return;
                        }
                    }

                    // fail
                    displayNoConnectedDeviceDialog();
                }
            });
        }
    }

    private void onConnectedToPeer(String peerId) {

        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(peerId).build();
        Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void sendConfigUpdateMessage(String configKey, int color) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(configKey, color);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + Integer.toHexString(color));
            }
        }
        else {
            Toast.makeText(this, "No watch connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        Log.d(TAG, "Got data from watch.");
    }
}
