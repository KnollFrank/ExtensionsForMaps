package de.knollfrank.extensionsformaps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Lists;
import de.knollfrank.extensionsformaps.databinding.ItemStopBinding;
import de.knollfrank.extensionsformaps.optimize.OptimizationType;
import de.knollfrank.extensionsformaps.route.DeliveryGroup;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

public class StopsAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Optional<Route> route = Optional.empty();
    private Optional<OptimizationType> optimizationType = Optional.empty();
    private final List<Optional<DeliveryGroup>> deliveryGroups = new ArrayList<>();

    public void setRoute(final Route route, final OptimizationType optimizationType) {
        this.route = Optional.of(route);
        this.optimizationType = Optional.of(optimizationType);
        setDeliveryGroups(getDeliveryGroups(route));
        notifyDataSetChanged();
    }

    public Optional<Route> getRoute() {
        return route.map(
                route -> {
                    final var uiData = RouteTemplate.of(deliveryGroups);
                    return new Route(
                            route.origin(),
                            getWaypointsWithUiData(route, uiData),
                            getDestinationWithUiData(route, uiData, optimizationType.orElseThrow()));
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final ItemStopBinding binding = ItemStopBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        final ViewHolder holder = new ViewHolder(binding);
        holder.binding.spinnerDeliveryGroup.setOnItemSelectedListener(
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
        holder.binding.tvAddress.setText(stop.address());
        holder.setDots(position, getItemCount());
        // Marker logic
        holder.binding.tvIndexLetter.setVisibility(View.GONE);
        holder.binding.viewOriginMarker.setVisibility(View.GONE);
        holder.binding.ivDestinationMarker.setVisibility(View.GONE);
        if (isOriginOfRoute(position)) {
            holder.binding.viewOriginMarker.setVisibility(View.VISIBLE);
            holder.binding.spinnerDeliveryGroup.setVisibility(View.GONE);
        } else if (isDestinationOfRoute(position) && optimizationType.orElseThrow() == OptimizationType.FIXED_DESTINATION) {
            holder.binding.ivDestinationMarker.setVisibility(View.VISIBLE);
            holder.binding.spinnerDeliveryGroup.setVisibility(View.GONE);
        } else {
            holder.binding.tvIndexLetter.setVisibility(View.VISIBLE);
            if (isDestinationOfRoute(position)) {
                holder.binding.ivDestinationMarker.setVisibility(View.VISIBLE);
            }
            holder.setIndexLetterForPosition(position - 1);
            holder.binding.spinnerDeliveryGroup.setVisibility(View.VISIBLE);
            holder.binding.spinnerDeliveryGroup.setSelection(
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

    private static Stop getDestinationWithUiData(final Route route,
                                                 final RouteTemplate<Optional<DeliveryGroup>> uiData,
                                                 final OptimizationType optimizationType) {
        return getStopWithUiData(
                route.destination(),
                optimizationType == OptimizationType.ANY_DESTINATION ?
                        uiData.destination() :
                        route.destination().deliveryGroup());
    }

    private static List<Stop> getWaypointsWithUiData(final Route route,
                                                     final RouteTemplate<Optional<DeliveryGroup>> uiData) {
        return getStopsWithUiData(route.waypoints(), uiData.waypoints());
    }

    private static List<Stop> getStopsWithUiData(final List<Stop> stops, final List<Optional<DeliveryGroup>> uiDataList) {
        return Lists
                .zip(stops, uiDataList)
                .stream()
                .map(stop_uiData -> getStopWithUiData(stop_uiData.first, stop_uiData.second))
                .toList();
    }

    private static Stop getStopWithUiData(final Stop stop, final Optional<DeliveryGroup> uiData) {
        return new Stop(
                stop.id(),
                stop.address(),
                stop.officialPlaceId(),
                stop.geodetic(),
                uiData);
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

    private record RouteTemplate<T>(T origin, List<T> waypoints, T destination) {

        public static <T> RouteTemplate<T> of(final List<T> stops) {
            return new RouteTemplate<>(
                    stops.get(0),
                    stops.subList(1, stops.size() - 1),
                    stops.get(stops.size() - 1));
        }
    }
}
