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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Registro;
import tfg.sergio.bascula.Resources.EnumIMC;
import tfg.sergio.bascula.Resources.EnumIMCFirebase;
import tfg.sergio.bascula.Resources.IMCCalculator;

/**
 * Created by sergio on 12/03/2018.
 */

public class basculaFragment extends Fragment {
    private Button add;
    private DatabaseReference mDatabase, mDatabase2, mDatabaseCentros, mDatabaseDatosMes;
    private String key = "";
    private int estado;
    private String centro;
    private Paciente paciente;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bascula, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference("registros");
        mDatabase2 = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");



        Bundle bundle = getArguments();
        key = bundle.getString("key");
        estado = bundle.getInt("estado");
        centro = bundle.getString("centro");

        add = view.findViewById(R.id.nuevoPeso);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random rand = new Random();
                Double n = rand.nextDouble() * (20) ;
                Double m = rand.nextDouble() * (1.90);

                final RegistroPaciente regis = new RegistroPaciente(key,n,m, Calendar.getInstance().getTime());

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
                        String Key="";
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            System.out.println(child.getKey());
                            pmc = child.getValue(PacientesMesCentro.class);
                            Key = child.getRef().getKey();
                        }

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

                        IMCCalculator imcCalculator = new IMCCalculator();
                        int imcNuevo = imcCalculator.Calcular(paciente.monthsBetweenDates(),regis.getIMC(),0);
                        try {
                            mDatabaseDatosMes.child(Key).child(EnumIMCFirebase.values()[imcNuevo].toString()).setValue(pmc.obtenerNumPacientes(imcNuevo)+1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                FragmentManager fm = getFragmentManager();
                fm.popBackStackImmediate();

            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}
