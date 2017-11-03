package com.example.anton.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by anton on 11/3/17.
 */

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private Camera mCamera = null;
    private ImageView MyCameraPreview = null;
    private Bitmap bitmap = null;
    private int[] pixels = null;
    private byte[] FrameData = null;
    private int imageFormat;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private boolean bProcessing = false;
    private MainActivity _context;
    public boolean isAndroid = true;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight,
                         ImageView CameraPreview, MainActivity context)
    {
        PreviewSizeWidth = PreviewlayoutWidth;
        PreviewSizeHeight = PreviewlayoutHeight;
        MyCameraPreview = CameraPreview;
        bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
        pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
        _context = context;

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera arg1)
    {
        Camera.Parameters parameters = mCamera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap after;
        if (this.isAndroid) {
            after = darkenBitmap(bitmap.copy(bitmap.getConfig(), true));
        } else {
            after = bitmap.copy(bitmap.getConfig(), true);
        }
        _context.changeImage(after);
        // At preview mode, the frame data will push to here.
    }
    private Bitmap sepicEffect(Bitmap val) {
        Bitmap bitmap = val;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] += 20;
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

    }

    private Bitmap darkenBitmap(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int pixel;

        // Iterate over each row (y-axis)
        for (int y = 0; y < height; y++) {
            // and each column (x-axis) on that row
            for (int x = 0; x < width; x++) {
                pixel = bitmap.getPixel(x, y);

                int a = Color.alpha( pixel );
                int r = Color.red( pixel );
                int g = Color.green( pixel );
                int b = Color.blue( pixel );


                bitmap.setPixel(x, y, Color.argb( a,
                        Math.max( (int)(r * 2), 0 ),
                        Math.max( (int)(g * 2), 0 ),
                        Math.max( (int)(b * 2), 0 ) ));
            }
        }
        return bitmap;
    }

    public void onPause()
    {
        mCamera.stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        Camera.Parameters parameters;

        parameters = mCamera.getParameters();
        // Set the camera preview size
        parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);

        imageFormat = parameters.getPreviewFormat();

        mCamera.setParameters(parameters);

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        mCamera = Camera.open();
        try
        {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            Log.i("sadfsd", "DoImageProcessing():");
            bProcessing = true;

            bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
//            MyCameraPreview.setImageBitmap(bitmap);
            bProcessing = false;
        }
    };
}