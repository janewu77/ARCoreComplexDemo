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
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.janestrip.demo.arcorecomplexdemo.helper.ARCoreHelper;
import com.janestrip.demo.arcorecomplexdemo.recorder.ARVideoRecorder;
import com.janestrip.demo.arcorecomplexdemo.helper.ModelLoader;
import com.janestrip.demo.arcorecomplexdemo.solarsystem.SolarHelper;
import com.janestrip.demo.arcorecomplexdemo.solarsystem.SolarSettings;
import com.janestrip.demo.arcorecomplexdemo.helper.WritingArFragment;

import java.util.function.Consumer;


public class SceneformActivity extends AppCompatActivity
        implements ModelLoader.ModelLoaderCallbacks {

    private static final String TAG = SceneformActivity.class.getSimpleName();

    private WritingArFragment arFragment;
    private ModelRenderable andyRenderable;
    private ModelRenderable ballRenderable;

    private ModelLoader modelLoader;
//    private ModelLoader modelLoader_ball;

    private FloatingActionButton recordButton;
    // VideoRecorder encapsulates all the video recording functionality.
    private ARVideoRecorder videoRecorder;

//    private final SolarSettings solarSettings = new SolarSettings();


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

//        arFragment.getArSceneView();
        //load model: andy
        modelLoader = new ModelLoader(this);
        modelLoader.loadModel(this, R.raw.andy);

        //load model: ball
        ModelRenderable.builder()
                .setSource(this, R.raw.ball)
                .build()
                .thenAccept(new Consumer<ModelRenderable>() {
                    @Override
                    public void accept(ModelRenderable renderable) {
                        ballRenderable = renderable;
                    }
                })
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load ball renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });



//      arFragment.getPlaneDiscoveryController().hide();
//      arFragment.getPlaneDiscoveryController().setInstructionView(null);
//      arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    if (ballRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    //TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    Node andy = new Node();
                    andy.setParent(anchorNode);
                    andy.setRenderable(ballRenderable);
                    andy.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
                    //andy.select();

                    SolarSettings solarSettings = new SolarSettings();
                    SolarHelper.createPlanet(this,solarSettings,"ball", andy, 0.5f, 35f, andyRenderable, 2.0f, 20.64f);
                    //createPlanet("Ball", andy, 0.4f, 47f, andyRenderable, 0.019f, 0.03f);
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
        andyRenderable = modelRenderable;
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