package tfg.sergio.bascula.Resources;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import tfg.sergio.bascula.R;



/**
 * Created by yeyo on 17/03/2018.
 */

public class CustomMarkerView extends MarkerView {
    private TextView tvContent;
    private TextView prueba;
    private float posx;
    private float posy;

    public CustomMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        prueba = findViewById(R.id.text2);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        prueba.setText("Peso: " + e.getY());
        posx = e.getX();
        posy = e.getY();
    }


    @Override
    public MPPointF getOffset() {
        MPPointF ret = new MPPointF(-getWidth()/2,-getHeight());
        return ret;
    }
}
