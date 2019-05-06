package com.janestrip.demo.arcorecomplexdemo;

import android.content.ContentValues;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.janestrip.demo.arcorecomplexdemo.helper.ARCoreHelper;
import com.janestrip.demo.arcorecomplexdemo.helper.ARVideoRecorder;
import com.janestrip.demo.arcorecomplexdemo.helper.ModelLoader;
import com.janestrip.demo.arcorecomplexdemo.helper.WritingArFragment;


public class SceneformActivity extends AppCompatActivity
        implements ModelLoader.ModelLoaderCallbacks {

    private static final String TAG = SceneformActivity.class.getSimpleName();

    private WritingArFragment arFragment;
    private ModelRenderable myRenderable;

    private ModelLoader modelLoader;

    private FloatingActionButton recordButton;
    // VideoRecorder encapsulates all the video recording functionality.
    private ARVideoRecorder videoRecorder;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ARCoreHelper.checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ar);
        arFragment = (WritingArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        //load model
        modelLoader = new ModelLoader(this);
        modelLoader.loadModel(this, R.raw.ball);

//      arFragment.getPlaneDiscoveryController().hide();
//      arFragment.getPlaneDiscoveryController().setInstructionView(null);
//      arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (myRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode ball = new TransformableNode(arFragment.getTransformationSystem());
                    ball.setParent(anchorNode);
                    ball.setRenderable(myRenderable);
//                    ball.setLocalScale(new Vector3(0.1f,0.1f,0.1f));
//                    ball.setWorldScale(new Vector3(0.1f,0.1f,0.1f));
                    ball.select();
                });


        // Initialize the VideoRecorder.
        videoRecorder = new ARVideoRecorder();
        int orientation = getResources().getConfiguration().orientation;
        videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation);
        videoRecorder.setSceneView(arFragment.getArSceneView());

        recordButton = findViewById(R.id.record);
        recordButton.setOnClickListener(this::toggleRecording);
        recordButton.setEnabled(true);
        recordButton.setImageResource(R.drawable.round_videocam);
    }


    // implements ModelLoader.ModelLoaderCallbacks
    @Override
    public void setRenderable(ModelRenderable modelRenderable) {
        myRenderable = modelRenderable;
    }

    @Override
    public void onLoadException(Throwable throwable) {
        Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        Log.e(TAG, "Unable to load andy renderable", throwable);
    }


    //for recorder
    /*
     * Used as a handler for onClick, so the signature must match onClickListener.
     */
    private void toggleRecording(View unusedView) {
        if (!arFragment.hasWritePermission()) {
            Log.e(TAG, "Video recording requires the WRITE_EXTERNAL_STORAGE permission");
            Toast.makeText(
                    this,
                    "Video recording requires the WRITE_EXTERNAL_STORAGE permission",
                    Toast.LENGTH_LONG)
                    .show();
            arFragment.launchPermissionSettings();
            return;
        }

        boolean recording = videoRecorder.onToggleRecord();
        if (recording) {
            recordButton.setImageResource(R.drawable.round_stop);
        } else {
            recordButton.setImageResource(R.drawable.round_videocam);
            String videoPath = videoRecorder.getVideoPath().getAbsolutePath();
            Toast.makeText(this, "Video saved: " + videoPath, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + videoPath);

            // Send  notification of updated content.
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video");
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.DATA, videoPath);
            getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

}