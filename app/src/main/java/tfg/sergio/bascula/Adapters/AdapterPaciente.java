package tfg.sergio.bascula.Adapters;

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

import tfg.sergio.bascula.Models.ElementoListadoPaciente;
import tfg.sergio.bascula.Pacientes.DetallePacienteFragment;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 24/03/2018.
 */

public class AdapterPaciente extends RecyclerView.Adapter<AdapterPaciente.PacientesViewHolder>{

    List<ElementoListadoPaciente> pacientes;
    Context ctx;

    public AdapterPaciente(List<ElementoListadoPaciente> listado, Context c){
        this.pacientes = listado;
        this.ctx = c;
    }

    @Override
    public PacientesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pacientes_list_layout, parent, false);
        PacientesViewHolder holder = new PacientesViewHolder(v,parent.getContext());
        return holder;
    }

    @Override
    public void onBindViewHolder(PacientesViewHolder holder, int position) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

        final ElementoListadoPaciente paciente = pacientes.get(position);
        holder.paciente_nombre.setText(paciente.paciente.getNombre() + " " + paciente.paciente.getApellidos());
        holder.paciente_fecha.setText(paciente.registroPaciente == null ? "-":formatter.format(paciente.registroPaciente.getFecha()));
        //            //cargar Imagen
        Picasso.with(holder.context).load(paciente.paciente.getUrlImagen()).resize(200,200).into(holder.foto_paciente);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm =  ((AppCompatActivity)ctx).getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack("pacientes");
                Bundle bundle = new Bundle();
                bundle.putString("key",paciente.key);
                DetallePacienteFragment fragment = new DetallePacienteFragment();
                fragment.setArguments(bundle);
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return pacientes.size();
    }

    public static class PacientesViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        Context context;
        TextView paciente_nombre, paciente_fecha;
        ImageView foto_paciente;


        public PacientesViewHolder(View itemView, Context ctx) {
            super(itemView);
            mView = itemView;
            paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            paciente_fecha = mView.findViewById(R.id.fecha);
            foto_paciente = (ImageView) mView.findViewById(R.id.iamgen_perfil);

            context = ctx;

        }

        public void setDetails(Context ctx, ElementoListadoPaciente paciente){
            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            paciente_fecha = mView.findViewById(R.id.fecha);
           // ImageView estadoPaciente = mView.findViewById(R.id.imagen_estado);
//            GradientDrawable bgShape = (GradientDrawable)estadoPaciente.getDrawable();
//            Resources res = ctx.getResources();
//            switch (estado){
//                case 0:
//                    bgShape.setColor(Color.RED);
//                    break;
//                case 1:
//                    bgShape.setColor(res.getColor(R.color.OrangeRed));
//                    break;
//                case 2:
//                    bgShape.setColor(res.getColor(R.color.Green));
//                    break;
//                case 3:
//                    bgShape.setColor(res.getColor(R.color.LightGreen));
//                    break;
//                case 4:
//                    bgShape.setColor(res.getColor(R.color.OrangeRed));
//                    break;
//                case 5:
//                    bgShape.setColor(Color.RED);
//                    break;
//
//            }
//            bgShape.setColor(Color.BLACK);


            paciente_nombre.setText(paciente.paciente.getNombre() + " " + paciente.paciente.getApellidos());

            paciente_fecha.setText(paciente.registroPaciente == null ? "-":formatter.format(paciente.registroPaciente.getFecha()));
//
//            //cargar Imagen
//            ImageView foto_paciente = (ImageView) mView.findViewById(R.id.iamgen_perfil);
//            Picasso.with(ctx).load(imagen).resize(200,200).into(foto_paciente);
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
