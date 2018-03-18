package tfg.sergio.bascula.Pacientes;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 25/02/2018.
 */

public class PacientesFragment extends Fragment {
    private RecyclerView listaPacientes;
    private EditText buscador;
    private Spinner mSpinner;
    private ArrayList<Centro> centros = new ArrayList<>();
    private DatabaseReference mDatabaseCentros, mDatabaseRegistros,dbpacientes;
    private Button btnadd;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacientes, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbpacientes = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseRegistros = FirebaseDatabase.getInstance().getReference("registros");
        listaPacientes = (RecyclerView) view.findViewById(R.id.lista_pacientes);
        mSpinner = (Spinner) view.findViewById(R.id.sp_centros);
        btnadd = view.findViewById(R.id.btn_nuevo);


        //listaPacientes.setHasFixedSize(true);
        listaPacientes.setLayoutManager(new GridLayoutManager(this.getActivity(),3));

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

    //region centros
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
    //endregion

    //region pacientes
    private void FireBasePacientesSearch(String search){
       final Centro c = (Centro)mSpinner.getSelectedItem();
        final Date[] fechaUltimoRegistro = {null};


        Query firebaseSearchQuery = dbpacientes.orderByChild("nombre").startAt(search).endAt(search + "\uf8ff");

        FirebaseRecyclerAdapter<Paciente,PacientesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Paciente, PacientesViewHolder>(
                Paciente.class,
                R.layout.pacientes_list_layout,
                PacientesViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(final PacientesViewHolder viewHolder, final Paciente model, int position) {
                //obtenemos el id del paciente en firebase

                if (model == null){
                    return;
                }
                final String paciente_key = getRef(position).getKey();
                if(model.getUltimoRegistro() != null){
                   mDatabaseRegistros.child(model.getUltimoRegistro()).child("fecha").addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           Date fecha =  (Date) dataSnapshot.getValue(Date.class);
                           CallViewHolder(viewHolder,model,fecha,paciente_key,c);

                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });
                }
                CallViewHolder(viewHolder,model,null,paciente_key,c);
            }
        };
        listaPacientes.setAdapter(firebaseRecyclerAdapter);
    }

    public void CallViewHolder(PacientesViewHolder viewHolder, Paciente model, Date fecha, final String paciente_key, Centro centro){

        viewHolder.setDetails(getActivity().getApplicationContext(), model.getNombre(),model.getApellidos(), model.getUrlImagen(), model.getCentro(), centro , fecha);
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack("pacientes");
                Bundle bundle = new Bundle();
                bundle.putString("key",paciente_key);
                DetallePacienteFragment fragment = new DetallePacienteFragment();
                fragment.setArguments(bundle);
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
            }
        });
    }

    public static class PacientesViewHolder extends RecyclerView.ViewHolder
    {
        View mView;


        public PacientesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDetails(Context ctx, String pNombre, String pApellidos,String imagen,String centro, Centro c, Date ultimoRegistro){
            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            TextView paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            TextView paciente_fecha = mView.findViewById(R.id.fecha);

            paciente_nombre.setText(pNombre + " " + pApellidos);

            paciente_fecha.setText(ultimoRegistro == null ? "-":formatter.format(ultimoRegistro));

            //cargar Imagen
            ImageView foto_paciente = (ImageView) mView.findViewById(R.id.iamgen_perfil);
            Picasso.with(ctx).load(imagen).resize(200,200).into(foto_paciente);

            if(c!= null && !centro.equals(c.getId()) && c.getId() != "-1"){
                //Si no pertenece al centro del filtro de búsqueda, se quita la vista de pantalla.l
                mView.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
                params.height = 0;
                mView.setLayoutParams(params);
            }
        }
    }
    //endregion
}
