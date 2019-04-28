package it.unipr.scarpentim.pasmtftest1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import it.unipr.scarpentim.pasmtftest1.img.ImageProcessor;
import it.unipr.scarpentim.pasmtftest1.tensorflow.Classifier;
import it.unipr.scarpentim.pasmtftest1.yolo.YoloV3Classifier;
import it.unipr.scarpentim.pasmtftest1.yolo.YoloV3TinyClassifier;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Yolov3TinyDrawRectTest {

    private static final String TAG = "pasm-YoloClassifier";
    Context appContext = InstrumentationRegistry.getTargetContext();


    private static final String YOLO_MODEL_FILE = "file:///android_asset/ultimate_yolov3-tiny.bp";
    private static final String YOLO_INPUT_NAME = "yolov3-tiny/net1"; //0 Tensor("yolov3-tiny/net1:0", shape=(?, 416, 416, 3), dtype=float32)
    private static final String YOLO_OUTPUT_NAMES = "yolov3-tiny/convolutional10/BiasAdd,yolov3-tiny/convolutional13/BiasAdd";
//            => Output layer:  Tensor("yolov3-tiny/convolutional10/BiasAdd:0", shape=(?, 13, 13, 255), dtype=float32)
//            => Output layer:  Tensor("yolov3-tiny/convolutional13/BiasAdd:0", shape=(?, 26, 26, 255), dtype=float32)

    private static final int YOLO_INPUT_SIZE = 416;

    private static final int[] YOLO_BLOCK_SIZE = {32, 16};

    public static final String SAMPLE_IMG = "cargo-bike-with-dog-flickr-grrsh.jpg";

    @Test
    public void drawImage() throws IOException {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, appContext, mLoaderCallback);

        YoloV3TinyClassifier detector = (YoloV3TinyClassifier) YoloV3TinyClassifier.create(
                appContext.getAssets(),
                YOLO_MODEL_FILE,
                YOLO_INPUT_SIZE,
                YOLO_INPUT_NAME,
                YOLO_OUTPUT_NAMES,
                YOLO_BLOCK_SIZE);

//        Bitmap loadedImage = getBitmapFromTestAssets(SAMPLE_IMG);

        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = testContext.getAssets();

        String[] list = assetManager.list("");

        for (int i = 0; i < list.length; i++) {
            if(list[i].endsWith(".jpg")) {
                Bitmap loadedImage = getBitmapFromTestAssets(list[i]);

                Bitmap redimBitmap = Bitmap.createScaledBitmap(loadedImage, YOLO_INPUT_SIZE, YOLO_INPUT_SIZE, false);

                Log.i(TAG, "start = " + System.currentTimeMillis());
                List<Classifier.Recognition> recognitions = detector.recognizeImage(redimBitmap);
                Log.i(TAG, " end  = " + System.currentTimeMillis());

                Log.i(TAG, "yolov3 recognitions = " + recognitions);

                ImageProcessor processor = new ImageProcessor(testContext);
                processor.loadImage(loadedImage, YOLO_INPUT_SIZE, YOLO_INPUT_SIZE);
                Mat mat = processor.drawBoxes(recognitions);
                Mat ultimate = new Mat();
                Imgproc.cvtColor(mat, ultimate, Imgproc.COLOR_RGB2BGR);
                checkPermissions();


                Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/img_yolo/boxes" + i + ".jpg", ultimate);
            }
        }






    }

    private void checkPermissions() {
        //check sdcard availability
        Assert.assertEquals("Media is not mounted!", Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED);
        //get and check Permissions
        MyPermissionRequester.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        int grantResult = ActivityCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Assert.assertEquals(PackageManager.PERMISSION_GRANTED, grantResult);
        grantResult = ActivityCompat.checkSelfPermission(InstrumentationRegistry.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Assert.assertEquals(PackageManager.PERMISSION_GRANTED, grantResult);

        //Bitmap ultimateImg = Bitmap.createScaledBitmap(loadedImage, YOLO_INPUT_SIZE, YOLO_INPUT_SIZE, false);
        File d = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "img_yolo");
        if (!d.exists()) {
            d.mkdirs();
        }
    }

    public Bitmap getBitmapFromTestAssets(String fileName) throws IOException {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = testContext.getAssets();

        InputStream testInput = assetManager.open(fileName);
        Bitmap bitmap = BitmapFactory.decodeStream(testInput);

        return bitmap;
    }

    @Ignore
    @Test
    public void sdcardTest(){

        //check sdcard availability
        Assert.assertEquals("Media is not mounted!", Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED);
        //get and check Permissions
        //MyPermissionRequester.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
//        int grantResult = ActivityCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        Assert.assertEquals(PackageManager.PERMISSION_GRANTED, grantResult);
//        grantResult = ActivityCompat.checkSelfPermission(InstrumentationRegistry.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        Assert.assertEquals(PackageManager.PERMISSION_GRANTED, grantResult);

        //finally try to create folder
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CompanyTest");
        if (!f.exists()){
            boolean mkdir = f.mkdirs();
            Assert.assertTrue("Folder '"+f.getAbsolutePath() + "' not Present!", mkdir);
        }
        //boolean delete = f.delete();
        //Assert.assertTrue("Folder '"+f.getAbsolutePath() + "' is Present!", delete);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(appContext) {
        @Override
        // Una volta che OpenCV manager è connesso viene chiamato questo metodo di
        public void onManagerConnected(int status) {
            switch (status) {
                // Una volta che OpenCV manager si è connesso con successo
                // possiamo abilitare l'interazione con la tlc
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
}