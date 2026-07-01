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
import java.util.stream.IntStream;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.ViewHolder> {

    private List<Stop> stops = List.of();
    private final List<Integer> priorities = new ArrayList<>();

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
        return new ViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_stop, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Stop stop = stops.get(position);
        holder.tvAddress.setText(stop.address());
        holder.spinnerPriority.setAdapter(createAndConfigureSpinnerAdapter(holder.itemView.getContext()));
        holder.spinnerPriority.setSelection(priorities.get(position) - 1);
        holder.spinnerPriority.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                        final int adapterPos = holder.getBindingAdapterPosition();
                        if (adapterPos != RecyclerView.NO_POSITION) {
                            priorities.set(adapterPos, position + 1);
                        }
                    }

                    @Override
                    public void onNothingSelected(final AdapterView<?> parent) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    private void setPriorities(final List<Integer> priorities) {
        this.priorities.clear();
        this.priorities.addAll(priorities);
    }

    private static ArrayAdapter<Integer> createAndConfigureSpinnerAdapter(final Context context) {
        final ArrayAdapter<Integer> spinnerAdapter =
                new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_spinner_item,
                        IntStream.rangeClosed(1, 10).boxed().toList());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerAdapter;
    }

    private static List<Integer> getPriorities(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::priority)
                .toList();
    }

    private static Stop asStopWithPriority(final Stop stop, final int priority) {
        return new Stop(
                stop.id(),
                stop.address(),
                stop.placeId(),
                stop.geodetic(),
                priority);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvAddress;
        public Spinner spinnerPriority;

        public ViewHolder(final View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            spinnerPriority = itemView.findViewById(R.id.spinnerPriority);
        }
    }
}
