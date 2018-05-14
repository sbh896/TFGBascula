package tfg.sergio.bascula.Calendario;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tfg.sergio.bascula.Models.AdapterAlerta;
import tfg.sergio.bascula.Models.Alerta;
import tfg.sergio.bascula.Models.ElementoListadoAlerta;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.SwipeController;
import tfg.sergio.bascula.Resources.SwipeControllerActions;

/**
 * Created by yeyo on 28/04/2018.
 */

public class CalendarioFragment extends Fragment {

    CompactCalendarView calendar;
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/mm/yyyy", Locale.getDefault());
    private DatabaseReference mDatabaseCentros, mDatabaseRegistros, mDatabasePacientes, mDatabaseAlertas;
    private List<ElementoListadoAlerta> listaAlertas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        return inflater.inflate(R.layout.fragment_calendario, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ActionBar actionbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(false);
        actionbar.setTitle(null);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseRegistros = FirebaseDatabase.getInstance().getReference("registros");
        mDatabasePacientes = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseAlertas = FirebaseDatabase.getInstance().getReference("alertas");


        calendar = view.findViewById(R.id.compactcalendar_view);
        calendar.setUseThreeLetterAbbreviation(true);
        String fecha = "30/04/2018";
        Date evento = null;
        try {
            evento = df.parse(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = evento.getTime();
        Event ev1 = new Event(Color.RED,time,"dia de prueba");
        calendar.addEvent(ev1);
        calendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Context context = getActivity().getApplicationContext();
                //if(dateClicked.toString().compareTo())
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                actionbar.setTitle(formatoFecha.format(firstDayOfNewMonth));
            }
        });

        obtenerAlertas();
    }

    public void obtenerAlertas(){
        listaAlertas.removeAll(listaAlertas);

        final Date[] fechaUltimoRegistro = {null};

        mDatabaseAlertas.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot child: dataSnapshot.getChildren()) {
                    final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");


                    final Alerta alerta = child.getValue(Alerta.class);
                    final String alertaKey = child.getKey();
                    String stralert = formatter.format(alerta.fechaInicio);

                    Date fechaHoy = new Date();
                    if(alerta.periodica == 1){
                        int mesAlerta = alerta.fechaInicio.getMonth();
                        int diaHoy = fechaHoy.getDay();
                        Date fechaAlerta = null;
                        try {
                            fechaAlerta = formatter.parse(stralert);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(fechaAlerta);
                        boolean flag = true;
                        while (flag){
                            Date tempFecha = cal.getTime();
                            if(tempFecha.after(fechaHoy)){
                                flag = false;
                                Event ev1 = new Event(Color.RED,tempFecha.getTime(),"dia de prueba");
                                calendar.addEvent(ev1);
                            }
                            cal.add(Calendar.DATE,7);

                        }

                    }

                    if (alerta.codigoPaciente != null) {
                        Query firebaseSearchQuery = mDatabaseRegistros.orderByChild("StrFecha");//.startAt(search).endAt(search + "\uf8ff");
                        mDatabasePacientes.child(alerta.codigoPaciente).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Paciente pac = dataSnapshot.getValue(Paciente.class);
                                ElementoListadoAlerta ela = new ElementoListadoAlerta();
                                String key = child.getKey();
                                if (pac != null) {
                                    ela.alerta = alerta;
                                    ela.paciente = pac;
                                    if(pac.getUltimoRegistro() != null){
                                        mDatabaseRegistros.child(pac.getUltimoRegistro()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshotR) {
                                                RegistroPaciente rp = dataSnapshotR.getValue(RegistroPaciente.class);
                                                ElementoListadoAlerta ela2 = new ElementoListadoAlerta();
                                                ela2.ultimoRegistro = rp;
                                                ela2.alerta = alerta;
                                                ela2.paciente = pac;
                                                ela2.key = alertaKey;
                                                addElementAlerta(ela2);

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }else{
                                        ela.key = alertaKey;
                                        addElementAlerta(ela);
                                    }
                                }
                                else{
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
            public void addElementAlerta(ElementoListadoAlerta ela){

                if(!listaAlertas.contains(ela)){
                    listaAlertas.add(ela);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
