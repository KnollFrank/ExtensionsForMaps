package de.KnollFrank.routeoptimizerforgooglemaps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

class StopsAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Optional<Route> route = Optional.empty();
    private final List<Optional<DeliveryGroup>> deliveryGroups = new ArrayList<>();

    public void setRoute(final Route route) {
        this.route = Optional.of(route);
        setDeliveryGroups(getDeliveryGroups(route));
        notifyDataSetChanged();
    }

    public Optional<Route> getRoute() {
        return route.map(
                _route ->
                        new Route(
                                _route.origin(),
                                getWaypointsWithUiData(_route),
                                _route.destination()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final ViewHolder holder =
                new ViewHolder(
                        LayoutInflater
                                .from(parent.getContext())
                                .inflate(R.layout.item_stop, parent, false));
        holder.spinnerDeliveryGroup.setOnItemSelectedListener(
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
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Stop stop = route.orElseThrow().stops().get(position);
        holder.tvAddress.setText(stop.address());
        holder.setDots(position, getItemCount());
        // Marker logic
        holder.tvIndexLetter.setVisibility(View.GONE);
        holder.viewOriginMarker.setVisibility(View.GONE);
        holder.ivDestinationMarker.setVisibility(View.GONE);
        if (isOriginOfRoute(position)) {
            holder.viewOriginMarker.setVisibility(View.VISIBLE);
            holder.spinnerDeliveryGroup.setVisibility(View.GONE);
        } else if (isDestinationOfRoute(position)) {
            holder.ivDestinationMarker.setVisibility(View.VISIBLE);
            holder.spinnerDeliveryGroup.setVisibility(View.GONE);
        } else {
            holder.setIndexLetterForPosition(position - 1);
            holder.spinnerDeliveryGroup.setVisibility(View.VISIBLE);
            holder.spinnerDeliveryGroup.setSelection(
                    holder.spinnerDeliveryGroupAdapter.getPosition(
                            deliveryGroups.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return route
                .map(_route -> _route.stops().size())
                .orElse(0);
    }

    private List<Stop> getWaypointsWithUiData(final Route route) {
        return IntStream
                .range(1, route.waypoints().size() + 1)
                .mapToObj(index -> getStopWithUiData(route.stops(), index))
                .toList();
    }

    private Stop getStopWithUiData(final List<Stop> stops, int index) {
        final Stop stop = stops.get(index);
        return new Stop(
                stop.id(),
                stop.address(),
                stop.placeId(),
                stop.geodetic(),
                deliveryGroups.get(index));
    }

    private static boolean isOriginOfRoute(final int position) {
        return position == 0;
    }

    private boolean isDestinationOfRoute(final int position) {
        return position == getItemCount() - 1;
    }

    private void setDeliveryGroups(final List<Optional<DeliveryGroup>> deliveryGroups) {
        this.deliveryGroups.clear();
        this.deliveryGroups.addAll(deliveryGroups);
    }

    private static List<Optional<DeliveryGroup>> getDeliveryGroups(final Route route) {
        return route
                .stops()
                .stream()
                .map(Stop::deliveryGroup)
                .toList();
    }
}
