package tfg.sergio.bascula.bascula;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 15/04/2018.
 */

public class PesoPageFragment extends Fragment {
    public PesoPageFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_peso_page, null);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }
}
