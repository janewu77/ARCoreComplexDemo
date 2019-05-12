package com.janestrip.demo.arcorecomplexdemo.augmentedimage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;
import com.janestrip.demo.arcorecomplexdemo.R;
import com.janestrip.demo.common.SnackbarHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class AugmentedImageActivity extends AppCompatActivity {

    private static final String TAG = "AugmentedImageActivity";

    private ArFragment arFragment;
    private ImageView fitToScanView;

    private HashMap<String,Integer> mdoelMaps = null;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmentedimage);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        mdoelMaps = new HashMap<String, Integer>();
        mdoelMaps.put("default.jpg",R.raw.andy);    //default: andy
        mdoelMaps.put("f.jpg",R.raw.ball);    //flower : ball

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);


        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {

                        String imagename = augmentedImage.getName();
                        Log.d(TAG, "onUpdateFrame: image name: "+imagename);

                        Integer modelRID = mdoelMaps.get(imagename);
                        AugmentedImageNode node = new AugmentedImageNode(this,modelRID);
                        node.setImage(augmentedImage);

                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);
                    }
                    break;

                case STOPPED:

                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }
}
