package tfg.sergio.bascula.bascula;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import tfg.sergio.bascula.MainActivity;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.Models.Silla;
import tfg.sergio.bascula.Pacientes.PacientesFragment;
import tfg.sergio.bascula.R;
import tfg.sergio.bascula.Resources.BluetoothUtils;
import tfg.sergio.bascula.Resources.FixedDatePicker;

/**
 * Created by yeyo on 06/06/2018.
 */

public class AniadirSillaFragment extends Fragment {
    private static final int BLUETOOTH_CODE = 2;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 3;
    private static final int SCAN_PERIOD = 2000;
    private static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID NOTIFY_UID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final String BASCULA_MAC = "30:AE:A4:06:55:16";
    private EditText inputModelo, inputPeso;
    private Button inputGuardar;
    private boolean conectado = false;

    //almacenamiento firebase
    private DatabaseReference mDatabaseSillas, mDatabasePacientes;

    private ProgressDialog progreso;
    private AlertDialog.Builder builder;
    private Paciente pacienteOriginal;

    //Bluetooth
    private boolean mScanning;
    private Map<String, BluetoothDevice> mScanResults;
    private ScanCallback mScanCallback;
    private boolean mConnected = false;
    private BluetoothGatt mGatt;
    private BluetoothAdapter mBluettothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    private ProgressDialog progressDialogDisp = null, progressDialog = null;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        return inflater.inflate(R.layout.fragment_aniadir_silla, null);    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluettothAdapter = bluetoothManager.getAdapter();

        Bundle bundle = getArguments();
        pacienteOriginal = bundle.getParcelable("paciente");
        mDatabaseSillas = FirebaseDatabase.getInstance().getReference("silla");
        mDatabasePacientes = FirebaseDatabase.getInstance().getReference("pacientes");

        inputModelo = (EditText) view.findViewById(R.id.modelo);
        inputPeso = view.findViewById(R.id.btn_peso_silla);
        inputGuardar =  view.findViewById(R.id.btn_guardar_silla);

        inputPeso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pesar silla");
        builder.setMessage("Por favor coloque la silla en la báscula y pulse aceptar");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Random r = new Random();
                if(mConnected){
                    enviarMensaje("P");
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });


        inputGuardar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String Modelo = String.valueOf(inputModelo.getText());
                double peso = 0;
                if(!inputPeso.getText().toString().equals("")){
                    peso = Double.parseDouble(inputPeso.getText().toString().replace("kg","").replace(",","."));
                }

                if(Modelo == null || peso == 0){
                    return;
                }
                else{
                    Silla silla = new Silla();
                    silla.peso = (double)peso;
                    silla.modelo = Modelo;
                    String id = mDatabaseSillas.push().getKey();
                    pacienteOriginal.setCodigoSilla(id);
                    mDatabaseSillas.child(pacienteOriginal.getCodigoSilla()).removeValue();
                    mDatabaseSillas.child(id).setValue(silla);

                    mDatabasePacientes.child(pacienteOriginal.getId()).setValue(pacienteOriginal);
                    FragmentManager fm = getFragmentManager();
                    disconnectGattServer();
                    fm.popBackStack();

                }
            }
        });
        progressDialogDisp = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_DARK);
        progressDialogDisp.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogDisp.setTitle("Conectando con dispositivo");
        progressDialogDisp.setMessage("Por favor espere...");
        progressDialogDisp.setCancelable(false);

        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Pesando");
        progressDialog.setMessage("Por favor espere...");
        progressDialog.setCancelable(false);
        progressDialogDisp.show();
        boolean result = comprobarPermisos();
    }

    //region bluetooth
    private void enviarMensaje(String msg){
        //Mostramos ventana de espera y bloqueamos acciones del usuario
        progressDialog.show();
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
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 5000);

    }

    private void escanear(){
        BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        assert manager != null;
        List<BluetoothDevice> adsf = manager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice dev:adsf)
        {
            if(dev.getAddress().equals(BASCULA_MAC) && mGatt == null){
                //getFragmentManager().popBackStackImmediate();
                reconnectGattServer(dev);
                progressDialogDisp.dismiss();
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
                    scanComplete();
                }

                mScanCallback = null;
                mScanning = false;
                mHandler = null;
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
        escanear();
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
                progressDialogDisp.dismiss();
            }
        }
        if(device == null){
            progressDialogDisp.dismiss();
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
                double peso = Double.parseDouble(message);
                setResult(String.format("%.2f" + "kg", peso));            }

        }
    }
    // Gatt connection

    private void connectDevice(BluetoothDevice device) {
        AniadirSillaFragment.GattClientCallback gattClientCallback = new AniadirSillaFragment.GattClientCallback();
        mGatt = device.connectGatt(getActivity(), false, gattClientCallback);
        ((MainActivity)getActivity()).setGatt(mGatt);
        progressDialogDisp.dismiss();
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void disconnectGattServer() {

        mConnected = false;

        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

    public void reconnectGattServer(BluetoothDevice device){
        AniadirSillaFragment.GattClientCallback gattClientCallback = new AniadirSillaFragment.GattClientCallback();
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

    //endregion

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_FINE_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                escanear();
            }else{
                Toast.makeText(getActivity(), "Por favor active la ubicación e inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                progressDialogDisp.dismiss();
                getFragmentManager().popBackStackImmediate();
                disconnectGattServer();
            }
        }
    }
    public void setResult(final String result){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputPeso.setText(result);
            }
        });
    }

    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == BLUETOOTH_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Por favor active el bluetooth e inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                progressDialogDisp.dismiss();
                getFragmentManager().popBackStackImmediate();
                disconnectGattServer();
            }
            else if(resultCode == Activity.RESULT_OK){
                escanear();
            }
        }

    }
}
