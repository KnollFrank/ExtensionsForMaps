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
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

class StopsAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    private List<Stop> stops = List.of();
    private final List<Optional<DeliveryGroup>> deliveryGroups = new ArrayList<>();
    private final List<Optional<LocalDateTime>> windowStarts = new ArrayList<>();
    private final List<Optional<LocalDateTime>> windowEnds = new ArrayList<>();

    // FK-TODO: refactor
    public void setStops(final List<Stop> newStops) {
        stops = newStops;
        deliveryGroups.clear();
        windowStarts.clear();
        windowEnds.clear();
        for (final Stop stop : newStops) {
            deliveryGroups.add(stop.deliveryGroup());
            windowStarts.add(
                    stop
                            .timeWindow()
                            .flatMap(
                                    range ->
                                            range.hasLowerBound() ?
                                                    Optional.of(range.lowerEndpoint()) :
                                                    Optional.empty()));
            windowEnds.add(
                    stop
                            .timeWindow()
                            .flatMap(
                                    range ->
                                            range.hasUpperBound() ?
                                                    Optional.of(range.upperEndpoint()) :
                                                    Optional.empty()));
        }
        notifyDataSetChanged();
    }

    public List<Stop> getStops() {
        return IntStream
                .range(0, stops.size())
                .mapToObj(this::createStopWithUiData)
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

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Stop stop = stops.get(position);
        holder.setIndexLetterForPosition(position);
        holder.setDots(position, stops.size());
        holder.tvAddress.setText(stop.address());
        holder.spinnerDeliveryGroup.setSelection(
                holder.spinnerDeliveryGroupAdapter.getPosition(
                        deliveryGroups.get(position)));
        holder.tvWindowStart.setText(getText(windowStarts.get(position)));
        holder.tvWindowEnd.setText(getText(windowEnds.get(position)));
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    private String getText(final Optional<LocalDateTime> localDateTime) {
        return localDateTime
                .map(_localDateTime -> _localDateTime.format(DATE_TIME_FORMATTER))
                .orElse("");
    }

    private Stop createStopWithUiData(int index) {
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
                                    public void onClick(final View _view) {
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
}
