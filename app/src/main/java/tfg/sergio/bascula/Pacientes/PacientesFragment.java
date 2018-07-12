package tfg.sergio.bascula.Pacientes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tfg.sergio.bascula.Adapters.AdapterPaciente;
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
    private Spinner sp_centros, sp_imc, sp_genero, sp_dieta;
    private ArrayList<Centro> centros = new ArrayList<>();
    private DatabaseReference mDatabaseCentros, mDatabaseRegistros,dbpacientes;
    private ImageButton btnadd, btnSearch;


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
        sp_genero = view.findViewById(R.id.sp_genero);
        sp_dieta = view.findViewById(R.id.sp_dieta);
        btnadd = view.findViewById(R.id.btn_nuevo);
        btnSearch = view.findViewById(R.id.btn_buscar);
        btnadd.bringToFront();


        //listaPacientes.setHasFixedSize(true);
        listaPacientes.setLayoutManager(new GridLayoutManager(this.getActivity(),3));
        sp_imc.setAdapter(new ArrayAdapter<EnumIMC>(getActivity(), android.R.layout.simple_spinner_dropdown_item, EnumIMC.values()));
        sp_imc.setSelection(6);

        this.FireBasePacientesSearch("");
        this.obtenerCentros();

        buscador = (EditText) view.findViewById(R.id.buscador);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FireBasePacientesSearch(buscador.getText().toString());
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

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                return view;
            }


        };
        centros.add(new Centro("-1","Centro"));
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
       final int genero = sp_genero.getSelectedItemPosition();
       final int dieta = sp_dieta.getSelectedItemPosition();
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
                if(paciente.getCodigoUltimoRegistro() != null){

                   mDatabaseRegistros.child(paciente.getCodigoUltimoRegistro()).addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           RegistroPaciente reg = dataSnapshot.getValue(RegistroPaciente.class);
                           if(reg != null){
                               IMCCalculator imcCalculator = new IMCCalculator();
                               int IMC = reg.getCodigoEstadoIMC();
                               if(IMC != estado && estado != -1){

                               }else if((estado != -1 && estado == IMC) || (estado == -1)){
                                   addElement(paciente,reg, paciente_key);
                               }
                           }else{
                               addElement(paciente,reg, paciente_key);
                           }
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });
                }
                else if (paciente.getCodigoUltimoRegistro()==null){
                    addElement(paciente,null, paciente_key);
                }
            }

            public void addElement(Paciente pac , RegistroPaciente reg, String key){
                if(c!= null && !pac.getCodigoCentro().equals(c.Id) && c.Id != "-1"){
                    //Si no pertenece al centro del filtro de búsqueda, se quita
                    return;
                }
                else if(genero != 0 && pac.getSexo() != genero){
                    return;
                }
                else if( dieta != 0 && pac.getTipoDieta() != dieta){
                    return;
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
    }


    //endregion
}
