package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroups;

class ViewHolder extends RecyclerView.ViewHolder {

    public final TextView tvDotsTop;
    public final TextView tvDotsBottom;
    public final TextView tvIndexLetter;
    public final View viewOriginMarker;
    public final ImageView ivDestinationMarker;
    public final TextView tvAddress;
    public final Spinner spinnerDeliveryGroup;
    public final ArrayAdapter<Optional<DeliveryGroup>> spinnerDeliveryGroupAdapter;

    public ViewHolder(final View itemView) {
        super(itemView);
        tvDotsTop = itemView.findViewById(R.id.tvDotsTop);
        tvDotsBottom = itemView.findViewById(R.id.tvDotsBottom);
        tvIndexLetter = itemView.findViewById(R.id.tvIndexLetter);
        viewOriginMarker = itemView.findViewById(R.id.viewOriginMarker);
        ivDestinationMarker = itemView.findViewById(R.id.ivDestinationMarker);
        tvAddress = itemView.findViewById(R.id.tvAddress);
        spinnerDeliveryGroup = itemView.findViewById(R.id.spinnerDeliveryGroup);
        spinnerDeliveryGroupAdapter =
                createAndConfigureSpinnerDeliveryGroupAdapter(
                        getDeliveryGroupOptions(DeliveryGroups.DELIVERY_GROUPS),
                        itemView.getContext());
        spinnerDeliveryGroup.setAdapter(spinnerDeliveryGroupAdapter);
    }

    public void setDots(final int position, final int numStops) {
        tvDotsTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        tvDotsBottom.setVisibility(position == numStops - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    public void setIndexLetterForPosition(final int position) {
        tvIndexLetter.setVisibility(View.VISIBLE);
        tvIndexLetter.setText(getIndexLetter(position));
    }

    private static Optional<DeliveryGroup>[] getDeliveryGroupOptions(final List<DeliveryGroup> deliveryGroups) {
        return Lists
                .concat(
                        Optional.empty(),
                        deliveryGroups
                                .stream()
                                .map(Optional::of)
                                .toList())
                .toArray(Optional[]::new);
    }

    private static ArrayAdapter<Optional<DeliveryGroup>> createAndConfigureSpinnerDeliveryGroupAdapter(
            final Optional<DeliveryGroup>[] deliveryGroupOptions,
            final Context context) {
        final ArrayAdapter<Optional<DeliveryGroup>> spinnerDeliveryGroupAdapter =
                new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_spinner_item,
                        deliveryGroupOptions) {

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
                                .map(group -> context.getString(R.string.delivery_group_name_format, group.sequenceOrder()))
                                .orElse(context.getString(R.string.delivery_group_none));
                    }
                };
        spinnerDeliveryGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerDeliveryGroupAdapter;
    }

    private String getIndexLetter(final int position) {
        final StringBuilder sb = new StringBuilder();
        int p = position;
        do {
            sb.insert(0, (char) ('A' + (p % 26)));
            p = (p / 26) - 1;
        } while (p >= 0);
        return sb.toString();
    }
}
