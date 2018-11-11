package tfg.sergio.bascula.bascula;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.ActionMenuItem;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;
import tfg.sergio.bascula.Base.BaseFragment;
import tfg.sergio.bascula.MainActivity;
import tfg.sergio.bascula.Manifest;
import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.Models.RegistroPaciente;
import tfg.sergio.bascula.Pacientes.AniadirPacienteFragment;
import tfg.sergio.bascula.Pacientes.PacientesFragment;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Registro;
import tfg.sergio.bascula.Resources.BluetoothUtils;
import tfg.sergio.bascula.Resources.EnumIMC;
import tfg.sergio.bascula.Resources.EnumIMCFirebase;
import tfg.sergio.bascula.Resources.IMCCalculator;

/**
 * Created by sergio on 12/03/2018.
 */

public class basculaFragment extends BaseFragment implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener{
    private static final int MY_DATA_CHECK_CODE = 5;
    private static final int BLUETOOTH_CODE = 2;
    private Button add, tare;
    private  ImageButton btn_altura;
    private DatabaseReference mDatabase, mDatabase2, mDatabaseCentros, mDatabaseDatosMes;
    private String key = "";
    private int estado;
    private String centro, nombre;
    private Handler mHandler;
    private Paciente paciente;
    private TextToSpeech mTts;
    private Handler viewHandler;
    private Bundle bundle;
    private BluetoothAdapter mBluettothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mEchoInitialized;
    private ProgressDialog progressDialog = null, progressDialogDisp = null;
    private View view;
    private RadioGroup inputTipoMedida;
    private Paciente paciente_final;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 3;
    private static final int SCAN_PERIOD = 2000;
    private UUID SERVICE_UUID;
    private UUID CHARACTERISTIC_UUID;
    private UUID NOTIFY_UID;
    private String BASCULA_MAC;

    private BluetoothGattCharacteristic pruebacharac;
    private BluetoothGattDescriptor descPrueba;

    private boolean mScanning;
    private Map<String, BluetoothDevice> mScanResults;
    private ScanCallback mScanCallback;
    private boolean mConnected;
    private BluetoothGatt mGatt;
    private AlertDialog.Builder builder;
    private TextView tituloAltura, resultAltura, resultBascula;
    private FloatingTextButton ftb_continuar;
    private double peso = 0;
    private double altura =0;
    private double pesoSilla = 0;
    private double altura_original =0;
    private int tipoMedicion = 0; //0 peso, 1 altura

