package fr.johann_web.easyar.core;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Stuart on 14/06/2013.
 */
public class AROverlayCanvas extends RelativeLayout {


    public AROverlayCanvas(Context context) {
        super(context);
    }

    public AROverlayCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }
}
