package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Priority;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.ViewHolder> {

    private List<Stop> stops = List.of();
    private final List<Priority> priorities = new ArrayList<>();

    public void setStops(final List<Stop> newStops) {
        stops = newStops;
        setPriorities(getPriorities(newStops));
        notifyDataSetChanged();
    }

    public List<Stop> getStops() {
        return Lists
                .zip(stops, priorities)
                .stream()
                .map(stop_priority -> asStopWithPriority(stop_priority.first, stop_priority.second))
                .toList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final ViewHolder holder =
                new ViewHolder(
                        LayoutInflater
                                .from(parent.getContext())
                                .inflate(R.layout.item_stop, parent, false));
        holder.spinnerPriority.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(final AdapterView<?> parent,
                                               final View view,
                                               final int spinnerPosition,
                                               final long id) {
                        final int adapterPos = holder.getBindingAdapterPosition();
                        if (adapterPos != RecyclerView.NO_POSITION) {
                            priorities.set(adapterPos, (Priority) parent.getItemAtPosition(spinnerPosition));
                        }
                    }

                    @Override
                    public void onNothingSelected(final AdapterView<?> parent) {
                    }
                });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Stop stop = stops.get(position);
        holder.tvAddress.setText(stop.address());
        holder.spinnerPriority.setSelection(holder.spinnerAdapter.getPosition(priorities.get(position)));
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    private void setPriorities(final List<Priority> priorities) {
        this.priorities.clear();
        this.priorities.addAll(priorities);
    }

    private static List<Priority> getPriorities(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::priority)
                .toList();
    }

    private static Stop asStopWithPriority(final Stop stop, final Priority priority) {
        return new Stop(
                stop.id(),
                stop.address(),
                stop.placeId(),
                stop.geodetic(),
                priority);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvAddress;
        public final Spinner spinnerPriority;
        public final ArrayAdapter<Priority> spinnerAdapter;

        public ViewHolder(final View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            spinnerPriority = itemView.findViewById(R.id.spinnerPriority);
            spinnerAdapter = createAndConfigureSpinnerAdapter(itemView.getContext());
            spinnerPriority.setAdapter(spinnerAdapter);
        }

        private static ArrayAdapter<Priority> createAndConfigureSpinnerAdapter(final Context context) {
            final ArrayAdapter<Priority> spinnerAdapter =
                    new ArrayAdapter<>(
                            context,
                            android.R.layout.simple_spinner_item,
                            Priority.values());
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            return spinnerAdapter;
        }
    }
}
