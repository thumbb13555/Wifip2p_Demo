package com.noahliu.wifip2p_demo.adapter;

import android.annotation.SuppressLint;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.noahliu.wifip2p_demo.Entity.SearchItem;
import com.noahliu.wifip2p_demo.R;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


public class SearchDeviceAdapter extends RecyclerView.Adapter<SearchDeviceAdapter.ViewHolder> {

    private final ArrayList<SearchItem> items = new ArrayList<>();
    private final HashSet<String> logAddress = new HashSet<String>();
    private OnItemClick click;

    public void setClick(OnItemClick click) {
        this.click = click;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle,tvDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.textView_Title);
            tvDetail = itemView.findViewById(R.id.textView_DeviceInfo);
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clearList(){
        logAddress.clear();
        items.clear();
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setList(Collection<WifiP2pDevice> devices){
        for (WifiP2pDevice device: devices) {
            if (!logAddress.contains(device.deviceAddress)){
                items.add(new SearchItem(device.deviceName,device.deviceAddress
                        ,device.status,device));
                logAddress.add(device.deviceAddress);
            }
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDetail.setText(item.getDeviceAddress()+"\n"+SearchItem.getDeviceStatus(item.getStatus()));
        holder.itemView.setOnClickListener(view -> click.callBack(item.getP2pDevice(),item.getDeviceAddress()));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public interface OnItemClick{
        void callBack(WifiP2pDevice device, String address);
    }
}
