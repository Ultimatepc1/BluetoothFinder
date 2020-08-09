package com.example.bluetoothfinder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView deviceview;
    ArrayAdapter<String> deviceadapter;
    List<String> deviceslist,addresslist,rssilist;
    TextView statusview;
    Button searchbutton;
    BluetoothAdapter bluetoothAdapter;
    private  final BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction().toString();
            Log.i("Action ",action);
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                statusview.setText("Finished");
                searchbutton.setEnabled(true);
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name=device.getName();
                String address=device.getAddress();
                String rssi=Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE));
                Log.i("Device found","name "+name+" adderss "+address+" rssi "+rssi);
                if(!addresslist.contains(address)) {
                    if (name == null || name.equals("")) {
                        deviceslist.add(address + " - RSSI " + rssi + "dBm");
                    } else {
                        deviceslist.add(name + " - RSSI " + rssi + "dBm");
                    }
                    addresslist.add(address);
                    rssilist.add(rssi);
                }else{
                    int k=addresslist.indexOf(address);
                    if(!rssilist.get(k).equals(rssi)){
                       rssilist.set(k,rssi);
                        if (name == null || name.equals("")) {
                            deviceslist.set(k,address + " - RSSI " + rssi + "dBm");
                        } else {
                            deviceslist.set(k,name + " - RSSI " + rssi + "dBm");
                        }
                    }
                }
                deviceadapter.notifyDataSetChanged();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceview=(ListView)findViewById(R.id.devicesview);
        statusview=(TextView) findViewById(R.id.statustextview);
        searchbutton=(Button) findViewById(R.id.searchbutton);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        deviceslist=new ArrayList<String>();
        addresslist=new ArrayList<String>();
        rssilist=new ArrayList<String>();
        deviceadapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,deviceslist);
        deviceview.setAdapter(deviceadapter);
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support to Bluetooth", Toast.LENGTH_LONG).show();
        }
        //enable bt if is disabled
        if (!bluetoothAdapter.isEnabled()) {
            //enabled bluetooth
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
        switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            case PackageManager.PERMISSION_DENIED:
                ((TextView) new AlertDialog.Builder(this)
                        .setTitle("Runtime Permissions up ahead")
                        .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            2);
                                }
                            }
                        })
                        .show()
                        .findViewById(android.R.id.message))
                        .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                break;
            case PackageManager.PERMISSION_GRANTED:
                break;
        }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(broadcastReceiver, intentFilter);
            bluetoothAdapter.startDiscovery();
        }

    public void searchclick(View view){
        statusview.setText("Scanning");
        searchbutton.setEnabled(false);
        deviceslist.clear();
        addresslist.clear();
        rssilist.clear();
        deviceadapter.notifyDataSetChanged();
        bluetoothAdapter.startDiscovery();
    }
}