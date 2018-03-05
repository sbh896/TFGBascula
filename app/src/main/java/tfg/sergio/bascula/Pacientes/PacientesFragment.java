package tfg.sergio.bascula.Pacientes;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 25/02/2018.
 */

public class PacientesFragment extends Fragment {
    DatabaseReference DBPacientes;
    private RecyclerView listaPacientes;
    private EditText buscador;

    private Spinner mSpinner;
    private ArrayList<Centro> centros = new ArrayList<>();
    private DatabaseReference mDatabaseCentros;


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
        mSpinner = (Spinner) view.findViewById(R.id.sp_centros);
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");


        //listaPacientes.setHasFixedSize(true);
        listaPacientes.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        this.FireBasePacientesSearch("");
        this.obtenerCentros();

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
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack("pacientes");
                AniadirPacienteFragment fragment = new AniadirPacienteFragment();
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
                Toast.makeText(getActivity(), "new one", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void obtenerCentros(){
        ArrayAdapter<Centro> arrayAdapter = new ArrayAdapter<Centro>(getActivity(),android.R.layout.simple_spinner_dropdown_item,centros){
            @Override
            public boolean isEnabled(int position) {
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;

            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }


        };
        centros.add(new Centro("-1","Seleccionar centro..."));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(arrayAdapter);
        mDatabaseCentros.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                centros.add(new Centro(key,value));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FireBasePacientesSearch(String search){
       final Centro c = (Centro)mSpinner.getSelectedItem();

        Query firebaseSearchQuery = DBPacientes.orderByChild("nombre").startAt(search).endAt(search + "\uf8ff");

        FirebaseRecyclerAdapter<Paciente,PacientesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Paciente, PacientesViewHolder>(
                Paciente.class,
                R.layout.pacientes_list_layout,
                PacientesViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(PacientesViewHolder viewHolder, Paciente model, int position) {
                //obtenemos el id del paciente en firebase

                final String paciente_key = getRef(position).getKey();
                viewHolder.setDetails(getActivity().getApplicationContext(), model.getNombre(),model.getApellidos(), model.getUrlImagen(), model.getCentro(), c);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

//                        Toast.makeText(getActivity(), paciente_key, Toast.LENGTH_SHORT).show();
                    }
                });
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

        public void setDetails(Context ctx, String pNombre, String pApellidos,String imagen,String centro, Centro c){
            TextView paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            TextView paciente_ape = (TextView) mView.findViewById(R.id.apellidos);
            TextView paciente_centro = (TextView) mView.findViewById(R.id.centro);
            paciente_nombre.setText(pNombre);
            paciente_ape.setText(pApellidos);
            paciente_centro.setText(centro);

            //cargar Imagen
            ImageView foto_paciente = (ImageView) mView.findViewById(R.id.iamgen_perfil);
            Picasso.with(ctx).load(imagen).into(foto_paciente);

            if(c!= null && !centro.equals(c.getId())){
                //Si no pertenece al centro del filtro de búsqueda, se quita la vista de pantalla.l
                mView.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
                params.height = 0;
                mView.setLayoutParams(params);

            }
        }
    }
}
