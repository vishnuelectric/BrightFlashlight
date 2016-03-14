package com.vishnu.brightflashlight;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener, Camera.ShutterCallback, Camera.PictureCallback {
    public static final String Tag = "tag";
    private static final int CROP_IMAGE = 2;
    private static final int PICK_IMAGE = 1;
    public Camera mCamera;
    public CameraPreview mPreview;
    Handler handler;
    TextView mSwitchOnOff, blinkSlow, blinkMedium, blinkFast;
    Runnable blinkRunnable;
    private boolean backCamera;
    private Bitmap bitmap;
    private int cameraId;
    private ImageView flashicon, trigger, cameraSwitch, gallery;
    private File mediaStorageDir;
    private String timeStamp;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private void releaseCameraAndPreview() {
        this.mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean safeCameraOpen(int id) {


        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        /*   Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);*/
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(data.getData(), "image/*");
                intent.putExtra("return-data", true);
                intent.putExtra("crop", "true");
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 16);
                intent.putExtra("aspectY", 9);
                startActivityForResult(intent, CROP_IMAGE);
            }
            if (requestCode == CROP_IMAGE) {
                File file = new File(getRealPathFromURI(this, data.getData()));
               /* UploadTask uploadTask = new UploadTask(file);
                uploadTask.execute();*/


            }
        }
    }

    @Override
    public void onClick(View paramView) {


        switch (paramView.getId()) {


            case R.id.switch_on_off:
                Drawable background = mSwitchOnOff.getBackground();
                /*if (background instanceof ShapeDrawable) {
                    ((ShapeDrawable)background).getPaint().setColor(getResources().getColor(R.color.colorToSet));
                } else if (background instanceof GradientDrawable) {
                    ((GradientDrawable)background).setColor(getResources().getColor(R.color.colorToSet));
                } else if (background instanceof ColorDrawable) {
                    ((ColorDrawable)background).setColor(getResources().getColor(R.color.colorToSet));
                }*/
                Camera.Parameters parameters = this.mCamera.getParameters();
                blinker(4, 0, true);
                if (parameters.getFlashMode().equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    this.mCamera.setParameters(parameters);
                    ((GradientDrawable) background).setColor(getResources().getColor(R.color.colorPrimary));
                    mSwitchOnOff.setTextColor(getResources().getColor(android.R.color.white));
                    mSwitchOnOff.setText("FLASH ON");
                    if(!mInterstitialAd.isLoaded())
                        mInterstitialAd.loadAd(adRequest);
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                ((GradientDrawable) background).setColor(getResources().getColor(R.color.background_material_light));
                mSwitchOnOff.setTextColor(getResources().getColor(android.R.color.darker_gray));
                mSwitchOnOff.setText("FLASH OFF");
                this.mCamera.setParameters(parameters);
                if(mInterstitialAd.isLoaded())
                    mInterstitialAd.show();

                if(!mInterstitialAd.isLoaded())
                    mInterstitialAd.loadAd(adRequest);


                return;

            case R.id.blink_slow:
                Camera.Parameters parameters1 = this.mCamera.getParameters();
                blinker(1, 1000, true);
                return;
            case R.id.blink_medium:
                Camera.Parameters parameters2 = this.mCamera.getParameters();
                blinker(2, 500, true);
                break;
            case R.id.blink_fast:
                Camera.Parameters parameters3 = this.mCamera.getParameters();
                blinker(3, 300, true);

        }


    }

    private void blinker(final int type, final int milisecs, boolean status) {
        switch (type) {
            case 1:
                blinker(4,0,true);
                ((GradientDrawable) mSwitchOnOff.getBackground()).setColor(getResources().getColor(R.color.colorPrimary));
                mSwitchOnOff.setTextColor(getResources().getColor(android.R.color.white));
                mSwitchOnOff.setText("FLASH ON");
                ((GradientDrawable) blinkSlow.getBackground()).setColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 2:
                blinker(4,0,true);
                ((GradientDrawable) mSwitchOnOff.getBackground()).setColor(getResources().getColor(R.color.colorPrimary));
                mSwitchOnOff.setTextColor(getResources().getColor(android.R.color.white));
                mSwitchOnOff.setText("FLASH ON");
                ((GradientDrawable) blinkMedium.getBackground()).setColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 3:
                blinker(4, 0, true);
                ((GradientDrawable) mSwitchOnOff.getBackground()).setColor(getResources().getColor(R.color.colorPrimary));
                mSwitchOnOff.setTextColor(getResources().getColor(android.R.color.white));
                mSwitchOnOff.setText("FLASH ON");
                ((GradientDrawable) blinkFast.getBackground()).setColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 4:
                ((GradientDrawable) blinkSlow.getBackground()).setColor(getResources().getColor(R.color.background_material_light));
                ((GradientDrawable) blinkMedium.getBackground()).setColor(getResources().getColor(R.color.background_material_light));
                ((GradientDrawable) blinkFast.getBackground()).setColor(getResources().getColor(R.color.background_material_light));
                ((GradientDrawable) mSwitchOnOff.getBackground()).setColor(getResources().getColor(R.color.background_material_light));
                mSwitchOnOff.setTextColor(getResources().getColor(android.R.color.darker_gray));
                mSwitchOnOff.setText("FLASH OFF");
                break;

        }
        handler.removeCallbacks(blinkRunnable);
        blinkRunnable = new Runnable() {
            Camera.Parameters parameters = mCamera.getParameters();
            @Override
            public void run() {
                System.out.println(type);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                try {
                    synchronized (this){
                        wait(100);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                handler.postDelayed(this, milisecs);
            }
        };

        if (milisecs > 0)// to switch off using main switch passing 0 millisecs
            handler.postDelayed(blinkRunnable, milisecs);


    }

    public void onCreate(Bundle bundle) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        if (bundle != null)
            cameraId = bundle.getInt("cameraid", 0);
        else
            cameraId = 0;
        mSwitchOnOff = (TextView) findViewById(R.id.switch_on_off);
        mSwitchOnOff.setOnClickListener(this);
        blinkSlow = (TextView) findViewById(R.id.blink_slow);
        blinkSlow.setOnClickListener(this);
        blinkMedium = (TextView) findViewById(R.id.blink_medium);
        blinkMedium.setOnClickListener(this);
        blinkFast = (TextView) findViewById(R.id.blink_fast);
        blinkFast.setOnClickListener(this);
        this.mInterstitialAd = new InterstitialAd(this);
        this.mInterstitialAd.setAdUnitId("ca-app-pub-7259770719293184/7305397557");
        adRequest = new AdRequest.Builder().addTestDevice("11EC09A99539249C285DEB0BE5451927").addTestDevice("EE2B081F3062102816D633EFAA98AFE8").build();
       /* cameraSwitch = (ImageView)findViewById(R.id.camera_switch);
        cameraSwitch.setOnClickListener(this);
        flashicon=(ImageView)findViewById(R.id.flash);
        flashicon.setOnClickListener(this);
        trigger= (ImageView)findViewById(R.id.trigger);
        trigger.setOnClickListener(this);
        gallery = (ImageView)findViewById(R.id.gallery);
        gallery.setOnClickListener(this);*/
        handler = new Handler(getMainLooper());


    }

    public void onDestroy() {
        super.onDestroy();
    }


@Override
public void onPause() {

        super.onPause();
    blinker(4,0,true);

        releaseCameraAndPreview();
        // ((FrameLayout)findViewById(R.id.frame)).removeView(this.mPreview);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera paramCamera) {

        int i = 0;
        this.mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "cameraSearch");
        if ((!this.mediaStorageDir.exists()) && (!this.mediaStorageDir.mkdirs())) {
            Toast.makeText(getApplicationContext(), "directory create failed", Toast.LENGTH_SHORT).show();
            return;
        }
        FileOutputStream fileOutputStream;
        this.timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(this.mediaStorageDir.getPath() + File.separator + "IMG_" + this.timeStamp + ".jpg");
        try {
            fileOutputStream = new FileOutputStream(file);
            (fileOutputStream).write(data);
            (fileOutputStream).close();
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), file.getName(), file.getName());
            i = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent;
        if (i != 0) {
            Toast.makeText(getApplicationContext(), "image saved" + this.timeStamp + " to sd card", Toast.LENGTH_SHORT).show();
            intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(getImageContentUri(this, file), "image/*");
            startActivityForResult(intent, CROP_IMAGE);
            return;
        }
        Toast.makeText(getApplicationContext(), "Some Problem occurred", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        this.mPreview = new CameraPreview(this, this.cameraId);
        //((FrameLayout)findViewById(R.id.frame)).addView(this.mPreview, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (safeCameraOpen(this.cameraId)) {
            //this.mPreview.setCamera(this.mCamera);
        }
        this.mCamera.startPreview();
    }

    @Override
    public void onSaveInstanceState(Bundle paramBundle) {
        super.onSaveInstanceState(paramBundle);
        paramBundle.putInt("cameraid", this.cameraId);
    }

    private void showNotification()
    {

    }

    @Override
    public void onShutter() {

    }
}
