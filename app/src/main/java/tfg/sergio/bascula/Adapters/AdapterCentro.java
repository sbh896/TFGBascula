package tfg.sergio.bascula.Adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 24/03/2018.
 */

public class AdapterCentro extends RecyclerView.Adapter<AdapterCentro.CentrosViewHolder>{

    List<Centro> centros;
    Context ctx;
    String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
    int mesSeleccionado = 01;

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
    public void onBindViewHolder(CentrosViewHolder holder, final int position) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

        final Centro centro = centros.get(position);
        holder.centro_nombre.setText(centro.Nombre);
        //            //cargar Imagen
        //Picasso.with(holder.context).load(centro.paciente.getUrlImagen()).resize(200,200).into(holder.foto_centro);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.centro_deetalle_layout,null);
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
                        ((TextView)layout.findViewById(R.id.centro)).setText(meses[mesSeleccionado]);
                    }
                });


                pw.setOutsideTouchable(true);
                // display the pop-up in the center
                pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });

    }

    @Override
    public int getItemCount() {
        return centros.size();
    }

    public static class CentrosViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        Context context;
        TextView centro_nombre;
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


            centro_nombre.setText(centro.Nombre);//
//            //cargar Imagen
//            ImageView foto_centro = (ImageView) mView.findViewById(R.id.iamgen_perfil);
//            Picasso.with(ctx).load(imagen).resize(200,200).into(foto_centro);
//
//            if(c!= null && !centro.equals(c.getId()) && c.getId() != "-1"){
//                //Si no pertenece al centro del filtro de búsqueda, se quita la vista de pantalla.l
//                mView.setVisibility(View.GONE);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
//                params.height = 0;
//                // mView.setLayoutParams(params);
//            }
        }
    }
}
