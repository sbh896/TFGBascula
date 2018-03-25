package tfg.sergio.bascula.Pacientes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.List;

import tfg.sergio.bascula.Models.AdapterPaciente;
import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.ElementoListadoPaciente;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.EnumIMC;
import tfg.sergio.bascula.Resources.IMCCalculator;

/**
 * Created by yeyo on 25/02/2018.
 */

public class PacientesFragment extends Fragment {
    private RecyclerView listaPacientes;
    private EditText buscador;
    private Spinner sp_centros, sp_imc;
    private ArrayList<Centro> centros = new ArrayList<>();
    private DatabaseReference mDatabaseCentros, mDatabaseRegistros,dbpacientes;
    private ImageButton btnadd;

    RecyclerView.Adapter adapter;
    List<ElementoListadoPaciente> elementos = new ArrayList<>();


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
        sp_imc = view.findViewById(R.id.sp_estado_imc);
        sp_centros = (Spinner) view.findViewById(R.id.sp_centros);
        btnadd = view.findViewById(R.id.btn_nuevo);
        btnadd.bringToFront();


        //listaPacientes.setHasFixedSize(true);
        listaPacientes.setLayoutManager(new GridLayoutManager(this.getActivity(),3));
        sp_imc.setAdapter(new ArrayAdapter<EnumIMC>(getActivity(), android.R.layout.simple_spinner_dropdown_item, EnumIMC.values()));


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

//        sp_centros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                FireBasePacientesSearch("");
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

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

        sp_centros.setAdapter(arrayAdapter);
        mDatabaseCentros.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Centro c= dataSnapshot.getValue(Centro.class);
                String key = dataSnapshot.getKey();
                centros.add(c);
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
       final Centro c = (Centro) sp_centros.getSelectedItem();
       final int estado = ((EnumIMC) sp_imc.getSelectedItem()).getId();
       elementos.removeAll(elementos);

        final Date[] fechaUltimoRegistro = {null};


        Query firebaseSearchQuery = dbpacientes.orderByChild("nombre").startAt(search).endAt(search + "\uf8ff");

        adapter = new AdapterPaciente(elementos, getActivity());

        listaPacientes.setAdapter(adapter);

        firebaseSearchQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Paciente paciente = dataSnapshot.getValue(Paciente.class);
                final String paciente_key = dataSnapshot.getKey();

                ElementoListadoPaciente elp = new ElementoListadoPaciente();
                final RegistroPaciente regis;
                elp.paciente=paciente;
                if(paciente.getUltimoRegistro() != null){

                   mDatabaseRegistros.child(paciente.getUltimoRegistro()).addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           RegistroPaciente reg = dataSnapshot.getValue(RegistroPaciente.class);
                           if(reg != null){
                               IMCCalculator imcCalculator = new IMCCalculator();
                               int IMC = IMCCalculator.Calcular(paciente.monthsBetweenDates(),reg.getIMC(),0);
                           }
                               addElement(paciente,reg, paciente_key);
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });
                }
                else if (paciente.getUltimoRegistro()==null){
                    addElement(paciente,null, paciente_key);
                }
            }

            public void addElement(Paciente pac , RegistroPaciente reg, String key){
            if(c!= null && !pac.getCentro().equals(c.Id) && c.Id != "-1"){
                //Si no pertenece al centro del filtro de búsqueda, se quita
                return;
               // mView.setLayoutParams(params);
            }
                ElementoListadoPaciente elp = new ElementoListadoPaciente();
                elp.paciente = pac;
                elp.registroPaciente = reg;
                elp.key = key;
                if(!elementos.contains(elp)){
                    elementos.add(elp);
                    adapter.notifyDataSetChanged();
                }

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


//        FirebaseRecyclerAdapter<Paciente,PacientesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Paciente, PacientesViewHolder>(
//                Paciente.class,
//                R.layout.pacientes_list_layout,
//                PacientesViewHolder.class,
//                firebaseSearchQuery
//        ) {
//            @Override
//            protected void populateViewHolder(final PacientesViewHolder viewHolder, final Paciente model, int position) {
//                //obtenemos el id del paciente en firebase
//
//                if (model == null){
//                    return;
//                }
//                final String paciente_key = getRef(position).getKey();
//                if(model.getUltimoRegistro() != null){
//                   mDatabaseRegistros.child(model.getUltimoRegistro()).addListenerForSingleValueEvent(new ValueEventListener() {
//                       @Override
//                       public void onDataChange(DataSnapshot dataSnapshot) {
//                           RegistroPaciente reg = dataSnapshot.getValue(RegistroPaciente.class);
//                           Date fecha =  reg.getFecha();
//                           IMCCalculator imcCalculator = new IMCCalculator();
//                           int IMC = IMCCalculator.Calcular(model.monthsBetweenDates(),reg.getIMC(),0);
//
//                           CallViewHolder(viewHolder,model,fecha,paciente_key,c, IMC);
//
//                       }
//
//                       @Override
//                       public void onCancelled(DatabaseError databaseError) {
//
//                       }
//                   });
//                }
//                CallViewHolder(viewHolder,model,null,paciente_key,c,0);
//            }
//        };
//        listaPacientes.setAdapter(firebaseRecyclerAdapter);
    }

