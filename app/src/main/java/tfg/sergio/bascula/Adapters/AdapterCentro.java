package tfg.sergio.bascula.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
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

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 24/03/2018.
 */

public class AdapterCentro extends RecyclerView.Adapter<AdapterCentro.CentrosViewHolder>{

    List<Centro> centros;
    Context ctx;
    String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
    private String[]estados ={"Obesidad","Sobrepeso", "Normal", "Desnutrición","Desnutrición moderada","Desnutrición severa"};
    int mesSeleccionado = 01;
    private DatabaseReference mDatabaseDatosMes;
    private BarChart mChart;


    public AdapterCentro(List<Centro> listado, Context c){
        this.centros = listado;
        this.ctx = c;



    }

    @Override
    public CentrosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.centros_list_layout, parent, false);
        CentrosViewHolder holder = new CentrosViewHolder(v,parent.getContext());
        return holder;
    }

    @Override
    public void onBindViewHolder(final CentrosViewHolder holder, final int position) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");

        final Centro centro = centros.get(position);
        holder.centro_nombre.setText(centro.Nombre);
        //            //cargar Imagen
        //Picasso.with(holder.context).load(centro.paciente.getUrlImagen()).resize(200,200).into(holder.foto_centro);


        holder.mView.findViewById(R.id.textViewOptions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(ctx, holder.mView.findViewById(R.id.textViewOptions));
                //inflating menu from xml resource
                popup.inflate(R.menu.main);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete:
                                Toast.makeText(ctx, "borrar", Toast.LENGTH_SHORT).show();

                                break;
                            case R.id.action_update:
                                Toast.makeText(ctx, "update", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();

            }
        });

        holder.mView.findViewById(R.id.iamgen_perfil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DateFormat dateFormat = new SimpleDateFormat("YYYY");

                final Date date = new Date();
                final String anio = dateFormat.format(date);
                Query firebaseSearchQuery;

                firebaseSearchQuery  = mDatabaseDatosMes.orderByChild("id").endAt(dateFormat.format(date));
                LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.centro_deetalle_layout,null);
                mChart = (BarChart) layout.findViewById(R.id.chart);
                final Centro centro = centros.get(position);
                Date currentTime = Calendar.getInstance().getTime();
                mesSeleccionado = currentTime.getMonth();
                ((TextView)layout.findViewById(R.id.centro)).setText(meses[mesSeleccionado]);
                float density=ctx.getResources().getDisplayMetrics().density;
                final PopupWindow pw = new PopupWindow(layout, (int)density*800, (int)density*800, true);
                pw.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                pw.setTouchInterceptor(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {

                            pw.dismiss();

                            return true;
                        }
                        return false;
                    }
                });
                ((ImageButton)layout.findViewById(R.id.izquierda)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mesSeleccionado == 0){
                            return;
                        }
                        mesSeleccionado--;
                        String id = centro.Id + anio + (String.format("%02d", mesSeleccionado + 1));
                        obtenerDatosMes(id);
                        ((TextView)layout.findViewById(R.id.centro)).setText(meses[mesSeleccionado]);
                    }
                });

                ((ImageButton)layout.findViewById(R.id.derecha)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mesSeleccionado == 11){
                            return;
                        }
                        mesSeleccionado++;
                        String id = centro.Id + anio + (String.format("%02d", mesSeleccionado + 1));
                        obtenerDatosMes(id);
                        ((TextView)layout.findViewById(R.id.centro)).setText(meses[mesSeleccionado]);
                    }
                });

                String id = centro.Id + anio + (String.format("%02d", mesSeleccionado + 1));
                obtenerDatosMes(id);
                pw.setOutsideTouchable(true);
                // display the pop-up in the center
                pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });

    }


    private void obtenerDatosMes(String id){
        final Query firebaseSearchQuery;
        final DateFormat dateFormat = new SimpleDateFormat("YYYY");
        final Date date = new Date();
        firebaseSearchQuery  = mDatabaseDatosMes.orderByChild("id").startAt(id).endAt(id);
        //actualización recuento por centros
        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int flag = 1;
                PacientesMesCentro pmc = null;
                String Key="";
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    System.out.println(child.getKey());
                    pmc = child.getValue(PacientesMesCentro.class);
                    PacientesMesCentro temp = null;
                    if (pmc != null && pmc.id.contains(dateFormat.format(date))) {
                        inicializarGrafica(pmc);
                        flag = 0;
                    }
                }
                if(flag == 1){
                    inicializarGrafica(null);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void inicializarGrafica(PacientesMesCentro pmc){
        if(pmc == null){
            mChart.clear();
            mChart.invalidate();
            return;
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        yVals1.add(new BarEntry(0, pmc.NumObesidad));
        yVals1.add(new BarEntry(1, pmc.NumSobrepeso));
        yVals1.add(new BarEntry(2, pmc.NumNormal));
        yVals1.add(new BarEntry(3, pmc.NumDesnutricion));
        yVals1.add(new BarEntry(4, pmc.NumDesnutricionMod));
        yVals1.add(new BarEntry(5, pmc.NumDesnutricionSev));

        BarDataSet dataset = new BarDataSet(yVals1, "# of Calls");

        BarDataSet set1;
        mChart.animateY(3000);
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "Pacientes "+ meses[mesSeleccionado]);

            set1.setDrawIcons(false);

            //    set1.setColors(ColorTemplate.MATERIAL_COLORS);
            set1.setColors(ColorTemplate.COLORFUL_COLORS);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setBarWidth(0.1f);
            data.setValueTextSize(10f);
            //  data.setValueTypeface(mTfLight);
            data.setBarWidth(0.5f);

            mChart.setData(data);
        }
        mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(estados));
        mChart.getXAxis().setLabelRotationAngle(20);
        mChart.invalidate();


    }

    @Override
    public int getItemCount() {
        return centros.size();
    }

    public static class CentrosViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        Context context;
        TextView centro_nombre, buttonViewOption;
        ImageView foto_centro;


        public CentrosViewHolder(View itemView, Context ctx) {
            super(itemView);
            mView = itemView;
            centro_nombre =  (TextView) mView.findViewById(R.id.nombre);
            //foto_centro = (ImageView) mView.findViewById(R.id.iamgen_perfil);

            context = ctx;

        }

        public void setDetails(Context ctx, Centro centro){
            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            centro_nombre =  (TextView) mView.findViewById(R.id.nombre);
            centro_nombre.setText(centro.Nombre);
            buttonViewOption = (TextView) itemView.findViewById(R.id.textViewOptions);

        }
    }
}
