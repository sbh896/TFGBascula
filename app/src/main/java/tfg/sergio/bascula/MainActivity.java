package tfg.sergio.bascula;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

import tfg.sergio.bascula.Centros.AniadirCentroFragment;
import tfg.sergio.bascula.Centros.CentrosFragment;
import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Mes;
import tfg.sergio.bascula.Models.MesesAnno;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Pacientes.PacientesFragment;
import tfg.sergio.bascula.Resources.EnumIMCFirebase;
import tfg.sergio.bascula.Resources.IMCCalculator;
import tfg.sergio.bascula.bascula.basculaFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private TextView navName;
    private TextView navMail;
    private NavigationView mDrawerLayout;
    private Spinner mSpinner, mSpinnerMeses, mSpinnerAnios;
    private BarChart mChart;
    private DatabaseReference mDatabaseCentros, mDatabaseDatosMes;
    private List<PacientesMesCentro> pmCentros = new ArrayList<>();
    private MesesAnno meses = new MesesAnno();


    private ArrayList<Centro> centros = new ArrayList<>();
    private int NumObesidad = 0;
    private int NumSobrepeso = 0;
    private int NumNormal = 0;
    private int NumDesnutricion = 0;
    private int NumDesnutricionMod = 0;
    private int NumDesnutricionSev = 0;

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
        mSpinner = (Spinner) findViewById(R.id.sp_centros);
        mSpinnerMeses = findViewById(R.id.sp_meses);
        mSpinnerAnios = findViewById(R.id.sp_anios);

        navMail = (TextView)header.findViewById(R.id.email);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        navMail.setText(user.getEmail());
        navName.setText(user.getDisplayName());
        mDrawerLayout = findViewById(R.id.nav_view);
        mChart = (BarChart) findViewById(R.id.chart);
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");

        //rellenar spinners
        ArrayAdapter<Mes> adaptermeses = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                meses.mesesAnio
        );
        mSpinnerMeses.setAdapter(adaptermeses);

        ArrayList<Integer> anios = new ArrayList<>();
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = 0; i<5 ; i++){
            anios.add(anioActual);
            anioActual--;
        }
        ArrayAdapter<Integer> adapteranios = new ArrayAdapter<Integer>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                anios
        );
        mSpinnerAnios.setAdapter(adapteranios);
        //seleccion de centro
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                pmCentros.clear();
                //inicializarGrafica();
                NumObesidad = 0;
                NumSobrepeso = 0;
                NumNormal = 0;
                NumDesnutricion = 0;
                NumDesnutricionMod = 0;
                NumDesnutricionSev = 0;
                obtenerRegistroPacientes();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mSpinnerMeses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pmCentros.clear();
                //inicializarGrafica();
                NumObesidad = 0;
                NumSobrepeso = 0;
                NumNormal = 0;
                NumDesnutricion = 0;
                NumDesnutricionMod = 0;
                NumDesnutricionSev = 0;
                obtenerRegistroPacientes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpinnerAnios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pmCentros.clear();
                //inicializarGrafica();
                NumObesidad = 0;
                NumSobrepeso = 0;
                NumNormal = 0;
                NumDesnutricion = 0;
                NumDesnutricionMod = 0;
                NumDesnutricionSev = 0;
                obtenerRegistroPacientes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        obtenerCentros();


    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                super.onBackPressed();
                //additional code
            } else {
                getFragmentManager().popBackStack();
            }
        }

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

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "The year 2017");

            set1.setDrawIcons(false);

            //    set1.setColors(ColorTemplate.MATERIAL_COLORS);
            set1.setColors(Color.BLUE);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            //  data.setValueTypeface(mTfLight);
            data.setBarWidth(0.9f);

            mChart.setData(data);
        }

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

    private void obtenerRegistroPacientes(){
        DateFormat dateFormat = new SimpleDateFormat("YYYYMM");

        Date date = new Date();
        Centro selectedCentro = (Centro)mSpinner.getSelectedItem();
        Integer selectedYear = (Integer)mSpinnerAnios.getSelectedItem();
        Mes selectedMonth = (Mes)mSpinnerMeses.getSelectedItem();

        final String ident;
        Query firebaseSearchQuery;
        if(selectedCentro.Id.equals("-1")){
            return;
        }
        else{
            ident = selectedCentro.Id + selectedYear + selectedMonth.Id;
            firebaseSearchQuery  = mDatabaseDatosMes.orderByChild("id").startAt(ident).endAt(ident);
        }

        //actualización recuento por centros
        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PacientesMesCentro pmc = null;
                String Key="";
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    System.out.println(child.getKey());
                    pmc = child.getValue(PacientesMesCentro.class);
                }
                PacientesMesCentro temp = null;

                if(pmc!=null){
                    for (PacientesMesCentro pmc2: pmCentros
                            ) {
                        if(pmc2.id.equals(pmc.id)){
                            temp = pmc2;
                        }
                    }
                    NumObesidad = NumObesidad + pmc.NumObesidad - (temp == null ?  0 : temp.NumObesidad);
                    NumSobrepeso = NumSobrepeso + pmc.NumSobrepeso - (temp == null ? 0 : temp.NumSobrepeso);
                    NumNormal = pmc.NumNormal + NumNormal - (temp == null ? 0 : temp.NumNormal);
                    NumDesnutricion = pmc.NumDesnutricion + NumDesnutricion - (temp == null ? 0 : temp.NumDesnutricion);
                    NumDesnutricionMod = pmc.NumDesnutricionMod + NumDesnutricionMod - (temp == null ? 0 : temp.NumDesnutricionMod);
                    NumDesnutricionSev = pmc.NumDesnutricionSev + NumDesnutricionSev - (temp == null ? 0 : temp.NumDesnutricionSev);
                    pmCentros.remove(temp);
                    pmCentros.add(pmc);
                }

                inicializarGrafica();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
    //region centros
    private void obtenerCentros(){
        ArrayAdapter<Centro> arrayAdapter = new ArrayAdapter<Centro>(this,android.R.layout.simple_spinner_dropdown_item,centros){
            @Override
            public boolean isEnabled(int position) {
                return true;
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

        //Se establece el adapter para el selector de centros
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(arrayAdapter);

        mDatabaseCentros.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Centro selectedCentro = (Centro)mSpinner.getSelectedItem();
                if(selectedCentro!= null){
                    Toast.makeText(getApplicationContext(), selectedCentro.Id, Toast.LENGTH_SHORT).show();
                }
                Centro c= dataSnapshot.getValue(Centro.class);
                if(selectedCentro != null && selectedCentro.Id != "-1" && selectedCentro.Id != c.Id){
                    return;
                }


                    String key = dataSnapshot.getKey();
                centros.add(c);
                obtenerRegistroPacientes();
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
