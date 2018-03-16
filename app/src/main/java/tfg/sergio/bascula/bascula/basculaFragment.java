package tfg.sergio.bascula.bascula;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.PrivateKey;
import java.util.Calendar;
import java.util.Random;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Registro;

/**
 * Created by sergio on 12/03/2018.
 */

public class basculaFragment extends Fragment {
    private Button add;
    private DatabaseReference mDatabase;
    private String key = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bascula, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference("registros");
        Bundle bundle = getArguments();
        key = bundle.getString("key");


        add = view.findViewById(R.id.nuevoPeso);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random rand = new Random();
                Double n = rand.nextDouble() * (20) ;
                Double m = rand.nextDouble() * (1.90);

                final RegistroPaciente regis = new RegistroPaciente(key,n,m, Calendar.getInstance().getTime());

                //Guardado del paciente en Firebase
                String id = mDatabase.push().getKey();
                mDatabase.child(id).setValue(regis);

                FragmentManager fm = getFragmentManager();
                fm.popBackStackImmediate();

            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}
