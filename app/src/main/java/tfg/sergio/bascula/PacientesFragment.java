package tfg.sergio.bascula;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import tfg.sergio.bascula.Models.Paciente;

/**
 * Created by yeyo on 25/02/2018.
 */

public class PacientesFragment extends Fragment {
    DatabaseReference DBPacientes;
    private RecyclerView listaPacientes;
    private EditText buscador;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacientes, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DBPacientes = FirebaseDatabase.getInstance().getReference("pacientes");
        listaPacientes = (RecyclerView) view.findViewById(R.id.lista_pacientes);
        //listaPacientes.setHasFixedSize(true);
        listaPacientes.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        this.FireBasePacientesSearch("");

        buscador = (EditText) view.findViewById(R.id.buscador);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                FireBasePacientesSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        view.findViewById(R.id.btn_nuevo).setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                Fragment fragment = new AniadirPacienteFragment();
                //AniadirPacienteFragment fragment = new AniadirPacienteFragment();
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();

            }
        });
    }
    private void FireBasePacientesSearch(String search){

        Query firebaseSearchQuery = DBPacientes.orderByChild("nombre").startAt(search).endAt(search + "\uf8ff");

        FirebaseRecyclerAdapter<Paciente,PacientesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Paciente, PacientesViewHolder>(
                Paciente.class,
                R.layout.pacientes_list_layout,
                PacientesViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(PacientesViewHolder viewHolder, Paciente model, int position) {
                viewHolder.setDetails(getActivity().getApplicationContext(), model.getNombre(),model.getApellidos());
            }
        };
        listaPacientes.setAdapter(firebaseRecyclerAdapter);
    }
    public static class PacientesViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PacientesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDetails(Context ctx, String pNombre, String pApellidos){
            TextView paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            TextView paciente_ape = (TextView) mView.findViewById(R.id.apellidos);
            paciente_nombre.setText(pNombre);
            paciente_ape.setText(pApellidos);

        }
    }
}
