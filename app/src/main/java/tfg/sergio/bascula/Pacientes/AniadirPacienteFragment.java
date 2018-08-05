package tfg.sergio.bascula.Pacientes;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.text.format.Time;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.FixedDatePicker;

/**
 * Created by sergio on 27/02/2018.
 */

public class AniadirPacienteFragment extends Fragment {

    private EditText inputNombre,inputApellido;
    private ImageButton inputFoto;
    private Uri uriImagenAltaCalidad = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Spinner inputDieta;
    private Spinner mSpinner, inputGenero;
    private ArrayList<Centro>centros = new ArrayList<>();
    private Date fechaNacimiento;
    //almacenamiento firebase
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCentros;
    private ProgressDialog progreso;
    private Bitmap imageBitmap;


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
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mSpinner = (Spinner) view.findViewById(R.id.sp_centros);
        mDisplayDate = (TextView) view.findViewById(R.id.date_pick);
        inputNombre = (EditText) view.findViewById(R.id.nombre);
        inputApellido = (EditText) view.findViewById(R.id.apellidos);
        inputFoto = (ImageButton) view.findViewById(R.id.imagen_paciente);
        inputDieta =  view.findViewById(R.id.sw_dieta);
        progreso = new ProgressDialog(getActivity());
        inputGenero = view.findViewById(R.id.sp_genero);
        if(savedInstanceState != null){
            byte[] byteArray = savedInstanceState.getByteArray("imageBitmap");
            if(byteArray != null){
                imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                uriImagenAltaCalidad = Uri.parse(savedInstanceState.getString("imageUri"));
            }
        }
        if(imageBitmap != null){
            inputFoto.setScaleType(ImageView.ScaleType.FIT_XY);
            inputFoto.setImageBitmap(imageBitmap);
        }


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
                else if (TextUtils.isEmpty(apellidos)) {
                    Toast.makeText(getActivity(), "Introduzca los apellidos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(uriImagenAltaCalidad == null) {
                    Toast.makeText(getActivity(), "Introduzca una imagen de usuario.", Toast.LENGTH_SHORT).show();
                    return;

                }
                else if(mSpinner.getSelectedItemPosition() == 0){
                    Toast.makeText(getActivity(), "Seleccione un centro.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(inputGenero.getSelectedItemPosition() == 0){
                    Toast.makeText(getActivity(), "Seleccione un género.", Toast.LENGTH_SHORT).show();
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
                getActivity().setRequestedOrientation(getActivity().getResources().getConfiguration().orientation);
                imageBitmap= null;
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
                fechaNacimiento=new Date();
                fechaNacimiento.setMonth(mes);
                fechaNacimiento.setYear(anio);
                fechaNacimiento.setDate(dia);
                mes = mes +1;
                String fecha = dia+"/" + mes + "/" + anio;
                mDisplayDate.setText(fecha);
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
        progreso.setMessage("Guardando paciente ...");
        progreso.show();
        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriImagenAltaCalidad);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageReference path = mStorage.child("Fotos_pacientes").child(uriImagenAltaCalidad.getLastPathSegment());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask2 = path.putBytes(data);
        uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                Centro c = (Centro)mSpinner.getSelectedItem();
                String id = mDatabase.push().getKey();
                int genero = inputGenero.getSelectedItemPosition();
                Paciente paciente = new Paciente(nombre,apellidos,id,downloadUri.toString(),c.Id,fechaNacimiento,inputDieta.getSelectedItemPosition(),uriImagenAltaCalidad.getLastPathSegment(),genero);
                mDatabase.child(id).setValue(paciente);
                progreso.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progreso.dismiss();
                // Toast.makeText(getActivity(), "Upload Failed -> " + e, Toast.LENGTH_LONG).show();
            }
        });

        FragmentManager fm = getFragmentManager();
        fm.popBackStackImmediate();
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
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            //imagen alta calidad
            inputFoto.setScaleType(ImageView.ScaleType.FIT_XY);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            if(uriImagenAltaCalidad == null){
                Toast.makeText(getActivity(), "Por favor mantenga la orientación de la cámara al hacer la foto.", Toast.LENGTH_LONG).show();
                return;
            }
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            InputStream imageStream = null;
            try {
                imageStream = getActivity().getContentResolver().openInputStream(uriImagenAltaCalidad);
                Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);
                img = rotateImageIfRequired(getActivity(), img, uriImagenAltaCalidad);
                inputFoto.setImageBitmap(img);
                imageBitmap = img;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
    //endregion

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        if(imageBitmap != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            imageBitmap.recycle();
            savedInstanceState.putByteArray("imageBitmap", byteArray);
            savedInstanceState.putString("imageUri", uriImagenAltaCalidad.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}