    private boolean reintentar = true;
    ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        viewHandler = new Handler();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_bascula, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            return;
        }
        this.view = view;
        BASCULA_MAC = getString(R.string.bascula_mac);
        SERVICE_UUID = UUID.fromString(getString(R.string.service_uuid));
        CHARACTERISTIC_UUID = UUID.fromString(getString(R.string.characteristic_uuid));
        NOTIFY_UID = UUID.fromString(getString(R.string.notify_uuid));
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluettothAdapter = bluetoothManager.getAdapter();
        mDatabase = FirebaseDatabase.getInstance().getReference("registros");
        mDatabase2 = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");
        ftb_continuar = view.findViewById(R.id.ab_continuar);
        progressBar = view.findViewById(R.id.spin_kit);
        Wave wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);

        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Pesando");
        progressDialog.setMessage("Por favor espere...");
        progressDialog.setCancelable(false);

        progressDialogDisp = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_DARK);
        progressDialogDisp.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogDisp.setTitle("Conectando con dispositivo");
        progressDialogDisp.setMessage("Por favor espere...");
        progressDialogDisp.setCancelable(false);
        tituloAltura = view.findViewById(R.id.titulo_altura);
        btn_altura = view.findViewById(R.id.btn_usar_altura);
        resultAltura =  view.findViewById(R.id.result_altura);
        resultBascula = view.findViewById(R.id.txt_result_bascula);


        bundle = getArguments();
        key = bundle.getString("key");
        paciente_final = bundle.getParcelable("paciente");
        estado = bundle.getInt("estado");
        centro = bundle.getString("centro");
        nombre = bundle.getString("nombre");
        pesoSilla = bundle.getDouble("silla");
        altura_original = bundle.getDouble("altura");
        add = view.findViewById(R.id.nuevoPeso);
        if(Configuration.ORIENTATION_LANDSCAPE == getActivity().getResources().getConfiguration().orientation){
            tituloAltura.setVisibility(View.INVISIBLE);
            resultAltura.setVisibility(View.GONE);
        }else{
            RelativeLayout.LayoutParams layoutParams =(RelativeLayout.LayoutParams) add.getLayoutParams();
            layoutParams.setMargins(0,300,0,0);

            add.setLayoutParams(layoutParams);

        }

        tare = view.findViewById(R.id.btn_tare);
        inputTipoMedida = view.findViewById(R.id.sw_tipo_medida);


        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Dispositivo no encontrado");
        builder.setMessage("Por favor compruebe que la báscula está encendida e inténtelo de nuevo.");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(mGatt != null){
                    mGatt.close();
                }
                getFragmentManager().popBackStackImmediate();
            }
        });
        btn_altura.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {
                altura=altura_original;
                setResult(String.format("%.2f" + "m",altura));
            }
        });
        inputTipoMedida.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int orientation = getActivity().getResources().getConfiguration().orientation;

                RadioButton selectedRadioButton = (RadioButton) view.findViewById(radioGroup.getCheckedRadioButtonId());
                String selectedRadioButtonText = selectedRadioButton.getText().toString();
                int colorFrom = getResources().getColor(R.color.LightYellow);
                int colorTo = getResources().getColor(R.color.LightGreen);
                if(selectedRadioButtonText.equals("Altura")){
                    colorFrom = getResources().getColor(R.color.LightGreen);
                    colorTo = getResources().getColor(R.color.LightYellow);
                    resultBascula.setVisibility(View.VISIBLE);
                    add.setText("Medir");
                    resultAltura.setVisibility(View.VISIBLE);
                    tituloAltura.setVisibility(View.VISIBLE);
                    btn_altura.setVisibility(View.VISIBLE);
                    tare.setVisibility(View.GONE);
                    resultAltura.setText(String.format("%.2f", altura_original));
                    tipoMedicion = 1;
                    resultBascula.setVisibility(View.VISIBLE);
                    resultBascula.setText(String.format("%.2f" + "m", altura));


                }else{
                    add.setText("Pesar");
                    tipoMedicion = 0;
                    if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                        tituloAltura.setVisibility(View.INVISIBLE);
                        resultAltura.setVisibility(View.GONE);
                        btn_altura.setVisibility(View.GONE);

                    }
                    else{
                        resultAltura.setVisibility(View.INVISIBLE);
                        tituloAltura.setVisibility(View.INVISIBLE);
                        btn_altura.setVisibility(View.INVISIBLE);

                    }
                    tare.setVisibility(View.VISIBLE);
                    resultBascula.setVisibility(View.VISIBLE);
                    resultBascula.setText(String.format("%.2f" + "kg", peso));

                }
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(250); // milliseconds
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        add.setBackgroundColor((int) animator.getAnimatedValue());
                    }

                });
                colorAnimation.start();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mConnected){
                    switch (tipoMedicion){
                        case 0:
                            progressDialog.setTitle("Pesando");
                            enviarMensaje("P");
                            break;
                        case 1:
                            progressDialog.setTitle("Midiendo");
                            enviarMensaje("M");
                            break;
                    }
                }
            }
        });

        ftb_continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final RegistroPaciente regis = new RegistroPaciente(key,peso,altura, Calendar.getInstance().getTime());

                progressBar.setVisibility(View.INVISIBLE);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack(null);
                bundle.putDouble("peso",peso);
                bundle.putDouble("altura",altura);
                bundle.putParcelable("paciente",paciente_final);
                basculaResultFragment fragment = new basculaResultFragment();
                fragment.setArguments(bundle);
                if(mGatt != null){
                    mGatt.close();
                }
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
            }
        });

        tare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensaje("T");
            }
        });
        if(comprobarPermisos()){
            progressDialogDisp.show();

            escanear();
        }
        else{
            mConnected = true;
        }
        super.onViewCreated(view, savedInstanceState);
    }

    private void enviarMensaje(String msg){
        //Mostramos ventana de espera y bloqueamos acciones del usuario
        progressDialog.show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 5000);
        BluetoothGattCharacteristic characteristic = BluetoothUtils.findEchoCharacteristic(mGatt);
        if (characteristic == null) {
            disconnectGattServer();
            return;
        }

        byte[] messageBytes = new byte[0];
        try {
            messageBytes = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (messageBytes.length == 0) {
            return;
        }

        characteristic.setValue(messageBytes);
        boolean success = mGatt.writeCharacteristic(characteristic);

        //Se establece un timeout para que  en caso de no obtener respuesta se rehabilite la pantalla.


    }


    //region bluetooth
    private void escanear(){
        BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        assert manager != null;
        List<BluetoothDevice>adsf = manager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice dev:adsf)
        {
            if(dev.getAddress().equals(BASCULA_MAC) && mGatt == null){
                //getFragmentManager().popBackStackImmediate();
                reconnectGattServer(dev);
                progressDialogDisp.dismiss();
                habla("Por favor, colócate sobre la báscula y pulsa el botón.");
                return;
            }
        }


        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        mScanResults = new HashMap<>();
        mScanCallback = new BtleScanCallback(mScanResults);
        mBluetoothLeScanner = mBluettothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters,settings,mScanCallback);
        mScanning = true;
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScanning && mBluettothAdapter != null && mBluettothAdapter.isEnabled() && mBluetoothLeScanner != null) {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    mScanCallback = null;
                    mScanning = false;
                    mHandler = null;
                    scanComplete();
                }
                else{

                    progressDialogDisp.dismiss();
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }, 10000);

    }

    private class BtleScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> mScanResults;

        BtleScanCallback(Map<String, BluetoothDevice> scanResults) {
            mScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
        }
        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(deviceAddress, device);
        }
    };

    private boolean comprobarPermisos(){
        boolean ret = true;
        if(mBluettothAdapter == null || !mBluettothAdapter.isEnabled()) {
            habilitarBluetooth();
            ret=false;

        }
        if(!tienePermisoUbicacion()){
            habilitarUbicacion();
            ret= false;
        }
        return ret;
    }

    private void habilitarBluetooth(){
        Intent habilitarBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(habilitarBt, BLUETOOTH_CODE);
    }

    private boolean tienePermisoUbicacion(){
        return ActivityCompat.checkSelfPermission(getActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void habilitarUbicacion(){
        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_FINE_LOCATION);
    }

    private void scanComplete() {
        BluetoothDevice device = null;

        for (String deviceAddress : mScanResults.keySet()) {
            String prueba = deviceAddress;
            if(deviceAddress.equals(BASCULA_MAC)){
                device = mScanResults.get(deviceAddress);
                connectDevice(device);
                reintentar = false;
                habla("Por favor, colócate sobre la báscula y pulsa el botón.");
                progressDialogDisp.dismiss();
            }
        }
        if(device == null && !reintentar){

            progressDialogDisp.dismiss();
            AlertDialog alert = builder.create();
            alert.show();
        }else if(reintentar == true){
            reintentar = false;
            escanear();
        }
    }

    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                disconnectGattServer();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                setConnected(true);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            List<BluetoothGattCharacteristic> matchingCharacteristics = BluetoothUtils.findCharacteristics(gatt);

            for (BluetoothGattService gattService : gatt.getServices()) {
                for (BluetoothGattCharacteristic mCharacteristic : gattService.getCharacteristics()) {
                    mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    enableCharacteristicNotification(gatt, mCharacteristic);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            //Se devuelve el control al usuario
            progressDialog.dismiss();

            byte[] messageBytes = characteristic.getValue();
            String message = null;
            try {
                message = new String(messageBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (message == null) {
                return;
            }else{
                switch (tipoMedicion){
                    case 0:
                            peso = Double.parseDouble(message) - pesoSilla;
                            setResult(String.format("%.2f" + "kg", peso));
                        break;
                    case 1:
                            altura = Double.parseDouble(message);
                            altura = (altura/1000) - 0.4;
                            setResult(String.format("%.2f" + "m", altura));
                        break;
                }
            }

        }
    }
    // Gatt connection

    private void connectDevice(BluetoothDevice device) {
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = device.connectGatt(getActivity(), false, gattClientCallback);
        ((MainActivity)getActivity()).setGatt(mGatt);
        progressDialogDisp.dismiss();
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void disconnectGattServer() {

        mConnected = false;
        mEchoInitialized = false;

        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void reconnectGattServer(BluetoothDevice device){
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = device.connectGatt(getActivity(), false, gattClientCallback);
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
        mGatt = device.connectGatt(getActivity(), false, gattClientCallback);
        ((MainActivity)getActivity()).setGatt(mGatt);
    }

    private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(NOTIFY_UID) &&  gatt.setCharacteristicNotification(characteristic, true)) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            if (descriptor != null) {
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                } else {
                    // The characteristic does not have NOTIFY or INDICATE property set;
                }

                if (gatt.writeDescriptor(descriptor)) {
                    // Success
                } else {
                    // Failed to set client characteristic notification;
                }
            } else {
                // Failed to set client characteristic notification;
            }
        } else {
            // Failed to register notification;
        }
    }
    public void initializeEcho() {
        mEchoInitialized = true;
    }

    //endregion



    public void setResult(final String result){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(peso!=0 && altura!=0){
                    ftb_continuar.setVisibility(View.VISIBLE);
                }
                resultBascula.setVisibility(View.VISIBLE);
                resultBascula.setText(result);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_FINE_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                escanear();
            }else{
                Toast.makeText(getActivity(), "Por favor active la ubicación e inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                progressDialogDisp.dismiss();
                if(mGatt != null){
                    mGatt.close();
                }                getFragmentManager().popBackStackImmediate();
            }
        }
    }

    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(getActivity(), this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
        else if (requestCode == BLUETOOTH_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Por favor active el bluetooth e inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                progressDialogDisp.dismiss();
                if(mGatt != null){
                    mGatt.close();
                }

                getFragmentManager().popBackStackImmediate();
            }
            else if(resultCode == Activity.RESULT_OK){
                escanear();
            }
        }

    }

    private void habla(String mensaje){
        if(mTts == null){
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(""));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
        mTts.speak(mensaje, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    @Override
    public void onInit(int i) {
        mTts.setLanguage(new Locale(Locale.getDefault().getLanguage()));
        mTts.setOnUtteranceCompletedListener(this);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
        viewHandler.post(run);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTts!=null){
            mTts.shutdown(); //mTts is your TextToSpeech Object
        }
    }

    @Override
    public void onBackPressed() {
        if(mGatt != null){
            try {
                // BluetoothGatt gatt
                final Method refresh = mGatt.getClass().getMethod("refresh");
                if (refresh != null) {
                    refresh.invoke(mGatt);
                }
            } catch (Exception e)
            {
                // Log it
            }
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }
}