//    public void CallViewHolder(PacientesViewHolder viewHolder, Paciente model, Date fecha, final String paciente_key, Centro centro, int estado){
//
//        viewHolder.setDetails(getActivity().getApplicationContext(), model.getNombre(),model.getApellidos(), model.getUrlImagen(), model.getCentro(), centro , fecha, estado);
//        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager fm = getFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.addToBackStack("pacientes");
//                Bundle bundle = new Bundle();
//                bundle.putString("key",paciente_key);
//                DetallePacienteFragment fragment = new DetallePacienteFragment();
//                fragment.setArguments(bundle);
//                ft.replace(R.id.pacientes_screen,fragment);
//                ft.commit();
//            }
//        });
//    }

//    public static class PacientesViewHolder extends RecyclerView.ViewHolder
//    {
//        View mView;
//
//
//        public PacientesViewHolder(View itemView) {
//            super(itemView);
//            mView = itemView;
//        }
//
//        public void setDetails(Context ctx, String pNombre, String pApellidos,String imagen,String centro, Centro c, Date ultimoRegistro, int estado){
//            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
//            TextView paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
//            TextView paciente_fecha = mView.findViewById(R.id.fecha);
//            ImageView estadoPaciente = mView.findViewById(R.id.imagen_estado);
//            GradientDrawable bgShape = (GradientDrawable)estadoPaciente.getDrawable();
//            Resources res = ctx.getResources();
//            switch (estado){
//                case 0:
//                    bgShape.setColor(Color.RED);
//                    break;
//                case 1:
//                    bgShape.setColor(res.getColor(R.color.OrangeRed));
//                    break;
//                case 2:
//                    bgShape.setColor(res.getColor(R.color.Green));
//                    break;
//                case 3:
//                    bgShape.setColor(res.getColor(R.color.LightGreen));
//                    break;
//                case 4:
//                    bgShape.setColor(res.getColor(R.color.OrangeRed));
//                    break;
//                case 5:
//                    bgShape.setColor(Color.RED);
//                    break;
//
//            }
//            bgShape.setColor(Color.BLACK);
//
//
//            paciente_nombre.setText(pNombre + " " + pApellidos);
//
//            paciente_fecha.setText(ultimoRegistro == null ? "-":formatter.format(ultimoRegistro));
//
//            //cargar Imagen
//            ImageView foto_paciente = (ImageView) mView.findViewById(R.id.iamgen_perfil);
//            Picasso.with(ctx).load(imagen).resize(200,200).into(foto_paciente);
//
//            if(c!= null && !centro.equals(c.getId()) && c.getId() != "-1"){
//                //Si no pertenece al centro del filtro de búsqueda, se quita la vista de pantalla.l
//                mView.setVisibility(View.GONE);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
//                params.height = 0;
//               // mView.setLayoutParams(params);
//            }
//        }
//    }
    //endregion
}
