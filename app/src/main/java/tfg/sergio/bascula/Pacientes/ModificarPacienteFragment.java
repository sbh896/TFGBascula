package tfg.sergio.bascula.Pacientes;

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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.FixedDatePicker;

/**
 * Created by sergio on 27/02/2018.
 */

public class ModificarPacienteFragment extends Fragment {

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private EditText inputNombre,inputApellido;
    private ImageButton inputFoto;
    private Uri uriImagenAltaCalidad = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Switch inputDieta;
    private Spinner mSpinner;
    private ArrayList<Centro>centros = new ArrayList<>();
    private Date fechaNacimiento;
    //almacenamiento firebase
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCentros;
    private ProgressDialog progreso;
    private Paciente pacienteOriginal;
    private String keyPaciente;


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
        return inflater.inflate(R.layout.fragment_aniadir_paciente, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        pacienteOriginal = bundle.getParcelable("paciente");
        keyPaciente = bundle.getString("key");

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mSpinner = (Spinner) view.findViewById(R.id.sp_centros);
        mDisplayDate = (TextView) view.findViewById(R.id.date_pick);
        inputNombre = (EditText) view.findViewById(R.id.nombre);
        inputApellido = (EditText) view.findViewById(R.id.apellidos);
        inputFoto = (ImageButton) view.findViewById(R.id.imagen_paciente);
        inputDieta = (Switch) view.findViewById(R.id.sw_dieta);
        progreso = new ProgressDialog(getActivity());
        inputNombre.setText(pacienteOriginal.getNombre());
        inputApellido.setText(pacienteOriginal.getApellidos());
        //mSpinner.setSelection(pacienteOriginal.getCentro());
        mDisplayDate.setText(formatter.format(pacienteOriginal.getFechaNacimiento()));

        Picasso.with(getActivity()).load(pacienteOriginal.getUrlImagen()).resize(200,200).into(inputFoto);
        //Guardado
        view.findViewById(R.id.btn_guardar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = inputNombre.getText().toString();
                String apellidos = inputApellido.getText().toString();

                if (TextUtils.isEmpty(nombre)) {
                    Toast.makeText(getActivity(), "Introduzca un nombre.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(apellidos)) {
                    Toast.makeText(getActivity(), "Introduzca los apellidos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mSpinner.getSelectedItemPosition() == 0){
                    Toast.makeText(getActivity(), "Seleccione un centro.", Toast.LENGTH_SHORT).show();
                    return;
                }

                guardarPaciente(nombre,apellidos);
            }
        });

        //Botón foto
        inputFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verificación de permisos
                verifyStoragePermissions(getActivity());
                uriImagenAltaCalidad = generateTimeStampPhotoFileUri();

                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT,uriImagenAltaCalidad);
                startActivityForResult(camera_intent,CAMERA_REQUEST_CODE);
            }
        });

        //seleccion de centro
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                Centro c = (Centro)mSpinner.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });



        //Seleccion de fecha
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int anio = cal.get(Calendar.YEAR);
                int mes = cal.get(Calendar.MONTH);
                int dia = cal.get(Calendar.DAY_OF_MONTH);
                Context ctx = new ContextThemeWrapper(
                        getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth
                );
                DatePickerDialog dialog = new FixedDatePicker(
                        ctx,
                        mDateSetListener,
                        anio,mes,dia
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int anio, int mes, int dia) {
                fechaNacimiento=new Date(anio,mes,dia);
                mes = mes +1;
                String fecha = dia+"/" + mes + "/" + anio;
                mDisplayDate.setText(fecha);
                pacienteOriginal.setFechaNacimiento(fechaNacimiento);
            }
        };
        this.obtenerCentros();
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
    private void obtenerCentros(){
        //Se establece el array centros como fuente del selector.
        ArrayAdapter<Centro> arrayAdapter = new ArrayAdapter<Centro>(getActivity(),android.R.layout.simple_spinner_dropdown_item,centros){
            @Override
            public boolean isEnabled(int position) {
                if(position == 0)
                {// Primer item será hint
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Hint color
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
                    // Hint color
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
                //Se carga cada uno de los centros
                Centro c= dataSnapshot.getValue(Centro.class);
                String key = dataSnapshot.getKey();
                centros.add(c);
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

    private void  guardarPaciente(final String nombre,final String apellidos){

        //Guardado del paciente en Firebase

        pacienteOriginal.setNombre(inputNombre.getText().toString());
        pacienteOriginal.setApellidos(inputApellido.getText().toString());
        Centro c = (Centro)mSpinner.getSelectedItem();
        pacienteOriginal.setCentro(c.Id);
        pacienteOriginal.setEsDietaHipocalorica(inputDieta.isChecked());

        if(uriImagenAltaCalidad != null){
            progreso.setMessage("Guardando paciente ...");
            progreso.show();
            StorageReference fotoEliminar = mStorage.child("Fotos_pacientes/"+ pacienteOriginal.getArchivoFoto());
            // Delete the file
            fotoEliminar.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    int k = 2;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    int i = 0;

                }
            });
            StorageReference path = mStorage.child("Fotos_pacientes").child(uriImagenAltaCalidad.getLastPathSegment());
            path.putFile(uriImagenAltaCalidad).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    Centro c = (Centro)mSpinner.getSelectedItem();
                    pacienteOriginal.setUrlImagen(downloadUri.toString());
                    pacienteOriginal.setArchivoFoto(uriImagenAltaCalidad.getLastPathSegment());
                    mDatabase.child(pacienteOriginal.getId()).setValue(pacienteOriginal);
                    progreso.dismiss();
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStackImmediate();
                }
            });
        }else{
            mDatabase.child(pacienteOriginal.getId()).setValue(pacienteOriginal);
            FragmentManager fm = getFragmentManager();
            fm.popBackStackImmediate();

        }
    }
    //endregion

    //region Camara y guardado de imágenes
    private Uri generateTimeStampPhotoFileUri() {

        Uri photoFileUri = null;
        File outputDir = getPhotoDirectory();
        if (outputDir != null) {
            Time t = new Time();
            t.setToNow();
            File photoFile = new File(outputDir, System.currentTimeMillis() + ".jpg");
            photoFileUri = Uri.fromFile(photoFile);
        }
        return photoFileUri;
    }

    private File getPhotoDirectory() {
        File outputDir = null;
        String externalStorageStagte = Environment.getExternalStorageState();
        if (externalStorageStagte.equals(Environment.MEDIA_MOUNTED)) {
            File photoDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            outputDir = new File(photoDir, getString(R.string.app_name));
            if (!outputDir.exists())
                if (!outputDir.mkdirs()) {
                    Toast.makeText(getActivity(),"Error al crear directorio "+ outputDir.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                    outputDir = null;
                }
        }
        return outputDir;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            //imagen alta calidad
            inputFoto.setScaleType(ImageView.ScaleType.FIT_XY);
            inputFoto.setImageURI(uriImagenAltaCalidad);
        }
    }
    //endregion
}
