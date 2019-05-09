/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.opencvpreview;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;

import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends PluginActivity implements CvCameraViewListener2 {

    private static final String TAG = "Plug-in::MainActivity";

    private ThetaView mOpenCvCameraView;
    private boolean isEnded = false;
    private Mat mOutputFrame;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "OpenCV version: " + Core.VERSION);

        // Set a callback when a button operation event is acquired.
        setKeyCallback(new KeyCallback() {
            @Override
            public void onKeyDown(int keyCode, KeyEvent keyEvent) {

            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent keyEvent) {

            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) {
                    Log.d(TAG, "Do end process.");
                    closeCamera();
                }
            }
        });

        notificationCameraClose();

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (ThetaView) findViewById(R.id.opencv_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found.");
        } else {
            Log.d(TAG, "OpenCV library found inside package.");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }

    public void onCameraViewStarted(int width, int height) {
        mOutputFrame = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mOutputFrame.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // get a frame by rgba() or gray()
        Mat gray = inputFrame.gray();

        // do some image processing
        Imgproc.threshold(gray, mOutputFrame, 127.0, 255.0, Imgproc.THRESH_BINARY);

        return mOutputFrame;
    }

    private void closeCamera() {
        if (isEnded) {
            return;
        }
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        close();
        isEnded = true;
    }
}
