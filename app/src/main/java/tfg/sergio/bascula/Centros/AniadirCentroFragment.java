package tfg.sergio.bascula.Centros;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.FixedDatePicker;

/**
 * Created by yeyo on 24/03/2018.
 */

public class AniadirCentroFragment extends Fragment {

    private EditText inputCentro;
    private ImageButton inputFoto;
    private Uri uriImagenAltaCalidad = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    //almacenamiento firebase
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCentros, mDatabaseDatosMes;
    private ProgressDialog progreso;


    // Permiso de almacenamiento
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        return inflater.inflate(R.layout.fragment_aniadir_centro, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");

        inputCentro = (EditText) view.findViewById(R.id.nombre);
        inputFoto = (ImageButton) view.findViewById(R.id.imagen_paciente);
        progreso = new ProgressDialog(getActivity());

        //Guardado
        view.findViewById(R.id.btn_guardar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = inputCentro.getText().toString();

                if (TextUtils.isEmpty(nombre)) {
                    Toast.makeText(getActivity(), "Introduzca un nombre.", Toast.LENGTH_SHORT).show();
                    return;
                }

                guardarCentro(nombre);
            }
        });

    }

    //Verificador de permisos de la app para usar almacenamiento interno
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //region Firebase
    //Obtención de centros a cargar en el selector

    private void  guardarCentro(final String nombre){
        DateFormat dateFormat = new SimpleDateFormat("YYYYMM");
        Date date = new Date();

        //Guardado del paciente en Firebase
        progreso.setMessage("Guardando centro ...");
        progreso.show();

        String id = mDatabaseCentros.push().getKey();
        String id2 = mDatabaseDatosMes.push().getKey();
        Centro centro = new Centro(id,nombre);

        String ident = id + dateFormat.format(date);
        PacientesMesCentro pmc= new PacientesMesCentro(ident);

        mDatabaseDatosMes.child(id2).setValue(pmc);
        mDatabaseCentros.child(id).setValue(centro);
        progreso.dismiss();


        FragmentManager fm = getFragmentManager();
        fm.popBackStackImmediate();
    }
    //endregion
}
