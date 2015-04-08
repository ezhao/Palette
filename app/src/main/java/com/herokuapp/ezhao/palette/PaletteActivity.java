package com.herokuapp.ezhao.palette;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class PaletteActivity extends ActionBarActivity {
    @InjectView(R.id.ivPreview) ImageView ivPreview;
    @InjectView(R.id.btnVibrantSwatch) Button btnVibrantSwatch;
    @InjectView(R.id.btnDarkVibrantSwatch) Button btnDarkVibrantSwatch;

    public final static int PICK_PHOTO_CODE = 1046;
    public final static int CAPTURE_IMAGE_CODE = 1034;

    public final String APP_TAG = "PaletteApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);

        ButterKnife.inject(this);
    }

    @OnClick(R.id.btnLaunchGallery)
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    @OnClick(R.id.btnLaunchCamera)
    public void onPickCamera(View view) {
        // Create Intent to take a picture and save in temp space
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoUri());
        startActivityForResult(intent, CAPTURE_IMAGE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getPhotoUri();
                Bitmap selectedImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                changeOutPhoto(selectedImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if (data != null && resultCode == RESULT_OK && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = null;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                changeOutPhoto(selectedImage);
            } catch (IOException e) {
                e.printStackTrace(); // TODO(emily) handle somewhat intelligently
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_palette, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Uri getPhotoUri() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + "tempPhoto.jpg"));
    }

    private void changeOutPhoto(Bitmap selectedImage) {
        // Load the selected image into a preview
        ivPreview.setImageBitmap(selectedImage);

        Palette.generateAsync(selectedImage, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if (vibrant != null) {
                    btnVibrantSwatch.setBackgroundColor(vibrant.getRgb());
                }

                Palette.Swatch darkVibrant = palette.getDarkVibrantSwatch();
                if (darkVibrant != null) {
                    btnDarkVibrantSwatch.setBackgroundColor(darkVibrant.getRgb());
                }
            }
        });
    }
}
