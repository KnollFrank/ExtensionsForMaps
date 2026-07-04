package de.KnollFrank.routeoptimizerforgooglemaps;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

class StopsAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    private Optional<Route> route = Optional.empty();
    private final List<Optional<DeliveryGroup>> deliveryGroups = new ArrayList<>();
    // FK-TODO: brauchen record für start und end und dann davon eine List<Optional<>>
    private final List<Optional<LocalDateTime>> windowStarts = new ArrayList<>();
    private final List<Optional<LocalDateTime>> windowEnds = new ArrayList<>();

    public void setRoute(final Route route) {
        this.route = Optional.of(route);
        deliveryGroups.clear();
        windowStarts.clear();
        windowEnds.clear();
        // FK-TODO: refactor using streams und für jede Variable das clear() und setzen getrennt von den anderen Variablen durchführen
        for (final Stop stop : route.stops()) {
            deliveryGroups.add(stop.deliveryGroup());
            windowStarts.add(getEndpoint(stop, true));
            windowEnds.add(getEndpoint(stop, false));
        }
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
        setupDateTimePickers(holder);
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
            holder.llEditableFields.setVisibility(View.GONE);
        } else if (isDestinationOfRoute(position)) {
            holder.ivDestinationMarker.setVisibility(View.VISIBLE);
            holder.llEditableFields.setVisibility(View.GONE);
        } else {
            holder.setIndexLetterForPosition(position - 1);
            holder.llEditableFields.setVisibility(View.VISIBLE);
            holder.spinnerDeliveryGroup.setSelection(
                    holder.spinnerDeliveryGroupAdapter.getPosition(
                            deliveryGroups.get(position)));
            holder.tvWindowStart.setText(getText(windowStarts.get(position)));
            holder.tvWindowEnd.setText(getText(windowEnds.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return route
                .map(_route -> _route.stops().size())
                .orElse(0);
    }

    private String getText(final Optional<LocalDateTime> localDateTime) {
        return localDateTime
                .map(_localDateTime -> _localDateTime.format(DATE_TIME_FORMATTER))
                .orElse("");
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
                deliveryGroups.get(index),
                createRange(windowStarts.get(index), windowEnds.get(index)));
    }

    private Optional<Range<LocalDateTime>> createRange(final Optional<LocalDateTime> start,
                                                       final Optional<LocalDateTime> end) {
        if (start.isPresent() && end.isPresent()) {
            return Optional.of(Range.closed(start.get(), end.get()));
        } else {
            return start
                    .map(Range::atLeast)
                    .or(() -> end.map(Range::atMost));
        }
    }

    private void pickDateTime(final View view, final ViewHolder holder, final boolean isStart) {
        if (view.getContext() instanceof final FragmentActivity activity) {
            pickDateTime(activity, holder, isStart);
        }
    }

    private void pickDateTime(final FragmentActivity activity, final ViewHolder holder, final boolean isStart) {
        final MaterialDatePicker<Long> datePicker =
                MaterialDatePicker
                        .Builder
                        .datePicker()
                        .setTitleText(isStart ? "Start-Datum wählen" : "End-Datum wählen")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
        datePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<>() {

                    @Override
                    public void onPositiveButtonClick(final Long selection) {
                        final MaterialTimePicker timePicker =
                                new MaterialTimePicker
                                        .Builder()
                                        .setTimeFormat(DateFormat.is24HourFormat(activity) ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
                                        .setHour(8)
                                        .setMinute(0)
                                        .setTitleText(isStart ? "Start-Uhrzeit wählen" : "End-Uhrzeit wählen")
                                        .build();
                        timePicker.addOnPositiveButtonClickListener(
                                new View.OnClickListener() {

                                    private final LocalDateTime date =
                                            Instant
                                                    .ofEpochMilli(selection)
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDateTime();

                                    @Override
                                    public void onClick(final View view) {
                                        final int pos = holder.getBindingAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION) {
                                            (isStart ? windowStarts : windowEnds).set(
                                                    pos,
                                                    Optional.of(
                                                            date
                                                                    .withHour(timePicker.getHour())
                                                                    .withMinute(timePicker.getMinute())));
                                            notifyItemChanged(pos);
                                        }
                                    }
                                });
                        timePicker.show(activity.getSupportFragmentManager(), "TIME_PICKER");
                    }
                });
        datePicker.show(activity.getSupportFragmentManager(), "DATE_PICKER");
    }

    private void clearDateTime(final ViewHolder holder, final boolean isStart) {
        final int pos = holder.getBindingAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) {
            (isStart ? windowStarts : windowEnds).set(pos, Optional.empty());
            notifyItemChanged(pos);
        }
    }

    private Optional<LocalDateTime> getEndpoint(final Stop stop, final boolean lower) {
        return stop
                .timeWindow()
                .flatMap(
                        timeWindow ->
                                lower ?
                                        timeWindow.hasLowerBound() ?
                                                Optional.of(timeWindow.lowerEndpoint()) :
                                                Optional.empty() :
                                        timeWindow.hasUpperBound() ?
                                                Optional.of(timeWindow.upperEndpoint()) :
                                                Optional.empty());
    }

    private void setupDateTimePickers(final ViewHolder holder) {
        holder.tvWindowStart.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View view) {
                        pickDateTime(view, holder, true);
                    }
                });
        holder.tvWindowEnd.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View view) {
                        pickDateTime(view, holder, false);
                    }
                });
        holder.tvWindowStart.setOnLongClickListener(
                new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(final View view) {
                        clearDateTime(holder, true);
                        return true;
                    }
                });
        holder.tvWindowEnd.setOnLongClickListener(
                new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(final View view) {
                        clearDateTime(holder, false);
                        return true;
                    }
                });
    }

    private static boolean isOriginOfRoute(final int position) {
        return position == 0;
    }

    private boolean isDestinationOfRoute(final int position) {
        return position == getItemCount() - 1;
    }
}
