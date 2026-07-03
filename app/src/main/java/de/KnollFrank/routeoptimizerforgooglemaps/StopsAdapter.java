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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroups;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.ViewHolder> {

    private List<Stop> stops = List.of();
    private final List<Optional<DeliveryGroup>> deliveryGroups = new ArrayList<>();

    public void setStops(final List<Stop> newStops) {
        stops = newStops;
        setDeliveryGroups(getDeliveryGroups(newStops));
        notifyDataSetChanged();
    }

    public List<Stop> getStops() {
        return Lists
                .zip(stops, deliveryGroups)
                .stream()
                .map(stop_deliveryGroup -> asStopWithDeliveryGroup(stop_deliveryGroup.first, stop_deliveryGroup.second))
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
                            deliveryGroups.set(adapterPos, (Optional<DeliveryGroup>) parent.getItemAtPosition(spinnerPosition));
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
        holder.spinnerPriority.setSelection(holder.spinnerAdapter.getPosition(deliveryGroups.get(position)));
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    private void setDeliveryGroups(final List<Optional<DeliveryGroup>> deliveryGroups) {
        this.deliveryGroups.clear();
        this.deliveryGroups.addAll(deliveryGroups);
    }

    private static List<Optional<DeliveryGroup>> getDeliveryGroups(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::deliveryGroup)
                .toList();
    }

    private static Stop asStopWithDeliveryGroup(final Stop stop, final Optional<DeliveryGroup> deliveryGroup) {
        return new Stop(
                stop.id(),
                stop.address(),
                stop.placeId(),
                stop.geodetic(),
                deliveryGroup,
                stop.timeWindow());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvAddress;
        public final Spinner spinnerPriority;
        public final ArrayAdapter<Optional<DeliveryGroup>> spinnerAdapter;

        public ViewHolder(final View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            spinnerPriority = itemView.findViewById(R.id.spinnerPriority);
            spinnerAdapter = createAndConfigureSpinnerAdapter(itemView.getContext());
            spinnerPriority.setAdapter(spinnerAdapter);
        }

        private static ArrayAdapter<Optional<DeliveryGroup>> createAndConfigureSpinnerAdapter(final Context context) {
            final ArrayAdapter<Optional<DeliveryGroup>> spinnerAdapter =
                    new ArrayAdapter<Optional<DeliveryGroup>>(
                            context,
                            android.R.layout.simple_spinner_item,
                            new Optional[]{
                                    Optional.empty(),
                                    Optional.of(DeliveryGroups.KERNSTADT),
                                    Optional.of(DeliveryGroups.DOERFER)}) {

                        @NonNull
                        @Override
                        public View getView(final int position,
                                            @Nullable final View convertView,
                                            @NonNull final ViewGroup parent) {
                            final TextView tv = (TextView) super.getView(position, convertView, parent);
                            tv.setText(getText(position));
                            return tv;
                        }

                        @Override
                        public View getDropDownView(final int position,
                                                    @Nullable final View convertView,
                                                    @NonNull final ViewGroup parent) {
                            final TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                            tv.setText(getText(position));
                            return tv;
                        }

                        private String getText(final int position) {
                            return this
                                    .getItem(position)
                                    .map(DeliveryGroup::name)
                                    .orElse("keine Liefergruppe ausgewählt");
                        }
                    };
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            return spinnerAdapter;
        }
    }
}
