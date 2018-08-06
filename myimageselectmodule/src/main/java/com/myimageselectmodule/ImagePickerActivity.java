package com.myimageselectmodule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImagePickerActivity extends AppCompatActivity {

    private static final String TAG = "tag";
    private File finalFile;
    private String galleryPath;
    private Bitmap bitmap;
    private byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        int item = getIntent().getIntExtra(CommanUtils.OPEN_INTENT, 0);
        Log.d(TAG, "onCreate: " + item);

        if (item == CommanUtils.OPEN_CAMERA) {
            openCamera();
        } else if (item == CommanUtils.OPEN_GALLERY) {
            openGallery();
        }

    }

    private void openGallery() {
        String[] PERMISSIONS1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if (!hasImagePermissions(this, PERMISSIONS1)) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS1, CommanUtils.REQUEST_GALLERY);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);//
            startActivityForResult(Intent.createChooser(intent, "Select File"), CommanUtils.REQUEST_GALLERY);
        }
    }


    private void openCamera() {

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (!hasImagePermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, CommanUtils.REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CommanUtils.REQUEST_CAMERA);
        }
    }


    public static boolean hasImagePermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult:requestCode " + requestCode);
        switch (requestCode) {

            case CommanUtils.REQUEST_CAMERA:
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted && cameraAccepted) {
                    openCamera();
                }
                break;

            case CommanUtils.REQUEST_GALLERY:
                boolean storageAccepted1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted1) {
                    openGallery();
                }
                break;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult:requestCode " + requestCode);
        Bitmap photo = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CommanUtils.REQUEST_CAMERA) {

                photo = (Bitmap) data.getExtras().get("data");
                //GET THE BITMAP FROM THE BITMAP
                Uri tempUri = getImageUri(getApplicationContext(), photo);
                //GET THE ACTUAL PATH
                finalFile = new File(getRealPathFromURI2(tempUri));
                onCapturedImageResult(data);

                Intent intent = getIntent();
                intent.putExtra(CommanUtils.RESULT, finalFile.getAbsolutePath());
                intent.putExtra(CommanUtils.BITMAP, tempUri);
                setResult(Activity.RESULT_OK, intent);
                finish();

            } else if (requestCode == CommanUtils.REQUEST_GALLERY) {

                //onSelectGalleryResult(data);
                Uri selectedImageUri = data.getData();
                String url = data.getData().toString();
                Log.d(TAG, "onActivityResult: " + selectedImageUri);
                String selectedImagePath = ImageFilePath.getPath(getApplicationContext(), selectedImageUri);

                if (selectedImagePath != null) {
                    setGalleryPic(selectedImagePath);
                }

                if (selectedImagePath == null) {
                    @SuppressLint("Recycle")
                    Cursor returnCursor = null;
                    if (selectedImageUri != null) {
                        returnCursor = getContentResolver().query(selectedImageUri, null, null, null, null);
                    }
                    if (returnCursor != null) {
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        returnCursor.moveToFirst();
                        onSelectGalleryResult(data);
                        selectedImagePath = returnCursor.getString(nameIndex);
                    }
                }

                Intent intent = getIntent();
                intent.putExtra(CommanUtils.RESULT, selectedImagePath);
                intent.putExtra(CommanUtils.BITMAP, selectedImageUri);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "User cancelled.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Sorry! Failed", Toast.LENGTH_SHORT).show();
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI2(Uri uri) {
        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        int idx = 0;
        String index = null;
        if (cursor != null) {
            cursor.moveToFirst();
            idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            index = cursor.getString(idx);
        }
        return index;
    }

    private void onCapturedImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (thumbnail != null) {
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        }
        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        String mCurrentPhotoPath = finalFile.getAbsolutePath();
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//
//        int width = selectedImageView.getWidth();
//        int height = selectedImageView.getHeight();
//
//        int scaleFactor = CommanUtils.calculateInSampleSize(bmOptions, width, height);
        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;

        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        try {
            bitmap = CommanUtils.modifyOrientation(bitmap, mCurrentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();
        //  selectedImageView.setImageBitmap(thumbnail);
    }

    private void setGalleryPic(String selectedImagePath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, bmOptions);

        /*int width = selectedImageView.getWidth();
        int height = selectedImageView.getHeight();
*/
        // int scaleFactor = CommanUtils.calculateInSampleSize(bmOptions, width, height);

        bmOptions.inJustDecodeBounds = false;
        // bmOptions.inSampleSize = scaleFactor;
        bmOptions.inScaled = true;

        bitmap = BitmapFactory.decodeFile(selectedImagePath, bmOptions);

        try {
            bitmap = CommanUtils.modifyOrientation(bitmap, selectedImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();


    }

    private void onSelectGalleryResult(Intent data) {

        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap
                        (getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
