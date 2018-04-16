package tfg.sergio.bascula.bascula;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 15/04/2018.
 */

public class PesoPageFragment extends Fragment {
    public PesoPageFragment(){

    }
    private Double peso,altura;
    private Bundle bundle;
    private TextView textPeso;
    private static DecimalFormat df2 = new DecimalFormat(".##");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_peso_page, null);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bundle = getArguments();
        peso = bundle.getDouble("peso");
        altura = bundle.getDouble("altura");
        textPeso = view.findViewById(R.id.txt_peso);

        textPeso.setText(""+df2.format(peso)+ " Kg");
    }
}
