package fr.johann_web.ARSample;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.johann_web.easyar.core.ARFragment;
import fr.johann_web.easyar.core.ARMarker;
import fr.johann_web.easyar.core.ARView;


/**
 * Created by Johann on 12/02/2015.
 */
public class SampleFragment extends ARFragment implements ARView.OnChangeListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View view =  super.onCreateView(inflater, container, saved);

        setOnChangeListener(this);

        List<String> a = new ArrayList<>();

        addMarker(new ARMarker("", 48.557856, 7.683909, R.layout.sample_overlay));
        addMarker(new ARMarker("", 48.487694, 7.719058, R.layout.sample_overlay));
        addMarker(new ARMarker("", 48.564092, 7.705154, R.layout.sample_overlay, a));

        return view;
    }

    @Override
    protected void onTapMarker(ARMarker marker) {
        Log.i("i", marker.getLocation().toString());

    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void onLocationChange(Location location) {

    }

    @Override
    public void onBearingChange(float bearing) {

    }

    @Override
    public void onOrientationChange(int orientation) {

    }
}
