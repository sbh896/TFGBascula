package tfg.sergio.bascula.Calendario;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import tfg.sergio.bascula.Adapters.AdapterAlertaCalendadrio;
import tfg.sergio.bascula.Models.Alerta;
import tfg.sergio.bascula.Models.ElementoListadoAlerta;
import tfg.sergio.bascula.Models.MesesAnno;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;

/**
 * Created by Sergio Barrado on 28/04/2018.
 */

public class CalendarioFragment extends Fragment {

    CompactCalendarView calendar;
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/mm/yyyy", Locale.getDefault());
    private DatabaseReference mDatabaseCentros, mDatabaseRegistros, mDatabasePacientes, mDatabaseAlertas;
    private List<ElementoListadoAlerta> listaAlertas = new ArrayList<>();
    private RecyclerView recyclerAlertas;
    private List<ElementoListadoAlerta> listaAlertaasAux = new ArrayList<>();
    private RecyclerView.Adapter adapterAlertas;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
    private TextView tituloCalendario, alertaSinEventos;

    MesesAnno meses = new MesesAnno();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_calendario, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
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
        recyclerAlertas = view.findViewById(R.id.lista_alertas);
        recyclerAlertas.setLayoutManager(new GridLayoutManager(this.getActivity(),1));
        tituloCalendario = view.findViewById(R.id.title_calendario);
        alertaSinEventos = view.findViewById(R.id.no_eventos);
        adapterAlertas = new AdapterAlertaCalendadrio(listaAlertaasAux, this.getActivity());
        recyclerAlertas.setAdapter(adapterAlertas);
        tituloCalendario.setText(meses.mesesAnio.get(new Date().getMonth()).Nombre);

        calendar = view.findViewById(R.id.compactcalendar_view);
        calendar.setUseThreeLetterAbbreviation(true);


        calendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Context context = getActivity().getApplicationContext();
                listaAlertaasAux.clear();
                for (ElementoListadoAlerta ela : listaAlertas)
                {
                    String stralert = formatter.format(ela.alerta.fechaInicio);

                    Date fechaHoy = new Date();
                    Date fechaAlerta = null;
                    try {
                     fechaAlerta = formatter.parse(stralert);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(ela.alerta.periodica == 2 && fechaAlerta.getDate() == dateClicked.getDate()){
                        listaAlertaasAux.add(ela);
                    }
                    else if(ela.alerta.periodica == 1){
                        boolean flag = true;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(fechaAlerta);
                        while (flag){
                            Date tempFecha = cal.getTime();
                            if(tempFecha.getMonth() == dateClicked.getMonth() && tempFecha.getDate() == dateClicked.getDate() && tempFecha.getYear() == dateClicked.getYear()){
                                listaAlertaasAux.add(ela);
                            }
                            if(tempFecha.getMonth() > dateClicked.getMonth()){
                                flag = false;
                            }
                            cal.add(Calendar.DATE,7);
                        }
                    }
                    else if(ela.alerta.periodica == 0){
                        listaAlertaasAux.add(ela);
                    }
                }
                if(listaAlertaasAux.size() == 0){
                    alertaSinEventos.setVisibility(View.VISIBLE);
                }else{
                    alertaSinEventos.setVisibility(View.GONE);
                }
                adapterAlertas.notifyDataSetChanged();

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                tituloCalendario.setText(meses.mesesAnio.get(firstDayOfNewMonth.getMonth()).Nombre);
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


                    final Alerta alerta = child.getValue(Alerta.class);
                    final String alertaKey = child.getKey();
                    String stralert = formatter.format(alerta.fechaInicio);
                    Date fechaHoy = new Date();
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
                    if(alerta.periodica == 1){
                        boolean flag = true;
                        while (flag){
                            Date tempFecha = cal.getTime();
                            Event ev1 = new Event(Color.RED,tempFecha.getTime(),"dia de prueba");
                            calendar.addEvent(ev1);
                            if(tempFecha.getMonth() > fechaHoy.getMonth()){
                                flag = false;
                            }
                            cal.add(Calendar.DATE,7);
                        }
                    }
                    else if(alerta.periodica == 2){
                        boolean flag = true;
                        while (flag){
                            Date tempFecha = cal.getTime();
                            if(tempFecha.after(fechaHoy)){
                                flag = false;
                                Event ev1 = new Event(Color.RED,tempFecha.getTime(),"dia de prueba");
                                calendar.addEvent(ev1);
                            }
                            cal.add(Calendar.MONTH,1);
                        }
                    }
                    else if (alerta.periodica == 0){
                        Event ev1 = new Event(Color.RED,cal.getTime().getTime(),"dia de prueba");
                        calendar.addEvent(ev1);

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
                                    if(pac.getCodigoUltimoRegistro() != null){
                                        mDatabaseRegistros.child(pac.getCodigoUltimoRegistro()).addListenerForSingleValueEvent(new ValueEventListener() {
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
