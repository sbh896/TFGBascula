package tfg.sergio.bascula.bascula;

import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Wave;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.Pacientes.AniadirPacienteFragment;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Registro;
import tfg.sergio.bascula.Resources.EnumIMC;
import tfg.sergio.bascula.Resources.EnumIMCFirebase;
import tfg.sergio.bascula.Resources.IMCCalculator;

/**
 * Created by sergio on 12/03/2018.
 */

public class basculaFragment extends Fragment implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    private static final int MY_DATA_CHECK_CODE = 1;
    private ImageButton add;
    private DatabaseReference mDatabase, mDatabase2, mDatabaseCentros, mDatabaseDatosMes;
    private String key = "";
    private int estado;
    private String centro, nombre;
    private Paciente paciente;
    private TextToSpeech mTts;
    private Handler viewHandler;
    private Bundle bundle;

    ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        viewHandler = new Handler();
        return inflater.inflate(R.layout.fragment_bascula, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference("registros");
        mDatabase2 = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");
        progressBar = view.findViewById(R.id.spin_kit);
        Wave wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);


        bundle = getArguments();
        key = bundle.getString("key");
        estado = bundle.getInt("estado");
        centro = bundle.getString("centro");
        nombre = bundle.getString("nombre");

        add = view.findViewById(R.id.nuevoPeso);


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random rand = new Random();
                Double n = rand.nextDouble() * (20) ;
                Double m = rand.nextDouble() * (1.90);

                final RegistroPaciente regis = new RegistroPaciente(key,n,m, Calendar.getInstance().getTime());
//
//                //Guardado del registro en Firebase
//                final String id = mDatabase.push().getKey();
//                mDatabase.child(id).setValue(regis);
//
//                //Actualización de referencia de último registro de paciente
//                try {
//                    mDatabase2.child(key).child("ultimoRegistro").setValue(id);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                mDatabase2.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        paciente = dataSnapshot.getValue(Paciente.class);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//                DateFormat dateFormat = new SimpleDateFormat("YYYYMM");
//                Date date = new Date();
//                final String ident = centro +dateFormat.format(date);
//                Query firebaseSearchQuery = mDatabaseDatosMes.orderByChild("id").startAt(ident).endAt(ident);
//
//                //actualización recuento por centros
//                firebaseSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        PacientesMesCentro pmc = null;
//                        String Key="";
//                        for (DataSnapshot child: dataSnapshot.getChildren()) {
//                            System.out.println(child.getKey());
//                            pmc = child.getValue(PacientesMesCentro.class);
//                            Key = child.getRef().getKey();
//                        }
//
//                        if(pmc.obtenerNumPacientes(estado) == 0){
//
//                        }
//                        else{
//                            try {
//                                mDatabaseDatosMes.child(Key).child(EnumIMCFirebase.values()[estado].toString()).setValue(pmc.obtenerNumPacientes(estado)-1);
//                                pmc.ActualizarNumPacientes(estado,0);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//
//                        IMCCalculator imcCalculator = new IMCCalculator();
//                        int imcNuevo = imcCalculator.Calcular(paciente.monthsBetweenDates(),regis.getIMC(),0);
//                        try {
//                            mDatabaseDatosMes.child(Key).child(EnumIMCFirebase.values()[imcNuevo].toString()).setValue(pmc.obtenerNumPacientes(imcNuevo)+1);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
                progressBar.setVisibility(View.INVISIBLE);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack("bascula");
                bundle.putDouble("peso",n);
                bundle.putDouble("altura",m);
                basculaResultFragment fragment = new basculaResultFragment();
                fragment.setArguments(bundle);
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
                Toast.makeText(getActivity(), "new one", Toast.LENGTH_SHORT).show();


            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(getActivity(), this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

    }

    @Override
    public void onInit(int i) {
        mTts.setLanguage(new Locale(Locale.getDefault().getLanguage()));
        mTts.setOnUtteranceCompletedListener(this);

        String text = "Hola " + nombre+ ", es hora de pesarte";
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
        viewHandler.post(run);
    }
}
