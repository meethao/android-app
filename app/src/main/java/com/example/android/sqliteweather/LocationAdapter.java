package com.example.android.sqliteweather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sqliteweather.data.ForecastData;
import com.example.android.sqliteweather.data.Location;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationHolder> {

    private List<Location> data;

    private OnClickLocationItemListener listener;

    public LocationAdapter(@NonNull OnClickLocationItemListener listener) {
        this.listener = listener;
    }

    public void setData(List<Location> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public interface OnClickLocationItemListener {
        void onClickLocationItem(Location location);
    }

    @NonNull
    @Override
    public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.location_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class LocationHolder extends RecyclerView.ViewHolder {

        private Location location;

        public LocationHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                listener.onClickLocationItem(location);
            });
        }

        public void bind(Location location) {
            this.location = location;
            ((TextView) itemView).setText(location.location);
        }
    }

}
