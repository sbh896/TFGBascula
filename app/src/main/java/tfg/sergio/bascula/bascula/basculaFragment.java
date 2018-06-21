package tfg.sergio.bascula.bascula;

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
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

public class basculaFragment extends Fragment implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    private static final int MY_DATA_CHECK_CODE = 1;
    private Button add;
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
    private ProgressDialog progressDialog = null;
    private View view;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int SCAN_PERIOD = 2000;
    private static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID NOTIFY_UID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private BluetoothGattCharacteristic pruebacharac;
    private BluetoothGattDescriptor descPrueba;

    private boolean mScanning;
    private Map<String, BluetoothDevice> mScanResults;
    private ScanCallback mScanCallback;
    private boolean mConnected;
    private BluetoothGatt mGatt;
    private AlertDialog.Builder builder;




    ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        viewHandler = new Handler();
        return inflater.inflate(R.layout.fragment_bascula, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            return;
        }
        this.view = view;
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluettothAdapter = bluetoothManager.getAdapter();
        mDatabase = FirebaseDatabase.getInstance().getReference("registros");
        mDatabase2 = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");
        progressBar = view.findViewById(R.id.spin_kit);
        Wave wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);

        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_DARK);
// set indeterminate style
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
// set title and message
        progressDialog.setTitle("Pesando");
        progressDialog.setMessage("Por favor espere...");
        progressDialog.setCancelable(false);
        bundle = getArguments();
        key = bundle.getString("key");
        estado = bundle.getInt("estado");
        centro = bundle.getString("centro");
        nombre = bundle.getString("nombre");
        add = view.findViewById(R.id.nuevoPeso);

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Dispositivo no encontrado");
        builder.setMessage("Por favor compruebe que la báscula está encendida e inténtelo de nuevo.");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getFragmentManager().popBackStackImmediate();
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Random rand = new Random();
//                Double n = rand.nextDouble() * (20) ;
//                Double m = rand.nextDouble() * (1.90);
//
//                final RegistroPaciente regis = new RegistroPaciente(key,n,m, Calendar.getInstance().getTime());
//
//                progressBar.setVisibility(View.INVISIBLE);
//                FragmentManager fm = getFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.addToBackStack("bascula");
//                bundle.putDouble("peso",n);
//                bundle.putDouble("altura",m);
//                basculaResultFragment fragment = new basculaResultFragment();
//                fragment.setArguments(bundle);
//                ft.replace(R.id.pacientes_screen,fragment);
//                ft.commit();
//                Toast.makeText(getActivity(), "new one", Toast.LENGTH_SHORT).show();
                //Mostramos ventana de espera y bloqueamos acciones del usuario
                progressDialog.show();
                BluetoothGattCharacteristic characteristic = BluetoothUtils.findEchoCharacteristic(mGatt);
                if (characteristic == null) {
                    disconnectGattServer();
                    return;
                }
                String message = "P";

                byte[] messageBytes = new byte[0];
                try {
                    messageBytes = message.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (messageBytes.length == 0) {
                    return;
                }

                characteristic.setValue(messageBytes);
                boolean success = mGatt.writeCharacteristic(characteristic);

                //Se establece un timeout para que  en caso de no obtener respuesta se rehabilite la pantalla.
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 10000);
            }
        });
        escanear();
        super.onViewCreated(view, savedInstanceState);
    }

    private void habilitar_pantalla(){

    }


    //region bluetooth
    private void escanear(){
        if(!comprobarPermisos() ){//|| mScanning){
            return;
        }
        BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        assert manager != null;
        List<BluetoothDevice>adsf = manager.getConnectedDevices(BluetoothProfile.GATT);
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
                    scanComplete();
                }

                mScanCallback = null;
                mScanning = false;
                mHandler = null;
            }
        }, 5000);

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
        if(mBluettothAdapter == null || !mBluettothAdapter.isEnabled()) {
            habilitarBluetooth();
            return false;
        }else if(!tienePermisoUbicacion()){
            habilitarUbicacion();
            return false;
        }
        return true;
    }

    private void habilitarBluetooth(){
        Intent habilitarBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(habilitarBt, REQUEST_ENABLE_BT);
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
            if(deviceAddress.equals("30:AE:A4:06:55:16")){
                device = mScanResults.get(deviceAddress);
                connectDevice(device);
            }
        }
        if(device == null){
            AlertDialog alert = builder.create();
            alert.show();
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
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
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
                int i = 1;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (message == null) {
                return;
            }

        }
    }
    // Gatt connection

    private void connectDevice(BluetoothDevice device) {
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = device.connectGatt(getActivity(), false, gattClientCallback);
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
//        List<BluetoothGattDescriptor> descs= characteristic.getDescriptors();
//        for(BluetoothGattDescriptor descriptor : descs) {
//            boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//            boolean status = gatt.writeDescriptor(descriptor);
//            pruebacharac = characteristic;
//
//        }
    }
    public void initializeEcho() {
        mEchoInitialized = true;
    }

    //endregion





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

    }

    @Override
    public void onInit(int i) {
        mTts.setLanguage(new Locale(Locale.getDefault().getLanguage()));
        mTts.setOnUtteranceCompletedListener(this);

        String text = "Por favor, colócate sobre la báscula y pulsa el botón.";
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
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
}
