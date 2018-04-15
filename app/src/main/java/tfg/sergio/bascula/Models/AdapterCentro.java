package tfg.sergio.bascula.Models;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import tfg.sergio.bascula.Centros.CentrosFragment;
import tfg.sergio.bascula.Pacientes.DetallePacienteFragment;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 24/03/2018.
 */

public class AdapterCentro extends RecyclerView.Adapter<AdapterCentro.CentrosViewHolder>{

    List<Centro> centros;
    Context ctx;

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
    public void onBindViewHolder(CentrosViewHolder holder, int position) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

        final Centro centro = centros.get(position);
        holder.centro_nombre.setText(centro.Nombre);
        //            //cargar Imagen
        //Picasso.with(holder.context).load(centro.paciente.getUrlImagen()).resize(200,200).into(holder.foto_centro);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm =  ((AppCompatActivity)ctx).getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack("centros");
                Bundle bundle = new Bundle();
               // bundle.putString("key",centro.key);
                DetallePacienteFragment fragment = new DetallePacienteFragment();
                fragment.setArguments(bundle);
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
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
