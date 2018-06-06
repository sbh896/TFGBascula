package tfg.sergio.bascula.bascula;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Random;

import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.Silla;
import tfg.sergio.bascula.Pacientes.PacientesFragment;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.FixedDatePicker;

/**
 * Created by yeyo on 06/06/2018.
 */

public class AniadirSillaFragment extends Fragment {
    private EditText inputModelo, inputPeso;
    private Button inputGuardar;

    //almacenamiento firebase
    private DatabaseReference mDatabaseSillas, mDatabasePacientes;

    private ProgressDialog progreso;
    private AlertDialog.Builder builder;
    private Paciente pacienteOriginal;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        return inflater.inflate(R.layout.fragment_aniadir_silla, null);    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        pacienteOriginal = bundle.getParcelable("paciente");
        mDatabaseSillas = FirebaseDatabase.getInstance().getReference("silla");
        mDatabasePacientes = FirebaseDatabase.getInstance().getReference("pacientes");

        inputModelo = (EditText) view.findViewById(R.id.modelo);
        inputPeso = view.findViewById(R.id.btn_peso_silla);
        inputGuardar =  view.findViewById(R.id.btn_guardar_silla);

        inputPeso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pesar silla");
        builder.setMessage("Por favor coloque la silla en la báscula y pulse aceptar");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Random r = new Random();
                int Low = 50;
                int High = 100;
                int Result = r.nextInt(High-Low) + Low;

                inputPeso.setText(Result + "");
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });


        inputGuardar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String Modelo = String.valueOf(inputModelo.getText());
                int peso = 0;
                peso = Integer.parseInt(String.valueOf(inputPeso.getText()));

                if(Modelo == null || peso == 0){
                    return;
                }
                else{
                    Silla silla = new Silla();
                    silla.peso = (float)peso;
                    silla.modelo = Modelo;
                    String id = mDatabaseSillas.push().getKey();
                    pacienteOriginal.setCodigoSilla(id);
                    mDatabaseSillas.child(pacienteOriginal.getCodigoSilla()).removeValue();
                    mDatabaseSillas.child(id).setValue(silla);

                    mDatabasePacientes.child(pacienteOriginal.getId()).setValue(pacienteOriginal);
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack();

                }
            }
        });

    }
}
