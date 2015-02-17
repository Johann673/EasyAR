package fr.johann_web.easyar.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Stuart on 11/06/2013.
 */
public class AROverlay {

    private ARMarker mMarker;
    private View mView;
    private OnTapListener mListener = null;

    public void setOnTapListener(OnTapListener listener) {
        this.mListener = listener;
    }
    public interface OnTapListener{
        public void onTap(ARMarker marker);
    }


    public AROverlay(Context context, ARMarker marker){
        mMarker = marker;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(marker.getLayout(), null, false);

        mView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(mListener != null){
                    mListener.onTap(AROverlay.this.mMarker);
                }
                return false;
            }
        });
    }

    public void draw(int x, int y, AROverlayCanvas parent){

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.leftMargin = x - mView.getWidth()/2;
        lp.topMargin = y - mView.getHeight()/2;

        close();


            parent.addView(mView, lp);


        mView.setVisibility(View.VISIBLE);
        mMarker.setVisible(true);

    }

    public void close() {
        if (mMarker.isVisible()) {
            mMarker.setVisible(false);
            ((ViewGroup)mView.getParent()).removeView(mView);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  AROverlay) {
            AROverlay ov = (AROverlay) o;
            if(ov.getMarker() == this.getMarker()) {
                return true;
            }
        }
        return super.equals(o);
    }

    public ARMarker getMarker() {
        return this.mMarker;
    }


}
