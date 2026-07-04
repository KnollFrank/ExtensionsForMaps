package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroups;

class ViewHolder extends RecyclerView.ViewHolder {

    public final TextView tvAddress;
    public final Spinner spinnerDeliveryGroup;
    public final TextView tvWindowStart;
    public final TextView tvWindowEnd;
    public final ArrayAdapter<Optional<DeliveryGroup>> spinnerDeliveryGroupAdapter;

    public ViewHolder(final View itemView) {
        super(itemView);
        tvAddress = itemView.findViewById(R.id.tvAddress);
        spinnerDeliveryGroup = itemView.findViewById(R.id.spinnerDeliveryGroup);
        tvWindowStart = itemView.findViewById(R.id.tvWindowStart);
        tvWindowEnd = itemView.findViewById(R.id.tvWindowEnd);
        spinnerDeliveryGroupAdapter = createAndConfigureSpinnerDeliveryGroupAdapter(itemView.getContext());
        spinnerDeliveryGroup.setAdapter(spinnerDeliveryGroupAdapter);
    }

    private static ArrayAdapter<Optional<DeliveryGroup>> createAndConfigureSpinnerDeliveryGroupAdapter(final Context context) {
        final ArrayAdapter<Optional<DeliveryGroup>> spinnerDeliveryGroupAdapter =
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
                                .orElse("keine Liefergruppe");
                    }
                };
        spinnerDeliveryGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerDeliveryGroupAdapter;
    }
}
