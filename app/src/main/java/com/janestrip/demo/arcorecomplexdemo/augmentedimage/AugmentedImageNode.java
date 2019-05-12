/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.janestrip.demo.arcorecomplexdemo.augmentedimage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.janestrip.demo.arcorecomplexdemo.R;
import com.janestrip.demo.arcorecomplexdemo.helper.ModelLoader;

import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {
//    implements ModelLoader.ModelLoaderCallbacks

  private static final String TAG = "AugmentedImageNode";

  // The augmented image represented by this node.
  private AugmentedImage image;

  // Models of the 4 corners.  We use completable futures here to simplify
  // the error handling and asynchronous loading.  The loading is started with the
  // first construction of an instance, and then used when the image is set.
  private static CompletableFuture<ModelRenderable> ulCorner;
//  private static CompletableFuture<ModelRenderable> urCorner;
//  private static CompletableFuture<ModelRenderable> lrCorner;
//  private static CompletableFuture<ModelRenderable> llCorner;

  private Context mContext;

  public AugmentedImageNode(Context context){

    mContext = context;

    // Upon construction, start loading the models for the corners of the frame.
    if (ulCorner == null) {
      ulCorner =
          ModelRenderable.builder()
                  .setSource(context, R.raw.andy)
              //.setSource(context, Uri.parse("models/frame_upper_left.sfb"))
              .build();
//      urCorner =
//          ModelRenderable.builder()
//              .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
//              .build();
//      llCorner =
//          ModelRenderable.builder()
//              .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
//              .build();
//      lrCorner =
//          ModelRenderable.builder()
//              .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
//              .build();
    }
  }

  /**
   * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
   * created based on an Anchor created from the image. The corners are then positioned based on the
   * extents of the image. There is no need to worry about world coordinates since everything is
   * relative to the center of the image, which is the parent node of the corners.
   */
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  public void setImage(AugmentedImage image) {
    this.image = image;

    // If any of the models are not loaded, then recurse when all are loaded.
    if (!ulCorner.isDone()){
//    if (!ulCorner.isDone() || !urCorner.isDone() || !llCorner.isDone() || !lrCorner.isDone()) {
//  CompletableFuture.allOf(ulCorner, urCorner, llCorner, lrCorner)
      CompletableFuture.allOf(ulCorner)
          .thenAccept((Void aVoid) -> setImage(image))
          .exceptionally(
              throwable -> {
                Log.e(TAG, "Exception loading", throwable);
                return null;
              });
    }

    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));

    // Make the 4 corner nodes.
    Vector3 localPosition = new Vector3();
    Node cornerNode;

    // Upper left corner.
    localPosition.set(0.0f, 0.0f, 0.0f);

    //localPosition.set(-0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
    cornerNode = new Node();
    cornerNode.setParent(this);
    cornerNode.setLocalPosition(localPosition);
    cornerNode.setRenderable(ulCorner.getNow(null));
//    cornerNode.setLocalRotation();
    Quaternion baseOrientation = Quaternion.axisAngle(new Vector3(1.0f, 0.0f, 0.0f), -90);
    cornerNode.setLocalRotation(baseOrientation);

//    // Upper right corner.
//    localPosition.set(0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(urCorner.getNow(null));
//
//    // Lower right corner.
//    localPosition.set(0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(lrCorner.getNow(null));
//
//    // Lower left corner.
//    localPosition.set(-0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(llCorner.getNow(null));
//
//
//    ModelLoader modelLoader = new ModelLoader(this);
//    modelLoader.loadModel(mContext, R.raw.andy);
  }

  public AugmentedImage getImage() {
    return image;
  }

//
//  private ModelRenderable andyRenderable;
//  // implements ModelLoader.ModelLoaderCallbacks
//  @Override
//  public void setRenderable(ModelRenderable modelRenderable) {
//    andyRenderable = modelRenderable;
//  }
//
//  @Override
//  public void onLoadException(Throwable throwable) {
//    Toast toast = Toast.makeText(mContext, "Unable to load andy renderable", Toast.LENGTH_LONG);
//    toast.setGravity(Gravity.CENTER, 0, 0);
//    toast.show();
//    Log.e(TAG, "Unable to load andy renderable", throwable);
//  }
}
