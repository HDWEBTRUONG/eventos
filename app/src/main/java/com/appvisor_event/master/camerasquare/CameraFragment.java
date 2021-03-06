package com.appvisor_event.master.camerasquare;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appvisor_event.master.ImageActivity;
import com.appvisor_event.master.R;
import com.appvisor_event.master.modules.AppLanguage.AppLanguage;

import java.io.IOException;
import java.util.List;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {

    public static final String TAG = CameraFragment.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_mode";
    public static final String IMAGE_INFO = "image_info";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int mCameraID;
    private String mFlashMode;
    private Camera mCamera;
    private SquareCameraPreview mPreviewView;
    private SurfaceHolder mSurfaceHolder;
    private boolean status=true;

    private boolean mIsSafeToTakePhoto = false;

    private ImageParameters mImageParameters;

    private CameraOrientationListener mOrientationListener;


    private LinearLayout mlayout_camera_bottom;

    public static Fragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOrientationListener = new CameraOrientationListener(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore your state here because a double rotation with this fragment
        // in the backstack will cause improper state restoration
        // onCreate() -> onSavedInstanceState() instead of going through onCreateView()
        if (savedInstanceState == null) {
            mCameraID = getBackCameraID();
            mFlashMode = CameraSettingPreferences.getCameraFlashMode(getActivity());
            mImageParameters = new ImageParameters();
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashMode = savedInstanceState.getString(CAMERA_FLASH_KEY);
            mImageParameters = savedInstanceState.getParcelable(IMAGE_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_squarecamera_camera, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mOrientationListener==null){
            mOrientationListener=new CameraOrientationListener(getActivity());
        }
        mOrientationListener.enable();

        mPreviewView = (SquareCameraPreview) view.findViewById(R.id.camera_preview_view);
        mPreviewView.getHolder().addCallback(CameraFragment.this);
        mPreviewView.setBackgroundColor(Color.BLACK);
        mPreviewView.post(new Runnable() {
            @Override
            public void run() {
                Log.e("lytest", "preview  width:" + mPreviewView.getWidth() + "\n" + "height:" + mPreviewView.getHeight());
                Log.e("lytest", "allheight:" + ImageUtility.getScreenHeight(getActivity()));
            }
        });

        final View topCoverView = view.findViewById(R.id.cover_top_view);
        final View btnCoverView = view.findViewById(R.id.cover_bottom_view);

        if (mImageParameters == null) {
            mImageParameters = new ImageParameters();
        }
        mImageParameters.mIsPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (savedInstanceState == null) {
            ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageParameters.mPreviewWidth = mPreviewView.getWidth();
                    mImageParameters.mPreviewHeight = mPreviewView.getHeight();

                    mImageParameters.mCoverWidth = mImageParameters.mCoverHeight
                            = mImageParameters.calculateCoverWidthHeight();

//                    Log.d(TAG, "parameters: " + mImageParameters.getStringValues());
//                    Log.d(TAG, "cover height " + topCoverView.getHeight());
                    resizeTopAndBtmCover(topCoverView, btnCoverView);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {
            if (mImageParameters.isPortrait()) {
                topCoverView.getLayoutParams().height = mImageParameters.mCoverHeight;
                btnCoverView.getLayoutParams().height = mImageParameters.mCoverHeight;
            } else {
                topCoverView.getLayoutParams().width = mImageParameters.mCoverWidth;
                btnCoverView.getLayoutParams().width = mImageParameters.mCoverWidth;
            }
        }

        final ImageView swapCameraBtn = (ImageView) view.findViewById(R.id.change_camera);
        swapCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraID = getBackCameraID();

                } else {
                    mCameraID = getFrontCameraID();
                }
                restartPreview();
            }
        });

        final ImageView takePhotoBtn = (ImageView) view.findViewById(R.id.capture_image_button);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        view.findViewById(R.id.imgbtn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mPreviewView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putString(CAMERA_FLASH_KEY, mFlashMode);
        outState.putParcelable(IMAGE_INFO, mImageParameters);
        super.onSaveInstanceState(outState);
    }

    private void resizeTopAndBtmCover(final View topCover, final View bottomCover) {
        ResizeAnimation resizeTopAnimation
                = new ResizeAnimation(topCover, mImageParameters);
        resizeTopAnimation.setDuration(800);
        resizeTopAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        topCover.startAnimation(resizeTopAnimation);

        ResizeAnimation resizeBtmAnimation
                = new ResizeAnimation(bottomCover, mImageParameters);
        resizeBtmAnimation.setDuration(800);
        resizeBtmAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        bottomCover.startAnimation(resizeBtmAnimation);
    }

    private void getCamera(int cameraID) {
        try {
            mCamera = Camera.open(cameraID);
            mPreviewView.setCamera(mCamera);
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
        }
    }

    /**
     * Restart the camera preview
     */
    private void restartPreview() {
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
            Log.d("camera", "restart----" + mCamera);
        }

        getCamera(mCameraID);
        startCameraPreview();
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();

            setSafeToTakePhoto(true);
            setCameraFocusReady(true);
        } catch (IOException e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        setSafeToTakePhoto(false);
        setCameraFocusReady(false);

        // Nulls out callbacks, stops face detection
        mCamera.stopPreview();
        mPreviewView.setCamera(null);
    }

    private void setSafeToTakePhoto(final boolean isSafeToTakePhoto) {
        mIsSafeToTakePhoto = isSafeToTakePhoto;
    }

    private void setCameraFocusReady(final boolean isFocusReady) {
        if (this.mPreviewView != null) {
            mPreviewView.setIsFocusReady(isFocusReady);
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        // Clockwise rotation needed to align the window display to the natural position
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // CameraInfo.Orientation is the angle relative to the natural position of the device
        // in clockwise rotation (angle that is rotated clockwise from the natural position)
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mImageParameters.mDisplayOrientation = displayOrientation;
        mImageParameters.mLayoutOrientation = degrees;
        Log.d("camera",displayOrientation+"----"+mImageParameters.mDisplayOrientation+"----"+mCamera);
        if (mCamera!=null){
            mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);
        }else {
            startCameraPreview();
        }
    }

    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        final View changeCameraFlashModeBtn = getView().findViewById(R.id.flash);
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
            changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
        } else {
            changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }

    private Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;
        Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
