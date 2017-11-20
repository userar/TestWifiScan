package com.example.alexander.testwifiscan;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Alex Royds
 */

public class HiddenNetworkListAdapter extends ArrayAdapter<ScanResult>
{
    public HiddenNetworkListAdapter(@NonNull Context context, int resource)
    {
        super(context, resource);
    }
    
    @NonNull
    @Override
    public View getView(int position, View convert_view, @NonNull ViewGroup parent)
    {
        
        View row_view = convert_view;
        
        if (row_view == null)
        {
            LayoutInflater layout_inflater;
            layout_inflater = LayoutInflater.from(getContext());
            row_view = layout_inflater.inflate(R.layout.hidden_network_list_row, null);
        }
        
        ScanResult scan_result = getItem(position);
        
        if (scan_result != null)
        {
            TextView bssid_value_view = row_view.findViewById(R.id.bssid_value);
            TextView rssi_value_view = row_view.findViewById(R.id.rssi_value);
    
            bssid_value_view.setText(scan_result.BSSID);
            rssi_value_view.setText(String.valueOf(scan_result.level));
        }
        
        return row_view;
    }
}
