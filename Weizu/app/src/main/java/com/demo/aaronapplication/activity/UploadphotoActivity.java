package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.demo.aaronapplication.weizu.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class UploadphotoActivity extends Activity implements View.OnClickListener {

    //ImageView to show the selected photo
    private ImageView photopreview;
    private ImageView commitbtn, shotbtn, pickbtn, backbtn;

    private Uri imageuri;

    private Bitmap selected_photo;

    private static final int TAKE = 0, PICK = 1, PHOTO_CROP = 2;
    private static final int PHOTO_PICKED_WITH_DATA = 3021;
    private static final int CAMERA_WITH_DATA = 3023;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadphoto);
        photopreview = (ImageView)findViewById(R.id.preview);
        commitbtn = (ImageView)findViewById(R.id.commit);
        commitbtn.setOnClickListener(this);
        pickbtn = (ImageView)findViewById(R.id.pick);
        pickbtn.setOnClickListener(this);
        shotbtn = (ImageView)findViewById(R.id.shot);
        shotbtn.setOnClickListener(this);
        backbtn = (ImageView)findViewById(R.id.back);
        backbtn.setOnClickListener(this);

        Intent incomeintent = getIntent();
        Intent outintent = new Intent();
        switch (incomeintent.getIntExtra("action",0)) {
            case TAKE:
                File res = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/tmp.jpeg");
                if (res.exists()) {
                    res.delete();
                }
                outintent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                outintent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(res));
                startActivityForResult(outintent, CAMERA_WITH_DATA);
                break;
            case PICK:
                outintent.setType("image/*");
                outintent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(outintent, PHOTO_PICKED_WITH_DATA);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if(resultCode != RESULT_OK)
            return;
        switch(requestCode){
            case CAMERA_WITH_DATA:
                //final Bitmap photo = data.getParcelableExtra("data");
                File res = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/tmp.jpeg");
                if (res.exists())
                    startPhotoZoom(Uri.fromFile(res));
                else {
                    selected_photo.recycle();
                    selected_photo = null;
                }
                //photopreview.setImageBitmap(photo);
                break;
            case PHOTO_PICKED_WITH_DATA:
                if (data != null) {
                    imageuri = null;
                    imageuri = data.getData();
                    if (imageuri != null) {
                        Log.i("geturi", "succeed");
                        startPhotoZoom(imageuri);
                    } else {
                        selected_photo.recycle();
                        selected_photo = null;
                    }
                }
                break;
            case PHOTO_CROP:
                selected_photo = data.getParcelableExtra("data");
                photopreview.setImageBitmap(selected_photo);
                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.pick:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
                break;
            case R.id.shot:
                Intent intent1 = new Intent();
                File res = new File(Environment.getExternalStorageDirectory().getPath()+"/weizu/img/tmp.jpeg");
                if (res.exists())
                    res.delete();
                intent1.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(res));
                startActivityForResult(intent1, CAMERA_WITH_DATA);
                break;
            case R.id.commit:
                Intent intent2 = new Intent();
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                selected_photo.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                byte[] p = ostream.toByteArray();
                try {
                    ostream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent2.putExtra("photo", Base64.encodeToString(p, Base64.DEFAULT));
                intent2.putExtra("action",mainActivity.PHOTOGET);
                setResult(RESULT_OK, intent2);
                finish();
                break;
            case R.id.back:
                Intent intent3 = new Intent();
                intent3.putExtra("action",mainActivity.CANCEL);
                setResult(RESULT_OK, intent3);
                finish();
                break;
        }
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_CROP);
    }

}