//        return CameraInfo.CAMERA_FACING_BACK;
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.d("numberOfCameras", "numberOfCameras" + numberOfCameras);
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * Take a picture
     */
    private void takePicture() {

        if (mIsSafeToTakePhoto) {
            setSafeToTakePhoto(false);

            mOrientationListener.rememberOrientation();

            // Shutter callback occurs after the image is captured. This can
            // be used to trigger a sound to let the user know that image is taken
            Camera.ShutterCallback shutterCallback = null;

            // Raw callback occurs when the raw image data is available
            Camera.PictureCallback raw = null;

            // postView callback occurs when a scaled, fully processed
            // postView image is available.
            Camera.PictureCallback postView = null;

            // jpeg callback occurs when the compressed image is available
            mCamera.takePicture(shutterCallback, raw, postView, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!status){
            if (mCamera == null) {
                restartPreview();
                status=false;
            }
        }
    }


    @Override
    public void onStop() {
        mOrientationListener.disable();
        // stop the preview
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            Log.d("camera", "stop----" + mCamera);
            mCamera = null;
        }

        CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashMode);

        super.onStop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;

        getCamera(mCameraID);
        startCameraPreview();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case 1:
                Uri imageUri = data.getData();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A picture has been taken
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        setRotation(getPhotoRotation());
        setDataPic(data);
//        Log.d(TAG, "normal orientation: " + orientation);
//        Log.d(TAG, "Rotate Picture by: " + rotation);
//        getFragmentManager()
//                .beginTransaction()
//                .replace(
//                        R.id.fragment_container,
//                        EditSavePhotoFragment.newInstance(data, rotation, mImageParameters.createCopy()),
//                        EditSavePhotoFragment.TAG)
//                .addToBackStack(null)
//                .commit();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE);
            int checkCallPhonePermission2 = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED && checkCallPhonePermission2 != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_SAVE_PIC);
                return;
            } else {
                dialogshow();
            }

        } else {
//            String imgurl = ImageUtility.savePicture(getActivity(), ImageUtility.rotatePicture(getActivity(), getRotation(), data));
//            Log.e("imgurl", imgurl);
//            setSafeToTakePhoto(true);
//            Intent intent = new Intent(getActivity(), ImageActivity.class);
//            intent.putExtra("image_url", imgurl);
//            getActivity().startActivity(intent);
            dialogshow();
        }
    }

    private int getPhotoRotation() {
        int rotation;
        int orientation = mOrientationListener.getRememberedNormalOrientation();
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(mCameraID, info);

        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }

        return rotation;
    }

    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        /**
         * @param degrees Amount of clockwise rotation from the device's natural position
         * @return Normalized degrees to just 0, 90, 180, 270
         */
        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            rememberOrientation();
            return mRememberedNormalOrientation;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_SAVE_PIC) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                String imgurl = ImageUtility.savePicture(getActivity(), ImageUtility.rotatePicture(getActivity(), getRotation(), getDataPic()));
//                Log.e("imgurl", imgurl);
//
//                setSafeToTakePhoto(true);
//                Intent intent = new Intent(getActivity(), ImageActivity.class);
//                intent.putExtra("image_url", imgurl);
//                getActivity().startActivity(intent);
                dialogshow();
            } else {
                // Permission Denied
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private final int MY_PERMISSIONS_REQUEST_SAVE_PIC = 2;

    private int rotation;
    private byte[] dataPic;

    public byte[] getDataPic() {
        return dataPic;
    }

    public void setDataPic(byte[] dataPic) {
        this.dataPic = dataPic;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    private void dialogshow() {
        String content = "";
        String ok = "";
        String cancel = "";

        if (AppLanguage.getLanguageWithStringValue(getActivity()).equals("ja")) {
            content = getActivity().getResources().getString(R.string.camera_sucess_dialog_jp);
            ok = getActivity().getResources().getString(R.string.camera_sucess_ok_jp);
            cancel = getActivity().getResources().getString(R.string.camera_sucess_cancel_jp);

        } else {
            content = getActivity().getResources().getString(R.string.camera_sucess_dialog_en);
            ok = getActivity().getResources().getString(R.string.camera_sucess_ok_en);
            cancel = getActivity().getResources().getString(R.string.camera_sucess_cancel_en);
        }
        new AlertDialog.Builder(getActivity()).setMessage(content)
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String imgurl = ImageUtility.savePicture(getActivity(), ImageUtility.rotatePicture(getActivity(), getRotation(), getDataPic()));
                                Log.e("imgurl", imgurl);
                                setSafeToTakePhoto(true);
                                Intent intent = new Intent(getActivity(), ImageActivity.class);
                                intent.putExtra("image_url", imgurl);
//                                getActivity().startActivity(intent);
                                getActivity().startActivityForResult(intent, CameraSquareActivity.RESULT_FINISH);
                            }
                        }

                )
                .setNegativeButton(cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                restartCamera2();
                            }
                        }

                ).setCancelable(false)
                .show();
    }

    public void restartCamera2() {
        mOrientationListener.disable();
        // stop the preview
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
            Log.d("camera", "----" + mCamera);
        }
        CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashMode);
        if (mCamera == null) {
            restartPreview();
        }
    }
}
