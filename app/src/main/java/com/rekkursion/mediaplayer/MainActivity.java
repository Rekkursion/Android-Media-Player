package com.rekkursion.mediaplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaControllerFragment.OnFragmentInteractionListener {
    private Context context = null;
    private NavigationView navMain = null;
    private DrawerLayout dlyMain = null;
    private SeekBar skbMediaPlayBar = null;
    private GoodMediaPlayer mediaPlayer = null;
    private MediaControllerFragment mediaControllerFragment = null;
    private final int REQ_CODE_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 4731;
    private final int REQ_CODE_INTENT_GET_AUDIO_FILE_FROM_EXTERNAL_STORAGE = 2002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getApplicationContext();
        mediaPlayer = new GoodMediaPlayer(context);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.setControllerContainerAndChildren(mediaControllerFragment.getContainer());
    }

    // the navigation buttons 'back' clicked
    @Override
    public void onBackPressed() {
        if(dlyMain != null && dlyMain.isDrawerOpen(GravityCompat.START))
            dlyMain.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    // items in navigation drawer selected
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.itm_get_audio_file_from_external_storage:
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    getAudioFileFromExternalStorage();
                else
                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_CODE_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                break;

        }

        return true;
    }

    // get results of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getAudioFileFromExternalStorage();
                break;
        }
    }

    // get results of intents
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_INTENT_GET_AUDIO_FILE_FROM_EXTERNAL_STORAGE:
                Uri fileUri = null;
                try {
                    fileUri = convertUri(data.getData());
                    mediaControllerFragment.onGotAudioFile(fileUri);
                    dlyMain.closeDrawer(GravityCompat.START);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error happened when getting file", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error happened when loading audio data", Toast.LENGTH_SHORT).show();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error happened when preparing audio data", Toast.LENGTH_SHORT).show();
                } finally {
                    fileUri = null;
                }
                break;
        }
    }

    // media controller fragment interaction
    @Override
    public void onMediaControllerFragmentInteraction(Uri fileUri) throws IOException, IllegalStateException {
        mediaPlayer.setDataSourceThenPrepareAsync(fileUri);
    }

    @Override
    protected void onDestroy() {
        mediaPlayer.onDestroy();
        super.onDestroy();
    }

    // initialize views in main activity
    private void initViews() {
        // main drawer layout
        dlyMain = findViewById(R.id.dly_main);

        // main navigation view
        navMain = findViewById(R.id.nav_main);
        navMain.setNavigationItemSelectedListener(this);

        // get media controller fragment
        mediaControllerFragment = MediaControllerFragment.newInstance();
        // add media controller fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fly_media_controller, mediaControllerFragment);
        transaction.commit();
    }

    // get audio file from external storage
    private void getAudioFileFromExternalStorage() {
        Intent intentGetAudioFile = new Intent(Intent.ACTION_GET_CONTENT);
        intentGetAudioFile.setType("audio/*");
        startActivityForResult(Intent.createChooser(intentGetAudioFile, "Get Audio File"), REQ_CODE_INTENT_GET_AUDIO_FILE_FROM_EXTERNAL_STORAGE);
    }

    // convert uri from content uri to file uri
    private Uri convertUri(Uri uri) throws NullPointerException {
        if(uri == null)
            throw new NullPointerException();

        Uri newUri;
        if(uri.toString().substring(0, 7).equals("content")) {
            String[] colName = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, colName, null, null, null);

            cursor.moveToFirst();
            newUri = Uri.parse("file://" + cursor.getString(0));

            cursor.close();
        }
        else
            newUri = uri;

        return newUri;
    }
}
