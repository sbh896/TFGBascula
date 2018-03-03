package tfg.sergio.bascula;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import tfg.sergio.bascula.Models.Paciente;

/**
 * Created by sergio on 27/02/2018.
 */

public class AniadirPacienteFragment extends Fragment {
    private EditText inputNombre,inputApellido;
    DatabaseReference DBPacientes;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aniadir_paciente, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DBPacientes = FirebaseDatabase.getInstance().getReference("pacientes");
        inputNombre = (EditText) view.findViewById(R.id.lista_pacientes);
        inputApellido = (EditText) view.findViewById(R.id.apellidos);
        view.findViewById(R.id.btn_guardar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = inputNombre.getText().toString();
                String apellidos = inputApellido.getText().toString();

                if (TextUtils.isEmpty(nombre)) {
                    Toast.makeText(getActivity(), "Introduzca un nombre.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(apellidos)) {
                    Toast.makeText(getActivity(), "Introduzca los apellidos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String id = DBPacientes.push().getKey();
                Paciente paciente = new Paciente(nombre,apellidos,id);
                DBPacientes.child(id).setValue(paciente);
                Toast.makeText(getActivity(), "añadido usuario",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
