package tfg.sergio.bascula.bascula;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;
import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.EnumIMCFirebase;
import tfg.sergio.bascula.Resources.IMCCalculator;

/**
 * Created by yeyo on 15/04/2018.
 */

public class basculaResultFragment extends Fragment{
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {

    };
    private Bundle bundle;
    private String key = "";
    private int estado;
    private String centro, nombre;
    private Double peso,altura;
    private DatabaseReference mDatabase, mDatabase2, mDatabaseCentros, mDatabaseDatosMes;
    private StorageReference mStorage;
    private Paciente paciente;
    private FloatingTextButton aceptar,cancelar;
    private TextView textIMC,textPeso;
    private Paciente paciente_final;
    private ImageButton inputFoto;
    private Uri uriImagenAltaCalidad = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    private ProgressDialog progreso;
    private static DecimalFormat df2 = new DecimalFormat(".##");
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
        return inflater.inflate(R.layout.fragment_result_bascula, null);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        aceptar = view.findViewById(R.id.btn_aceptar);
        cancelar = view.findViewById(R.id.btn_cancelar);
//
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//
        addTabs();
//
//        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(viewPager);
        mStorage = FirebaseStorage.getInstance().getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference("registros");
        mDatabase2 = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");
        textPeso = view.findViewById(R.id.txt_peso);
        textIMC = view.findViewById(R.id.txt_imc);
        bundle = getArguments();
        key = bundle.getString("key");
        estado = bundle.getInt("estado");
        centro = bundle.getString("centro");
        nombre = bundle.getString("nombre");
        peso = bundle.getDouble("peso");
        altura = bundle.getDouble("altura");
        paciente_final = bundle.getParcelable("paciente");

        inputFoto = (ImageButton) view.findViewById(R.id.imagen_paciente);
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

        progreso = new ProgressDialog(getActivity());
        IMCCalculator imcCalc = new IMCCalculator();
        double imc = IMCCalculator.CalcularIMC(peso,altura);
        textPeso.setText(""+df2.format(peso) + " Kg");
        textIMC.setText(""+df2.format(imc));
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

        //Botón foto
        inputFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verificación de permisos
                verifyStoragePermissions(getActivity());
                uriImagenAltaCalidad = generateTimeStampPhotoFileUri();
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //camera_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT,uriImagenAltaCalidad);
                startActivityForResult(camera_intent,CAMERA_REQUEST_CODE);
            }
        });
    }



    //region Camara y guardado de imágenes

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

    private Uri generateTimeStampPhotoFileUri() {

        Uri photoFileUri = null;
        File outputDir = getPhotoDirectory();
        if (outputDir != null) {
            Time t = new Time();
            t.setToNow();
            File photoFile = new File(outputDir, System.currentTimeMillis() + ".jpg");
            photoFileUri = FileProvider.getUriForFile(this.getActivity(), this.getActivity().getApplicationContext().getPackageName() + ".my.package.name.provider", photoFile);
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
            if(uriImagenAltaCalidad == null){
                Toast.makeText(getActivity(), "Por favor mantenga la orientación de la cámara al hacer la foto.", Toast.LENGTH_LONG).show();
                return;
            }
            inputFoto.setScaleType(ImageView.ScaleType.FIT_XY);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

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
    //endregion


    private void addTabs() {

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate("detalle",0);
            }
        });
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RegistroPaciente regis = new RegistroPaciente(key,peso,altura, Calendar.getInstance().getTime());
                final String id = mDatabase.push().getKey();
                regis.setTimeStamp(Calendar.getInstance().getTimeInMillis());
                int IMC = IMCCalculator.Calcular(paciente_final.monthsBetweenDates(),regis.getIMC(),paciente_final.getSexo());
                regis.setCodigoEstadoIMC(IMC);

                //Guardado del registro en Firebase
                if(uriImagenAltaCalidad == null){
                    mDatabase.child(id).setValue(regis);
                }
                else {


                    //Guardado del registro en Firebase
                    progreso.setMessage("Guardando registro ...");
                    progreso.show();
                    StorageReference path = mStorage.child("Fotos_peso_pacientes").child(uriImagenAltaCalidad.getLastPathSegment());
                    Bitmap bmp = null;
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriImagenAltaCalidad);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                    byte[] data = baos.toByteArray();
                    UploadTask uploadTask2 = path.putBytes(data);
                    uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            regis.setUrlFoto(taskSnapshot.getDownloadUrl().toString());
                            regis.setArchivoFoto(uriImagenAltaCalidad.getLastPathSegment());
                            mDatabase.child(id).setValue(regis);
                            progreso.dismiss();
                            //Toast.makeText(getActivity(), "Upload successful", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progreso.dismiss();
                            // Toast.makeText(getActivity(), "Upload Failed -> " + e, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                //Actualización de referencia de último registro de paciente
                try {
                    mDatabase2.child(key).child("CodigoUltimoRegistro").setValue(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDatabase2.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        paciente = dataSnapshot.getValue(Paciente.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                DateFormat dateFormat = new SimpleDateFormat("YYYYMM");
                Date date = new Date();
                final String ident = centro +dateFormat.format(date);
                Query firebaseSearchQuery = mDatabaseDatosMes.orderByChild("id").startAt(ident).endAt(ident);


                //actualización recuento por centros
                firebaseSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        PacientesMesCentro pmc = null;
                        IMCCalculator imcCalculator = new IMCCalculator();
                        int imcNuevo = imcCalculator.Calcular(paciente.monthsBetweenDates(),regis.getIMC(),paciente.getSexo());
                        String Key="";
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            System.out.println(child.getKey());
                            pmc = child.getValue(PacientesMesCentro.class);
                            Key = child.getRef().getKey();
                        }
                        if(pmc != null){
                            if(pmc.obtenerNumPacientes(estado) == 0){

                            }
                            else{
                                try {
                                    mDatabaseDatosMes.child(Key).child(EnumIMCFirebase.values()[estado].toString()).setValue(pmc.obtenerNumPacientes(estado)-1);
                                    pmc.ActualizarNumPacientes(estado,0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                            try {
                                mDatabaseDatosMes.child(Key).child(EnumIMCFirebase.values()[imcNuevo].toString()).setValue(pmc.obtenerNumPacientes(imcNuevo)+1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            String id2 = mDatabaseDatosMes.push().getKey();
                            PacientesMesCentro newpmc= new PacientesMesCentro(ident);
                            newpmc.ActualizarNumPacientes(imcNuevo,1);
                            mDatabaseDatosMes.child(id2).setValue(newpmc);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                getFragmentManager().popBackStackImmediate("detalle",0);

            }
        });

    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            Bundle bnd = getArguments();
            Bundle myBundle = new Bundle();
            myBundle.putDouble( "peso", bnd.getDouble("peso"));
            myBundle.putDouble("altura", bnd.getDouble("altura"));
            fragment.setArguments( myBundle  );
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
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

