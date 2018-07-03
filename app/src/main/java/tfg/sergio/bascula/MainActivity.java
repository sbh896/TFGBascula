package tfg.sergio.bascula;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tfg.sergio.bascula.Calendario.CalendarioFragment;
import tfg.sergio.bascula.Centros.CentrosFragment;
import tfg.sergio.bascula.Models.AdapterAlerta;
import tfg.sergio.bascula.Models.AdapterRegistro;
import tfg.sergio.bascula.Models.Alerta;
import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.ElementoListadoAlerta;
import tfg.sergio.bascula.Models.ElementoListadoPaciente;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.Pacientes.DetallePacienteFragment;
import tfg.sergio.bascula.Pacientes.PacientesFragment;
import tfg.sergio.bascula.Resources.SwipeController;
import tfg.sergio.bascula.Resources.SwipeControllerActions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private TextView navName;
    private TextView navMail;
    private NavigationView mDrawerLayout;
    private BarChart mChart;
    private DatabaseReference mDatabaseCentros, mDatabaseDatosMes, mDatabaseRegistros, mDatabasePacientes, mDatabaseAlertas;
    private List<PacientesMesCentro> pmCentros = new ArrayList<>();
    private RecyclerView listaRegistros;

    private ArrayList<Centro> centros = new ArrayList<>();
    private int NumObesidad = 0;
    private int NumSobrepeso = 0;
    private int NumNormal = 0;
    private int NumDesnutricion = 0;
    private int NumDesnutricionMod = 0;
    private int NumDesnutricionSev = 0;
    private List<ElementoListadoPaciente> elementos = new ArrayList<>();
    private RecyclerView.Adapter adapter;
    private RecyclerView.Adapter adapterAlertas;

    private RecyclerView recyclerAlertas;
    private List<ElementoListadoAlerta> listaAlertas = new ArrayList<>();
    private BluetoothGatt mGatt;



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        navName = (TextView)header.findViewById(R.id.user_name);

        navMail = (TextView)header.findViewById(R.id.email);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        navMail.setText(user.getEmail());
        navName.setText(user.getDisplayName());
        mDrawerLayout = findViewById(R.id.nav_view);
        mChart = (BarChart) findViewById(R.id.chart);
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");
        mDatabaseRegistros = FirebaseDatabase.getInstance().getReference("registros");
        mDatabasePacientes = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseAlertas = FirebaseDatabase.getInstance().getReference("alertas");

        listaRegistros = findViewById(R.id.lista_registros);
        listaRegistros.setLayoutManager(new GridLayoutManager(this,1));

        recyclerAlertas = findViewById(R.id.lista_alertas);
        recyclerAlertas.setLayoutManager(new GridLayoutManager(this,1));



        obtenerCentros();
        obtenerUltimosRegistros();
        obtenerAlertas();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getSupportFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                super.onBackPressed();
                //additional code
            } else {
                if(mGatt != null){
                    mGatt.disconnect();
                    mGatt.close();
                }
                getSupportFragmentManager().popBackStack();

            }
        }
    }

    public void setGatt(BluetoothGatt gatt){
        mGatt = gatt;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            fragment = new PacientesFragment();
        } else if (id == R.id.nav_logout) {
            auth.signOut();

            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() > 0) {
                FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
                manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            startActivity(new Intent(MainActivity.this, Login.class));
        }
        else if(id == R.id.nav_tools){
            fragment = new CentrosFragment();
        }
        else if(id == R.id.nav_clendar){
            fragment = new CalendarioFragment();
        }


        if(fragment != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.addToBackStack("Main");
            ft.replace(R.id.screen_area, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void obtenerAlertas(){
        listaAlertas.removeAll(listaAlertas);

        final Date[] fechaUltimoRegistro = {null};



        adapterAlertas = new AdapterAlerta(listaAlertas, this);

        recyclerAlertas.setAdapter(adapterAlertas);

        SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                listaAlertas.remove(position);
                adapterAlertas.notifyDataSetChanged();
//                mAdapter.players.remove(position);
//                mAdapter.notifyItemRemoved(position);
//                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            }

            @Override
            public void onLeftClicked(int position) {
                ElementoListadoAlerta alertaSeleccionada = listaAlertas.get(position);
                Fragment fragment = new DetallePacienteFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString("key", alertaSeleccionada.paciente.getId());
                fragment.setArguments(bundle);
                ft.addToBackStack("Main");
                ft.replace(R.id.screen_area, fragment);
                ft.commit();

            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerAlertas);

        mDatabaseAlertas.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot child: dataSnapshot.getChildren()) {

                    final Alerta alerta = child.getValue(Alerta.class);
                    final String alertaKey = child.getKey();


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
                                //ela.key = key;
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
                    adapterAlertas.notifyDataSetChanged();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //region Grafica
    private void obtenerRegistroPacientes(){
        final DateFormat dateFormat = new SimpleDateFormat("YYYYMM");
        NumObesidad = 0;
        NumSobrepeso = 0;
        NumNormal = 0;
        NumDesnutricion = 0;
        NumDesnutricionMod = 0;
        NumDesnutricionSev = 0;
        final Date date = new Date();

        final String ident;
        Query firebaseSearchQuery;
        firebaseSearchQuery  = mDatabaseDatosMes.orderByChild("id").endAt(dateFormat.format(date));


        //actualización recuento por centros
        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int prueba = 1;
                PacientesMesCentro pmc = null;
                String Key="";
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    System.out.println(child.getKey());
                    pmc = child.getValue(PacientesMesCentro.class);
                    prueba = prueba + 1;


                    PacientesMesCentro temp = null;

                    if (pmc != null && pmc.id.contains(dateFormat.format(date))) {
                        NumObesidad = NumObesidad + pmc.NumObesidad;
                        NumSobrepeso = NumSobrepeso + pmc.NumSobrepeso;
                        NumNormal = pmc.NumNormal + NumNormal;
                        NumDesnutricion = pmc.NumDesnutricion + NumDesnutricion;
                        NumDesnutricionMod = pmc.NumDesnutricionMod + NumDesnutricionMod;
                        NumDesnutricionSev = pmc.NumDesnutricionSev + NumDesnutricionSev;
                    }
                }
                inicializarGrafica();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void inicializarGrafica(){
      //  mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setNoDataText("Por favor seleccione un centro para visualizar la gráfica.");
        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

//        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(mChart);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        //xAxis.setValueFormatter(xAxisFormatter);

       // IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setLabelCount(8, false);
        //leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        // rightAxis.setTypeface(mTfLight);
        rightAxis.setLabelCount(8, false);
       // rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        String[]estados ={"Obesidad","Sobrepeso", "Normal", "Desnutrición","Desnutrición moderada","Desnutrición severa"};
        mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(estados));
        mChart.getXAxis().setLabelRotationAngle(20);

//        XYMarkerView mv = new XYMarkerView(this, xAxisFormatter);
//        mv.setChartView(mChart); // For bounds control
//        mChart.setMarker(mv); // Set the marker to the chart

        setData(3, 50);
        mChart.invalidate();

    }

    private void setData(int count, float range) {

        float start = 1f;
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        yVals1.add(new BarEntry(0, NumObesidad));
        yVals1.add(new BarEntry(1, NumSobrepeso));
        yVals1.add(new BarEntry(2, NumNormal));
        yVals1.add(new BarEntry(3, NumDesnutricion));
        yVals1.add(new BarEntry(4, NumDesnutricionMod));
        yVals1.add(new BarEntry(5, NumDesnutricionSev));

        BarDataSet set1;
        mChart.animateY(3000);
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "Pacientes "+ meses[new Date().getMonth()]);

            set1.setDrawIcons(false);

            //    set1.setColors(ColorTemplate.MATERIAL_COLORS);
            set1.setColors(ColorTemplate.COLORFUL_COLORS);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            //  data.setValueTypeface(mTfLight);
            data.setBarWidth(0.9f);

            mChart.setData(data);
        }

    }
    //endregion

    //region centros
    private void obtenerCentros(){

        mDatabaseCentros.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int prueba = 1;
                PacientesMesCentro pmc = null;
                String Key="";
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Centro c= child.getValue(Centro.class);



                    String key = dataSnapshot.getKey();
                    centros.add(c);

                }
                NumObesidad = 0;
                NumSobrepeso = 0;
                NumNormal = 0;
                NumDesnutricion = 0;
                NumDesnutricionMod = 0;
                NumDesnutricionSev = 0;
                obtenerRegistroPacientes();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
    //endregion

    //region Últimos registros

    public void obtenerUltimosRegistros(){

        elementos.removeAll(elementos);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        Long fFin = Calendar.getInstance().getTimeInMillis();
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -15);
        Long fInicio = calendar.getTimeInMillis();
        final Date[] fechaUltimoRegistro = {null};


        Query firebaseSearchQuery = mDatabaseRegistros.orderByChild("timeStamp").startAt(fInicio);

        adapter = new AdapterRegistro(elementos, this);

        listaRegistros.setAdapter(adapter);

        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot child: dataSnapshot.getChildren()) {

                    final RegistroPaciente registro = child.getValue(RegistroPaciente.class);
                    //final String paciente_key = dataSnapshot.getKey();


                    if (registro.getCodigoPaciente() != null) {
                        Query firebaseSearchQuery = mDatabaseRegistros.orderByChild("StrFecha");//.startAt(search).endAt(search + "\uf8ff");

                        mDatabasePacientes.child(registro.getCodigoPaciente()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Paciente pac = dataSnapshot.getValue(Paciente.class);
                                ElementoListadoPaciente elp = new ElementoListadoPaciente();
                                String key = child.getKey();
                                elp.registroPaciente = registro;
                                if (pac != null) {
                                    elp.paciente = pac;
                                }
                                elp.key = key;
                                addElement(elp);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
            public void addElement(ElementoListadoPaciente elp){

                if(!elementos.contains(elp)){
                    elementos.add(elp);
                    adapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //endregion

}
