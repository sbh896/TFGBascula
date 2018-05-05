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

import tfg.sergio.bascula.Pacientes.DetallePacienteFragment;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 24/03/2018.
 */

public class AdapterAlerta extends RecyclerView.Adapter<AdapterAlerta.PacientesViewHolder>{

    List<ElementoListadoAlerta> alertas;
    Context ctx;

    public AdapterAlerta(List<ElementoListadoAlerta> listado, Context c){
        this.alertas = listado;
        this.ctx = c;
    }

    @Override
    public PacientesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.aviso_list_layout, parent, false);
        PacientesViewHolder holder = new PacientesViewHolder(v,parent.getContext());
        return holder;
    }

    @Override
    public void onBindViewHolder(PacientesViewHolder holder, int position) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

        final ElementoListadoAlerta alerta = alertas.get(position);
        holder.paciente_nombre.setText(alerta.paciente.getNombre() + " " + alerta.paciente.getApellidos());
        holder.paciente_fecha.setText(alerta.ultimoRegistro == null ? "-":formatter.format(alerta.ultimoRegistro.getFecha()));
        holder.alerta_fecha.setText(formatter.format(alerta.alerta.fechaInicio));
        holder.peso.setText(String.valueOf(alerta.ultimoRegistro.getPeso()));

//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager fm =  ((AppCompatActivity)ctx).getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.addToBackStack("pacientes");
//                Bundle bundle = new Bundle();
//                bundle.putString("key",paciente.key);
//                DetallePacienteFragment fragment = new DetallePacienteFragment();
//                fragment.setArguments(bundle);
//                ft.replace(R.id.pacientes_screen,fragment);
//                ft.commit();
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return alertas.size();
    }

    public static class PacientesViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        Context context;
        TextView paciente_nombre, paciente_fecha, alerta_fecha, peso, fechaRegistro;

        public PacientesViewHolder(View itemView, Context ctx) {
            super(itemView);
            mView = itemView;
            paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            paciente_fecha = mView.findViewById(R.id.txt_fecha);
            alerta_fecha = mView.findViewById(R.id.txt_fecha_alerta);
            peso = mView.findViewById(R.id.txt_peso);

            context = ctx;

        }

        public void setDetails(Context ctx, ElementoListadoAlerta alerta){
            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            paciente_fecha = mView.findViewById(R.id.fecha);
            alerta_fecha = mView.findViewById(R.id.txt_fecha_alerta);



            paciente_nombre.setText(alerta.paciente.getNombre() + " " + alerta.paciente.getApellidos());

            paciente_fecha.setText(alerta.ultimoRegistro== null ? "-":formatter.format(alerta.ultimoRegistro.getFecha()));

        }
    }
}
