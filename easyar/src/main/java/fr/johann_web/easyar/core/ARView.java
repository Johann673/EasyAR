package fr.johann_web.easyar.core;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stuart on 11/06/2013.
 */
public class ARView extends SurfaceView implements SensorEventListener, LocationListener, AROverlay.OnTapListener {

    private int saveHeight;
    private int saveWidth;

    private Context context;
    private SensorManager mSensorManager;
    private LocationManager locationManager;
    private Camera camera;
    private boolean inPreview = false;
    private boolean cameraConfigured = false;
    private boolean sensorStarted = false;
    private boolean loaded = false;

    private boolean fixedOrientation = false;

    private int Orientation = -1;

    private boolean show_compass = false;
    private Bitmap compass_inside, compass_outside;

    private AROverlayCanvas foreground = null;
    private SurfaceHolder previewHolder = null;

    private float[] rotationMatrix = new float[16];
    private float[] orientation = new float[3];
    private float geomag[] = new float[3];
    private float gravity[] = new float[3];

    private Location mylocation;

    private float screenScaleX;
    private float screenScaleY;

    private float widthDegrees;
    private float heightDegrees;

    private boolean cameraOn = true;

    private List<AROverlay> overlays = new ArrayList<AROverlay>();

    // Listener
    private OnChangeListener listener = null;

    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    public interface OnChangeListener{
        public void onLoaded();
        public void onLocationChange(Location location);
        public void onBearingChange(float bearing);
        public void onOrientationChange(int orientation);
    }

    private OnTapMarkerListener tapMarkerListener = null;

    public void setOnTapMarkerListener(OnTapMarkerListener listener) {
        this.tapMarkerListener = listener;
    }

    public interface OnTapMarkerListener {
        public void tapMarker(ARMarker marker);
    }

    public ARView(Context context) {
        super(context);
        init(context);
    }

    public ARView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        this.context = context;

