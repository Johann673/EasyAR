package fr.johann_web.easyar.core;

import android.location.Location;

/**
 * Created by Johann on 12/02/2015.
 */
public class ARMarker<T> {
    private Location mLocation;
    private int mLayout;
    private String mName;
    private boolean mVisible;
    private float mDistance = -1;
    private T mData;

    public ARMarker(String name, double lat, double lng, int layout){
        mName = name;
        mLocation = new Location("Point");
        mLocation.setLatitude(lat);
        mLocation.setLongitude(lng);
        mLayout = layout;
    }

    public ARMarker(String name, double lat, double lng, int layout, T data) {
        this(name, lat, lng, layout);
        this.mData = data;
    }

    public Location getLocation(){
        return mLocation;
    }

    public void setDistance(float distance){
        this.mDistance = distance;
    }

    public float getDistance(){
        return mDistance;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setVisible(boolean visible) {
        this.mVisible = visible;
    }

    public int getLayout() {
        return mLayout;
    }

    public void setLayout(int layout) {
        this.mLayout = layout;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public T getData() {
        return mData;
    }

    public void setData(T mData) {
        this.mData = mData;
    }
}
