package priyanka.example.com.myimagepickerapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myimageselectmodule.CommanUtils;
import com.myimageselectmodule.ImagePickerActivity;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TempActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 99;
    private static final String TAG = TempActivity.class.getSimpleName();

    @BindView(R.id.imagePathTextView)
    TextView imagePathTextView;
    @BindView(R.id.selectImageButton)
    Button selectImageButton;
    @BindView(R.id.selectedImageView)
    ImageView selectedImageView;
    @BindView(R.id.mainLinearLayout)
    LinearLayout mainLinearLayout;
    private int selecedOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(TempActivity.this);
        setTitle(R.string.activity_title_image_picker);
        selectImageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.selectImageButton) {
            selectImage();
        }
    }

    private void selectImage() {
        final CharSequence[] items = {getString(R.string.take_photo),
                getString(R.string.open_gallery), getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (items[which].equals(getString(R.string.take_photo))) {
                    selecedOption = 1;
                } else if (items[which].equals(getString(R.string.open_gallery))) {
                    selecedOption = 2;
                } else if (items[which].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
                openLib(selecedOption);
            }
        });

        builder.show();
    }


    private void openLib(int item) {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(CommanUtils.OPEN_INTENT, item);
        startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String path = null;

            if (data != null && data.hasExtra(CommanUtils.RESULT)) {
                path = data.getExtras().getString(CommanUtils.RESULT);
                imagePathTextView.setText(getString(R.string.path) + "  " + path);
            }


            Uri uri = null;
            if (data != null) {
                uri = data.getExtras().getParcelable(CommanUtils.BITMAP);
            }
            try {
                Bitmap myBitmap = MediaStore.Images.Media.getBitmap
                        (getApplicationContext().getContentResolver(), uri);
                selectedImageView.setImageBitmap(myBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
