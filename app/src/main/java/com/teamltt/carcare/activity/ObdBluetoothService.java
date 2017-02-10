package com.teamltt.carcare.activity;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.teamltt.carcare.adapter.IObdSocket;
import com.teamltt.carcare.adapter.bluetooth.DeviceSocket;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.contract.ResponseContract;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Any Activity can start this service to request for the bluetooth to start logging OBD data to the database
 */
public class ObdBluetoothService extends Service {

    // Text that appears in the phone's bluetooth manager for the adapter
    private final String OBDII_NAME = "OBDII";
    // UUID that is required to talk to the OBD II adapter
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice obdDevice = null;
    private BluetoothAdapter bluetoothAdapter;
    private IObdSocket socket;

    /**
     * A writable database connection
     */
    private SQLiteDatabase db;


    /**
     * Used to catch bluetooth status related events.
     * Relies on the Android Manifest including android.bluetooth.adapter.action.STATE_CHANGED in a receiver tag
     */
    BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("bluetooth", "bt state receiver " + action);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state){
                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        getObdDevice();
                        break;

                }
            }
        }
    };

    /**
     * Used to catch bluetooth discovery related events.
     */
    BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("bluetooth", "discovery receiver " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("bluetooth", "potential device found");
                Log.i("bluetooth", device.toString());
                if (device.getName() != null && device.getName().equals(OBDII_NAME)) {
                    Log.i("bluetooth", "desired device found");
                    obdDevice = device;
                    obdDeviceObtained();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("bluetooth", "discovery unsuccessful");
                // UI message connectButton.setText(R.string.retry_connect);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i("bluetooth", "discovery started");
            }
        }
    };

    /**
     * Activities wanting to use this service will register with it instead of binding, but in the absence of all
     * activities, data should still be logged.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("This Service does not support binding.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("bluetooth", "service started");
        // Register the BroadcastReceiver
        IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, discoveryFilter);

        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, bluetoothFilter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            // Do not try to connect if the device is already trying to or if our socket is open

            if (!bluetoothAdapter.isEnabled()) {
                Log.i("bluetooth", "requesting to enable BT");

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // Gives the user a chance to reject Bluetooth privileges at this time
                startActivity(enableBtIntent);
                // goes to onActivityResult where requestCode == REQUEST_ENABLE_BT
            }
            else {
                Log.i("bluetooth", "adapter enabled");
                // If bluetooth is on, go ahead and use it
                getObdDevice();
            }
        } // Else does not support Bluetooth

        // START_NOT_STICKY means when the service is stopped/killed, do NOT automatically restart the service
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        // release resources
        unregisterReceiver(discoveryReceiver);
        unregisterReceiver(bluetoothReceiver);
        Log.i("bluetooth", "service stopped");
    }

    /**
     * Returns a cached bluetooth device named OBDII_NAME or starts a discovery and bonds with a device like that.
     */
    private void getObdDevice() {
        // Query pair
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(OBDII_NAME)) {
                    obdDevice = device;
                    break;
                }
            }
        }
        if (obdDevice != null) {
            /* TODO a cached pair for device named "OBDII_NAME" is found if you have connected successfully. But what if
            you try using a new adapter with a new MAC address - this will need to be discovered */
            Log.i("bluetooth", "existing pair found");
            obdDeviceObtained();
        } else {
            Log.i("bluetooth", "starting discovery");
            if (bluetoothAdapter.startDiscovery()) {
                // Control goes to bluetoothReceiver member variable
                Log.i("bluetooth", "discovery started");
                // UI message connectButton.setText(R.string.discovering);
            } else {
                Log.i("bluetooth", "discovery not started");
                // UI message connectButton.setText(R.string.permission_fail_bt);
            }
        }
    }

    private void obdDeviceObtained() {
        Log.i("bluetooth", "trying socket creation");
        try {
            socket = new DeviceSocket(obdDevice.createRfcommSocketToServiceRecord(uuid));
            // UI message connectButton.setText(R.string.connecting_bt);

            connectTask.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This task takes control of the device's bluetooth and opens a socket to the OBD adapter.
     */
    AsyncTask<Void, Void, Void> connectTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Android advises to cancel discovery before using socket.connect()
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                // Connect to the database
                DbHelper dbHelper = new DbHelper(ObdBluetoothService.this);
                db = dbHelper.getWritableDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Add user feedback with Messenger and Handler

            queryTask.execute();
        }
    };

    /**
     * This task checks periodically if the car is on, then starts cycling through commands that CarCare tracks and
     * storing them in the database.
     */
    AsyncTask<Void, Void, Void> queryTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... ignore) {
            try {
                while (socket.isConnected()) {
                    // Check for can's "heartbeat"
                    boolean isCarOn = false;
                    while (!isCarOn) {
                        ObdCommand heartbeat = new RuntimeCommand();
                        heartbeat.run(socket.getInputStream(), socket.getOutputStream());

                        if (Integer.parseInt(heartbeat.getFormattedResult()) > 0) {
                            isCarOn = true;
                        }
                    }

                    Set<Class<? extends ObdCommand>> commands = new HashSet<>();
                    // TODO get these classes from somewhere else
                    commands.add(RuntimeCommand.class);
                    commands.add(SpeedCommand.class);

                    for (Class<? extends ObdCommand> commandClass : commands) {
                        ObdCommand sendCommand = commandClass.newInstance();
                        if (!socket.isConnected()) {
                            // In case the Bluetooth connection breaks suddenly
                            break;
                        } else {
                            sendCommand.run(socket.getInputStream(), socket.getOutputStream());
                        }

                        // Add this response to the database
                        ContentValues values = new ContentValues();
                        // TODO trip_id logic?
                        values.put(ResponseContract.ResponseEntry.COLUMN_NAME_TRIP_ID, 0);
                        values.put(ResponseContract.ResponseEntry.COLUMN_NAME_NAME, sendCommand.getName());
                        values.put(ResponseContract.ResponseEntry.COLUMN_NAME_PID, sendCommand.getCommandPID());
                        values.put(ResponseContract.ResponseEntry.COLUMN_NAME_VALUE, sendCommand.getFormattedResult());

                        db.insert(ResponseContract.ResponseEntry.TABLE_NAME, null, values);
                    }
                }
            } catch (IOException | InterruptedException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(Void ignore) {
            // TODO Tell the user the socket was disconnected or there was another exception
            // Also give them a way to reconnect it
        }

    };
}
