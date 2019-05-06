package com.janestrip.demo.arcorecomplexdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.janestrip.demo.arcorecomplexdemo.helper.ARCoreHelper;
import com.janestrip.demo.arcorecomplexdemo.helper.ModelLoader;


public class SceneformActivity extends AppCompatActivity
        implements ModelLoader.ModelLoaderCallbacks {

    private static final String TAG = SceneformActivity.class.getSimpleName();

    private ArFragment arFragment;
    private ModelRenderable myRenderable;

    private ModelLoader modelLoader;

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
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

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
                    ball.select();
                });
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

}