        setWillNotDraw(false);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        previewHolder = getHolder();
        previewHolder.addCallback(surfaceCallback);

    }

    public boolean onStartGPS(){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        Location l = null;
        for (int i = 0; i < providers.size(); i++) {
            l = locationManager.getLastKnownLocation(providers.get(i));
            if (l != null) {
                mylocation = l;
                break;
            }
        }
        boolean GPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(GPSenabled){
            String provider = LocationManager.GPS_PROVIDER;
            locationManager.requestLocationUpdates(provider, 1000, 10, this);
        }
        return GPSenabled;
    }

    public void onStopGPS(){
        locationManager.removeUpdates(this);
    }

    public void onStart() {


        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME );
        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME );
        if(cameraOn){

            safeCameraOpen();

            if(cameraConfigured){
                initPreview(saveWidth, saveHeight);
                startPreview();
            }
        }
    }

    public void onPause() {

        mSensorManager.unregisterListener(this);

        if(cameraOn && inPreview){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        Orientation = -1;
        inPreview = false;
    }

    @Override
    protected void onDraw(Canvas canvas){

        if(foreground == null || !sensorStarted){
            return;
        }

        // Compass bearing
        float compassBearing = getCompassBearing();

        if(listener != null){
            listener.onBearingChange(compassBearing);
        }

        // Phone tilt
        float yTilt = gravity[2] * 9f;

        // Draw Compass
        if(show_compass){

            Paint compass_paint = new Paint();
            if(compass_inside == null || compass_outside == null){
                compass_paint.setColor(Color.RED);
                compass_paint.setTextSize(30);
                canvas.drawText(Float.toString(compassBearing), 20, 40, compass_paint);
            }else{
                canvas.drawBitmap(compass_outside, 10, 10, compass_paint);
                canvas.save();
                canvas.rotate(compassBearing, 10 + compass_outside.getWidth()/2, 10 + compass_outside.getWidth()/2);
                canvas.drawBitmap(compass_inside, 10, 10, compass_paint);
                canvas.restore();
            }
        }
        // -----

        if(mylocation == null){
            return;
        }

        if(cameraOn && camera == null){
            return;
        }else if(!cameraOn){
            // Screen degrees - good idea to look at improving this
            widthDegrees = 35;
            heightDegrees = 20;
        }else{
            // degree width of camera
            Camera.Parameters parameters = camera.getParameters();
            widthDegrees = parameters.getHorizontalViewAngle();
            heightDegrees = parameters.getVerticalViewAngle();
        }

        // pixel to degree
        screenScaleX = (canvas.getWidth() / widthDegrees);
        screenScaleY = (canvas.getHeight() / heightDegrees);


        if(!loaded){
            loaded = true;
            if(listener != null){
                listener.onLoaded();
            }
        }

        for (int i = 0; i < overlays.size(); i ++){
            ARMarker marker = overlays.get(i).getMarker();

            double Xangle = ModSym(compassBearing - mylocation.bearingTo(marker.getLocation()));
            marker.setDistance(mylocation.distanceTo(marker.getLocation()));
            // Currently unable to work out y offset due to altitude differences because of altitude measurement error
            double Yangle = yTilt - 0;

            // Check for overlays on screen
            if((Math.abs(Xangle) < widthDegrees/2) && (Math.abs(Yangle) < heightDegrees/2)){

                float drawLocationX = (float) ((canvas.getWidth()/2) - (Xangle * screenScaleX));
                float drawLocationY = (float) ((canvas.getHeight()/2) - (Yangle * screenScaleY));

                overlays.get(i).draw((int)drawLocationX, (int)drawLocationY, foreground);
            }else{
                overlays.get(i).close();
            }
        }
    }

    private double ModSym(float angle){

        angle = (float) (angle - (360 * Math.floor(angle/360)));
        if (angle > 180){
            angle = angle - 360;
        }
        return angle;
    }

    // Location listener
    @Override
    public void onLocationChanged(Location location) {
        if(listener != null){
            listener.onLocationChange(location);
        }
        mylocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    // Sensor data
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int type = sensorEvent.sensor.getType();

        //Smoothing the sensor data a bit

        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomag[0] = (geomag[0] * 0.9f) + (0.1f * sensorEvent.values[0]);
            geomag[1] = (geomag[1] * 0.9f) + (0.1f * sensorEvent.values[1]);
            geomag[2] = (geomag[2] * 0.9f) + (0.1f * sensorEvent.values[2]);
        } else if (type == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = (gravity[0] * 0.9f) + (0.1f * sensorEvent.values[0]);
            gravity[1] = (gravity[1] * 0.9f) + (0.1f * sensorEvent.values[1]);
            gravity[2] = (gravity[2] * 0.9f) + (0.1f * sensorEvent.values[2]);
        }

        sensorStarted = true;
        checkOrination(gravity);
        // Re-draw
        invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void checkOrination(float[] value){

        double exact_rotation = Math.atan2(gravity[1], gravity[0]) * 57f;

        int rotation = (((int)(Math.floor((exact_rotation + 45) / 90))) + 4) % 4;

        if(Orientation != rotation){
            setOrientation(rotation);
        }

    }

    // Camera code

    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            camera = Camera.open();
            qOpened = (camera != null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                }
                else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return(result);
    }

    private void initPreview(int width, int height) {
        if (camera != null && previewHolder.getSurface() != null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e("PreviewDem",
                        "Exception in setPreviewDisplay()", t);

            }

            if (!cameraConfigured) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height,
                        parameters);

                if (size!=null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured = true;
                }
            }
        }
    }

    private void startPreview() {
        if (cameraConfigured && camera!=null) {
            camera.startPreview();
            inPreview = true;
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            saveWidth = width;
            saveHeight = height;
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    // Gettings and Setters

    public void fixOrientation(int Orientation){
        setOrientation(Orientation);
        fixedOrientation = true;
    }

    public void unFixOrientation(){
        fixedOrientation = false;
    }

    public void setOrientation(int Orientation){

        this.Orientation = Orientation;
        if(listener != null){
            listener.onOrientationChange(Orientation);
        }
        if(fixedOrientation){
            return;
        }

        if(camera != null){
            if(Orientation == Surface.ROTATION_0){
                camera.setDisplayOrientation(0);
            }else if(Orientation == Surface.ROTATION_90){
                camera.setDisplayOrientation(90);
            }else if(Orientation == Surface.ROTATION_180){
                camera.setDisplayOrientation(180);
            }else if(Orientation == Surface.ROTATION_270){
                camera.setDisplayOrientation(270);
            }
        }
    }

    public void setCamera(boolean cameraOn){
        this.cameraOn = cameraOn;
        if(cameraOn){
            setVisibility(View.VISIBLE);
        }else{
            setVisibility(View.INVISIBLE);
        }
    }

    public void setForeground(AROverlayCanvas foreground){
        this.foreground = foreground;
    }

    /**
     * Adds a compass to the top left corner of the screen
     * @param inside resources id of drawable for the turning element of the compass
     * @param outside resources id of drawable for the static element of the compass
     */
    public void setComapss(int inside, int outside){
        Resources res = context.getResources();
        compass_inside = BitmapFactory.decodeResource(res, inside);
        compass_outside = BitmapFactory.decodeResource(res, outside);
        show_compass = true;
    }

    public boolean showingCompass(){
        return show_compass;
    }

    public void addOverlay(AROverlay overlay){
        overlay.setOnTapListener(this);
        overlays.add(overlay);
    }

    @Override
    public void onTap(ARMarker marker) {
        if(this.tapMarkerListener != null)
            this.tapMarkerListener.tapMarker(marker);
    }

    public void removeOverlay(AROverlay overlay){
        overlays.remove(overlay);
        overlay.close();
    }

    public void removeAllOverlays() {
        for(AROverlay ov : overlays) {
            ov.close();
        }
        overlays = new ArrayList<>();
    }

    public void setOverlays(List<AROverlay> overlays){
        this.overlays = overlays;
    }

    public boolean isLoaded(){
        return loaded;
    }

    public int getOrientation(){
        return Orientation;
    }

    public float getCompassBearing(){
        rotationMatrix = new float[9];
        SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomag);
        orientation = new float[3];

        if(Orientation == Surface.ROTATION_0){
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_Z, rotationMatrix);
        }else if(Orientation == Surface.ROTATION_90){
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix);
        }else if(Orientation == Surface.ROTATION_180){
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_Z, rotationMatrix);
        }else if(Orientation == Surface.ROTATION_270){
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix);
        }

        SensorManager.getOrientation(rotationMatrix, orientation);
        double compassBearing = (orientation[0] * 360 / (2 * Math.PI));

        return (float)compassBearing;
    }


}