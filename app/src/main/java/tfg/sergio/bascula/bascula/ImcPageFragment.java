package tfg.sergio.bascula.bascula;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.IMCCalculator;

/**
 * Created by yeyo on 15/04/2018.
 */

public class ImcPageFragment extends Fragment {
    public ImcPageFragment(){

    }
    private Double peso,altura;
    private Bundle bundle;
    private TextView textIMC;
    private static DecimalFormat df2 = new DecimalFormat(".##");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_imc_page, null);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bundle = getArguments();
        peso = bundle.getDouble("peso");
        altura = bundle.getDouble("altura");
        textIMC = view.findViewById(R.id.txt_imc);
        IMCCalculator imcCalc = new IMCCalculator();
        double imc = IMCCalculator.CalcularIMC(peso,altura);
        textIMC.setText(""+df2.format(imc));
    }

}
