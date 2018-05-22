package tfg.sergio.bascula.Resources;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Registro;


/**
 * Created by yeyo on 17/03/2018.
 */

public class CustomMarkerView extends MarkerView {
    private TextView tvContent;
    private TextView prueba;
    private float posx;
    private ImageView imagenPeso;
    private float posy;
    private ArrayList<RegistroPaciente> registros = new ArrayList<>();

    public CustomMarkerView (Context context, int layoutResource, ArrayList<RegistroPaciente> registros ){
        super(context, layoutResource);
        // this markerview only displays a textview
        prueba = findViewById(R.id.text2);
        imagenPeso = findViewById(R.id.imagen_peso);
        this.registros = registros;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        prueba.setText("Peso: " + e.getY());
        RegistroPaciente aux = null;
        for(RegistroPaciente reg : registros){
            if((int)reg.getFecha().getTime()/1000 == (int)e.getX()){
                aux = reg;
            }
        }

        if(aux != null && aux.getUrlFoto() != null){
            Picasso.with(this.getContext()).load(aux.getUrlFoto()).resize(150,150).into(imagenPeso);
        }


        posx = e.getX();
        posy = e.getY();

    }


    @Override
    public MPPointF getOffset() {
        MPPointF ret = new MPPointF(-getWidth()/2,-getHeight());
        return ret;
    }
}
