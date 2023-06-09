package com.camera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;


public class MainActivity extends AppCompatActivity
{
    public static final int PICK_IMAGE_CODE = 100;
    public static final int DS_PHOTO_EDITOR_REQUEST_CODE = 200;
    private static final int REQUEST_EXTERNAL_STORAGE_CODE = 1000;
    public static final String OUTPUT_PHOTO_DIRECTORY = "ds_photo_editor_sample";
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.imageView = findViewById(R.id.mainImageView);
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.openGalleryButton:
                this.verifyStoragePermissionsAndPerformOperation(REQUEST_EXTERNAL_STORAGE_CODE);
                break;
        }
    }

    private void verifyStoragePermissionsAndPerformOperation(int requestPermissionCode)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_CODE);
        }
        else
        {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, requestPermissionCode);
            }
            else
            {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_CODE);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("The app needs this permission to edit photos on your device.");
            builder.setPositiveButton("Update Permission",  new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    verifyStoragePermissionsAndPerformOperation(REQUEST_EXTERNAL_STORAGE_CODE);
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Обработка резльтатов */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case PICK_IMAGE_CODE:
                    Uri inputImageUri = data.getData();
                    if (inputImageUri != null)
                    {
                        Intent dsPhotoEditorIntent = new Intent(this, DsPhotoEditorActivity.class);
                        dsPhotoEditorIntent.setData(inputImageUri);

                        // Это не особо важно. При выводе окна с каталогом редактированная, фотка будет сохранена в указанной папке на внешнем хранилище устройства. Иначе отредактированная фотография по умолчанию будет сохранена в папку с именем «DS_Photo_Editor».
                        // Да и вообще, язык текста будет зависить от языка системника.
                        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, OUTPUT_PHOTO_DIRECTORY);
                        startActivityForResult(dsPhotoEditorIntent, DS_PHOTO_EDITOR_REQUEST_CODE);
                    }
                    else
                    {
                        Toast.makeText(this, "Выбери изображение из своей галереи", Toast.LENGTH_LONG).show();
                    }
                    break;
                case DS_PHOTO_EDITOR_REQUEST_CODE:
                    Uri outputUri = data.getData();
                    imageView.setImageURI(outputUri);
                    Toast.makeText(this, "Фото в " + OUTPUT_PHOTO_DIRECTORY + " успешно сохранено!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}