package com.janestrip.demo.arcorecomplexdemo.arlocation;


import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.janestrip.demo.arcorecomplexdemo.R;
import com.janestrip.demo.arcorecomplexdemo.helper.ARCoreHelper;
import com.janestrip.demo.arcorecomplexdemo.helper.ModelLoader;
import com.janestrip.demo.common.LonLatUtils;


import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;


/*
* 在中间放一个andy, 在andy周围1KM放一圈fence(目前也是andy)
*
*
* */
public class ARLocationActivity extends AppCompatActivity
        implements ModelLoader.ModelLoaderCallbacks {

    private static final String TAG = ARLocationActivity.class.getSimpleName();

    protected Activity mContext = this;

    private ArFragment arFragment;

    private boolean hasFinishedLoading = false;
    private ModelLoader modelLoader;
    private ModelRenderable andyRenderable;

    //private FloatingActionButton btnAddModel;

    private boolean installRequested;
    ArSceneView mArSceneView;
    // Astronomical units to meters ratio. Used for positioning the planets of the solar system.
    //private static final float AU_TO_METERS = 0.5f;
    private LocationScene locationScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ARCoreHelper.checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_arlocation);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);

        //load model
        modelLoader = new ModelLoader(this);
        modelLoader.loadModel(this, R.raw.andy);


        mArSceneView = arFragment.getArSceneView();
        mArSceneView.getScene()
                .addOnUpdateListener(new Scene.OnUpdateListener() {
                    //.addOnUpdateListener(new Scene.OnUpdateListener() {
                    @Override
                    public void onUpdate(FrameTime frameTime) {
                        if (!hasFinishedLoading) {
                            return;
                        }

                        if (locationScene == null) {
                            // If our locationScene object hasn't been setup yet, this is a good time to do it
                            // We know that here, the AR components have been initiated.
                            locationScene = new LocationScene(mContext, mArSceneView);
                            locationScene.setAnchorRefreshInterval(1000 * 20); // 1000sec * 60 * 60 = 1 hour

                            //取得当前位置
//                            DeviceLocation deviceLocation = new DeviceLocation(mContext,null);
//                            deviceLocation.startUpdatingLocation();

                            //中心点
                            double lon = 121.4134274;
                            double lat = 31.2212379;

                            //fences：取得半径1KM的gps形表
                            for(int i= 0; i<360 ; i+= 15) {
                                double[] lonlat = LonLatUtils.ConvertDistanceToLogLat(lon, lat, 1, i);
                                System.out.println("nearby gps : "+i + ":" + lonlat[1] + "," + lonlat[0]);

                                LocationMarker locMarker =  new LocationMarker(
                                        lonlat[0],
                                        lonlat[1],
                                        getFendceModel());

                                //locMarker.setScalingMode(LocationMarker.ScalingMode.NO_SCALING);

                                //locMarker.setScalingMode(LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN);
                                locationScene.mLocationMarkers.add(locMarker);
                            }

                            //中心点
                            LocationMarker layoutLocationMarker = new LocationMarker(
                                    lon, lat, getAndyModel()
                            );
                            locationScene.mLocationMarkers.add(layoutLocationMarker);

                        }

                        Frame frame = mArSceneView.getArFrame();
                        if (frame == null) {
                            return;
                        }

                        if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                            return;
                        }

                        if (locationScene != null) {
                            locationScene.processFrame(frame);
                        }

                    }
                });



//        //button : for
//        btnAddModel = findViewById(R.id.button);
//        btnAddModel.setOnClickListener(this::clickedBtnAddModel);
//        btnAddModel.setEnabled(false);
//        btnAddModel.setImageResource(R.drawable.round_videocam);
//        //recordButton.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mArSceneView == null) {
            return;
        }
        if (locationScene != null) {
            locationScene.resume();
        }


        if (mArSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = ARCoreHelper.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = ARCoreHelper.hasCameraPermission(this);
                    return;
                } else {
                    mArSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                //ArUtils.handleSessionException(this, e);
                Log.e(TAG, "onResume: UnavailableException",e );
            }
        }

        try {
            mArSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            Log.e(TAG, "onResume: Unable to get camera", ex);
            //ArUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (mArSceneView.getSession() != null) {
            //showLoadingMessage();
            Log.d(TAG, "onResume: .....");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationScene != null) {
            locationScene.pause();
        }

        if (mArSceneView != null) {
            mArSceneView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mArSceneView != null) {
            mArSceneView.destroy();
        }
    }


//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            // Standard Android full-screen functionality.
//            getWindow()
//                    .getDecorView()
//                    .setSystemUiVisibility(
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
//    }


    // implements ModelLoader.ModelLoaderCallbacks
    @Override
    public void setRenderable(ModelRenderable modelRenderable) {
        andyRenderable = modelRenderable;

        hasFinishedLoading = true;
        //enable button
        //btnAddModel.setEnabled(true);
    }

    @Override
    public void onLoadException(Throwable throwable) {
        Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        Log.e(TAG, "Unable to load andy renderable", throwable);
    }


    //第一个andy出现在屏幕正中间（远方）
    //第二个：在第一个的右边；第三个：在第一个的上在。
    //注意andy是否站着/横着
    private void clickedBtnAddModel_1(View unusedView) {

        if (andyRenderable == null) {
            return;
        }

        ArSceneView arSceneView = arFragment.getArSceneView();
        Session session = arSceneView.getSession();
        Camera camera = arSceneView.getArFrame().getCamera();
        Pose cameraPose = camera.getPose();

        // Create the Anchor.
        Anchor anchor_new = session.createAnchor(cameraPose);
        AnchorNode anchorNode = new AnchorNode(anchor_new);
        anchorNode.setParent(arSceneView.getScene());

        // Create the transformable andy and add it to the anchor.
        //TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
        Node andy = new Node();
        andy.setParent(anchorNode);
        andy.setRenderable(andyRenderable);
        //andy.setLocalPosition(new Vector3(0,0,-1));
        andy.setLocalPosition(Vector3.forward()); //正面

        //默认时：手机竖着时，andy是横着面向手机的。
        //将andy竖起转90度，与手机纵向相同（即andy沿Z转正转90度）
        Quaternion orientation = Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 1.0f), 90);
        andy.setLocalRotation(orientation);

//        andy.setLookDirection(Vector3.up());

        //正右方
        Node andyx = new Node();
        andyx.setParent(andy);
        andyx.setRenderable(andyRenderable);
        //andyx.setLocalPosition(new Vector3(1,0,0));
        andyx.setLocalPosition(Vector3.right());

        //正上方
        Node andyy = new Node();
        andyy.setParent(andy);
        andyy.setRenderable(andyRenderable);
        //andyy.setLocalPosition(new Vector3(0,1,0));
        andyy.setLocalPosition(Vector3.up());


//        //让andy与手机朝向相同(背对手机屏幕）
//        Vector3 cameraPosition = anchorNode.getScene().getCamera().getWorldPosition();
//        Vector3 cardPosition = andy.getWorldPosition();
//        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
//        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
//        andy.setWorldRotation(lookRotation);

    }




    private Node getAndyModel() {
        Node andy = new Node();
        andy.setRenderable(andyRenderable);
        //Quaternion orientation = Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 1.0f), 90);
        //andy.setLocalRotation(orientation);
        //ball.setLocalScale(new Vector3(0.05f, 0.05f, 0.05f));
        return andy;
    }

    private Node getFendceModel() {
        Node ball = new Node();
        ball.setRenderable(andyRenderable);

        //ball.setLocalScale(new Vector3(0.05f, 0.05f, 0.05f));
        return ball;
    }
}
