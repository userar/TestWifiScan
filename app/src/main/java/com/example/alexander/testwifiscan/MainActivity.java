package com.example.alexander.testwifiscan;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    
    private static final int PERMISSIONS_REQUEST_FOR_LOCATION = 1;
    private WifiManager wifi_manager;
    private WifiReceiver wifi_receiver;
    private final Handler handler = new Handler();
    private TextView result_count_textview;
    private TextView average_hidden_textview;
    private HiddenNetworkListAdapter hidden_network_adapter;
    private List<ScanResult> hidden_wifi_list = new ArrayList<>();
    private int scan_result_count = 0;
    private LinkedList<Integer> moving_average_queue = new LinkedList<>();
    private double average_hidden_reported;
    private static final int MOVING_AVERAGE_SIZE = 20;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        this.result_count_textview = findViewById(R.id.total_scan_value);
        this.average_hidden_textview = findViewById(R.id.avg_scan_count_value);
        ListView hidden_networks_listview = findViewById(R.id.hidden_network_list);
        this.hidden_network_adapter = new HiddenNetworkListAdapter(
                this,
                R.layout.hidden_network_list_row);
        hidden_networks_listview.setAdapter(this.hidden_network_adapter);
        
        wifi_manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi_receiver = new WifiReceiver();
        EnableWifi();
        RequestLocationPermission();
    }
    
    @Override
    protected void onPause()
    {
        unregisterReceiver(wifi_receiver);
        super.onPause();
    }
    
    @Override
    protected void onResume()
    {
        registerReceiver(wifi_receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] results)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_FOR_LOCATION:
            {
                if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)
                {
                    StartWifiScan();
                }
                else
                {
                    Toast.makeText(
                            this, 
                            "The location permission is required for wifi scanning!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    private void EnableWifi()
    {
        if (!this.wifi_manager.isWifiEnabled())
        {
            this.wifi_manager.setWifiEnabled(true);
        }
    }
    
    private void RequestLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) 
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FOR_LOCATION);
            return;
        }
        StartWifiScan();
    }
    
    private void StartWifiScan()
    {
        handler.postDelayed(new Runnable() {
        
            @Override
            public void run()
            {
                wifi_manager.startScan();
                StartWifiScan();
            }
        }, 5000); //5 seconds
    }
    
    private class WifiReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> wifi_list = wifi_manager.getScanResults();
            hidden_wifi_list.clear();
            for (ScanResult scan_result : wifi_list)
            {
                if (scan_result.SSID.equals(""))
                {
                    hidden_wifi_list.add(scan_result);
                }
            }
            
            //Update the 'onRecieve' count and average hidden networks reported
            result_count_textview.setText(String.valueOf(++scan_result_count));
            average_hidden_textview.setText(String.valueOf(GetMovingAverage(hidden_wifi_list.size())));
            
            //Update the listview with the latest hidden network results
            hidden_network_adapter.clear();
            hidden_network_adapter.addAll(hidden_wifi_list);
            hidden_network_adapter.notifyDataSetChanged();
        }
    }
    
    public double GetMovingAverage(int val)
    {
        if(moving_average_queue.size() < MOVING_AVERAGE_SIZE)
        {
            moving_average_queue.offer(val);
            int sum = 0;
            for(int i : moving_average_queue)
            {
                sum += i;
            }
            average_hidden_reported = (double) sum / moving_average_queue.size();
            
            return average_hidden_reported;
        }
        else
        {
            int head = moving_average_queue.poll();
            double minus = (double) head / MOVING_AVERAGE_SIZE;
            moving_average_queue.offer(val);
            double add = (double) val / MOVING_AVERAGE_SIZE;
            average_hidden_reported = average_hidden_reported + add - minus;
            return average_hidden_reported;
        }
    }
}
