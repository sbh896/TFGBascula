package tfg.sergio.bascula.bascula;

import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.EnumIMCFirebase;
import tfg.sergio.bascula.Resources.IMCCalculator;

/**
 * Created by yeyo on 15/04/2018.
 */

public class basculaResultFragment extends Fragment{
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {

    };
    private Bundle bundle;
    private String key = "";
    private int estado;
    private String centro, nombre;
    private Double peso,altura;
    private DatabaseReference mDatabase, mDatabase2, mDatabaseCentros, mDatabaseDatosMes;
    private Paciente paciente;
    private Button aceptar,cancelar;
    private TextView textIMC,textPeso;
    private static DecimalFormat df2 = new DecimalFormat(".##");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result_bascula, null);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        aceptar = view.findViewById(R.id.btn_aceptar);
        cancelar = view.findViewById(R.id.btn_cancelar);
//
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//
        addTabs();
//
//        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(viewPager);
        mDatabase = FirebaseDatabase.getInstance().getReference("registros");
        mDatabase2 = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");
        textIMC = view.findViewById(R.id.txt_imc);
        textPeso = view.findViewById(R.id.txt_peso);
        bundle = getArguments();
        key = bundle.getString("key");
        estado = bundle.getInt("estado");
        centro = bundle.getString("centro");
        nombre = bundle.getString("nombre");
        peso = bundle.getDouble("peso");
        altura = bundle.getDouble("altura");

        IMCCalculator imcCalc = new IMCCalculator();
        double imc = IMCCalculator.CalcularIMC(peso,altura);
        textIMC.setText(""+df2.format(imc));
        textPeso.setText(""+df2.format(peso) + " Kg");
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");


    }
    private void addTabs() {

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RegistroPaciente regis = new RegistroPaciente(key,peso,altura, Calendar.getInstance().getTime());

                //Guardado del registro en Firebase
                final String id = mDatabase.push().getKey();
                mDatabase.child(id).setValue(regis);

                //Actualización de referencia de último registro de paciente
                try {
                    mDatabase2.child(key).child("ultimoRegistro").setValue(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDatabase2.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        paciente = dataSnapshot.getValue(Paciente.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                DateFormat dateFormat = new SimpleDateFormat("YYYYMM");
                Date date = new Date();
                final String ident = centro +dateFormat.format(date);
                Query firebaseSearchQuery = mDatabaseDatosMes.orderByChild("id").startAt(ident).endAt(ident);

                //actualización recuento por centros
                firebaseSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        PacientesMesCentro pmc = null;
                        IMCCalculator imcCalculator = new IMCCalculator();
                        int imcNuevo = imcCalculator.Calcular(paciente.monthsBetweenDates(),regis.getIMC(),0);
                        String Key="";
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            System.out.println(child.getKey());
                            pmc = child.getValue(PacientesMesCentro.class);
                            Key = child.getRef().getKey();
                        }
                        if(pmc != null){
                            if(pmc.obtenerNumPacientes(estado) == 0){

                            }
                            else{
                                try {
                                    mDatabaseDatosMes.child(Key).child(EnumIMCFirebase.values()[estado].toString()).setValue(pmc.obtenerNumPacientes(estado)-1);
                                    pmc.ActualizarNumPacientes(estado,0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                            try {
                                mDatabaseDatosMes.child(Key).child(EnumIMCFirebase.values()[imcNuevo].toString()).setValue(pmc.obtenerNumPacientes(imcNuevo)+1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            String id2 = mDatabaseDatosMes.push().getKey();
                            PacientesMesCentro newpmc= new PacientesMesCentro(ident);
                            newpmc.ActualizarNumPacientes(imcNuevo,1);
                            mDatabaseDatosMes.child(id2).setValue(newpmc);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                getFragmentManager().popBackStackImmediate();

            }
        });

    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            Bundle bnd = getArguments();
            Bundle myBundle = new Bundle();
            myBundle.putDouble( "peso", bnd.getDouble("peso"));
            myBundle.putDouble("altura", bnd.getDouble("altura"));
            fragment.setArguments( myBundle  );
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

