package tfg.sergio.bascula.Pacientes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;


import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Registro;
import tfg.sergio.bascula.Resources.CustomMarkerView;
import tfg.sergio.bascula.Resources.EnumIMC;
import tfg.sergio.bascula.Resources.IMCCalculator;
import tfg.sergio.bascula.bascula.basculaFragment;

/**
 * Created by yeyo on 05/03/2018.
 */

public class DetallePacienteFragment extends Fragment implements OnChartGestureListener,
        OnChartValueSelectedListener {
    private String key = "";
    private LineChart mChart, mChart2;
    private TextView out_nombre, out_peso, out_IMC, out_edad, out_altura;
    private ImageView out_perfil;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseRegs;
    private Button pesarButton;
    AlertDialog.Builder builder;
    private ArrayList<RegistroPaciente> registros = new ArrayList<>();


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_detalle_paciente, null);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_update:
                return true;
            case R.id.action_delete:
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        key = bundle.getString("key");
        mDatabase = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseRegs = FirebaseDatabase.getInstance().getReference("registros");
        out_nombre = view.findViewById(R.id.txt_nombre);
        out_peso = view.findViewById(R.id.txt_peso);
        out_edad = view.findViewById(R.id.txt_edad);
        out_IMC = view.findViewById(R.id.txt_imc);
        out_altura = view.findViewById(R.id.txt_altura);
        out_perfil = view.findViewById(R.id.foto_perfil);
        pesarButton = view.findViewById(R.id.btn_pesar);
        mChart = (LineChart) view.findViewById(R.id.peso_grafica);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart2 = (LineChart) view.findViewById(R.id.imc_grafica);
        mChart2.setOnChartGestureListener(this);
        mChart2.setOnChartValueSelectedListener(this);
        mChart2.setDrawGridBackground(false);
        obtenerRegistros();

        pesarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registros.clear();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack("detalle");
                Bundle bundle = new Bundle();
                bundle.putString("key",key);
                Fragment fragment = new basculaFragment();
                fragment.setArguments(bundle);
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
            }
        });


        mDatabase.child(key).addValueEventListener(new ValueEventListener() {
            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            IMCCalculator imcCalculator = new IMCCalculator();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Paciente paciente = (Paciente) dataSnapshot.getValue(Paciente.class);
                if(paciente == null){
                    return;
                }
                out_nombre.setText(paciente.getNombre() +" "+paciente.getApellidos());
                if(paciente.getUltimoRegistro() != null){
                    mDatabaseRegs.child(paciente.getUltimoRegistro()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            RegistroPaciente reg =  (RegistroPaciente) dataSnapshot.getValue(RegistroPaciente.class);
                            int IMC = IMCCalculator.Calcular(paciente.monthsBetweenDates(),reg.getIMC(),0);
                            out_IMC.setText(EnumIMC.values()[IMC].toString());
                            out_edad.setText(""+paciente.monthsBetweenDates());
                            out_altura.setText(String.format("%.2f",reg.getAltura()));
                            out_peso.setText(String.format("%.2f",reg.getPeso()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                out_edad.setText(formatter.format(paciente.getFechaNacimiento()));

                //cargar Imagen
                Picasso.with(getActivity()).load(paciente.getUrlImagen()).resize(200,200).into(out_perfil);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eliminar paciente");
        builder.setMessage("¿Está seguro de que desea eliminar al paciente seleccionado?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabase.child(key).removeValue();
                dialogInterface.dismiss();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                PacientesFragment fragment = new PacientesFragment();
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        iniciarGrafica();
        super.onViewCreated(view, savedInstanceState);
    }

    private void obtenerRegistros(){
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Query firebaseSearchQuery = mDatabaseRegs.orderByChild("codigoPaciente").equalTo(key);

        firebaseSearchQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Se carga cada uno de los centros
                RegistroPaciente value = dataSnapshot.getValue(RegistroPaciente.class);
                value.setCodigoRegistro(dataSnapshot.getKey());
                boolean insert = true;
                for(RegistroPaciente reg :registros){
                    if(reg.getCodigoRegistro() == value.getCodigoRegistro()){
                        insert = false;
                    }
                }
                if(insert){
                    registros.add(value);
                }
                Collections.sort(registros);
                setData();
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



    //region Graficas

    private void iniciarGrafica(){
        // add data
        setData();

//        ToDo: Pasar a funcion

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        // no description text
        //mChart.setDescription("Demo Line Chart");
        mChart.setNoDataText("You need to provide data for the chart.");
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(true);

        CustomMarkerView mv = new CustomMarkerView(getActivity(), R.layout.marker_layout);

        mChart.setMarkerView(mv);
        mChart.getAxisRight().setEnabled(false);
        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l2 = mChart2.getLegend();
        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l2.setForm(Legend.LegendForm.LINE);
        // no description text
        //mChart2.setDescription("Demo Line Chart");
        mChart2.setNoDataText("You need to provide data for the chart.");
        // enable touch gestures
        mChart2.setTouchEnabled(true);
        // enable scaling and dragging
        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);
        CustomMarkerView mv2 = new CustomMarkerView(getActivity(), R.layout.marker_layout);
        mChart2.setMarkerView(mv2);

        YAxis leftAxis2 = mChart2.getAxisLeft();
        leftAxis2.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis2.enableGridDashedLine(10f, 10f, 0f);
        leftAxis2.setDrawZeroLine(false);
        leftAxis2.setDrawLimitLinesBehindData(true);
        mChart2.getAxisRight().setEnabled(false);
        mChart2.animateX(2500, Easing.EasingOption.EaseInOutQuart);
        mChart2.invalidate();

    }

    private void setData() {
        Collections.sort(registros);
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues();

        ArrayList<Entry> yimc = setimcYvalues();

        LineDataSet set1;
        LineDataSet set2;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "Peso");
        set2 = new LineDataSet(yimc,"IMC");

        set2.setFillAlpha(110);
        set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        //   set1.enableDashedLine(10f, 5f, 0f);
        // set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);

        set2.setColor(Color.BLACK);
        set2.setCircleColor(Color.BLACK);
        set2.setLineWidth(1f);
        set2.setCircleRadius(3f);
        set2.setDrawCircleHole(false);
        set2.setValueTextSize(9f);
        set2.setDrawFilled(true);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<ILineDataSet> dataSets2 = new ArrayList<ILineDataSet>();

        dataSets.add(set1); // add the datasets
        dataSets2.add(set2);

        // create a data object with the datasets
        LineData data = new LineData(dataSets);
        LineData data2 = new LineData(dataSets2);

        // set data
        mChart.setData(data);
        mChart2.setData(data2);

    }

    private ArrayList<String> setXAxisValues(){
        ArrayList<String> xVals = new ArrayList<String>();
        int x =0 ;
        for(RegistroPaciente r : registros){
            xVals.add(xVals.toString());
            x+=60;
        }

        return xVals;
    }

    private ArrayList<Entry> setYAxisValues(){
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        int x =0;

        for(RegistroPaciente r : registros){
            yVals.add(new Entry(x,(float)r.getPeso()));
            x+=60;
        }
        if(yVals.size() == 0){
            yVals.add(new Entry(0, 0));
        }

        return yVals;
    }

    private ArrayList<Entry> setimcYvalues(){
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        int x =0;

        for(RegistroPaciente r : registros){
            float imc = (float) (r.getPeso()/Math.pow(r.getAltura(),2));
            yVals.add(new Entry(x,imc));
            x+=60;
        }
        if(yVals.size() == 0){
            yVals.add(new Entry(0, 0));
        }

        return yVals;

    }
    //region metodos override
    public void onChartGestureStart(MotionEvent me,
                                    ChartTouchListener.ChartGesture
                                            lastPerformedGesture) {

       // Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    public void onChartGestureEnd(MotionEvent me,
                                  ChartTouchListener.ChartGesture
                                          lastPerformedGesture) {

        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
//        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
//            // or highlightTouch(null) for callback to onNothingSelected(...)
//            mChart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {


    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {

    }
    //endregion

}

