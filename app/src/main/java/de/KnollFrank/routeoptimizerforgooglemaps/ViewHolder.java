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

    public final TextView tvDotsTop;
    public final TextView tvDotsBottom;
    public final TextView tvIndexLetter;
    public final TextView tvAddress;
    public final Spinner spinnerDeliveryGroup;
    public final TextView tvWindowStart;
    public final TextView tvWindowEnd;
    public final ArrayAdapter<Optional<DeliveryGroup>> spinnerDeliveryGroupAdapter;

    public ViewHolder(final View itemView) {
        super(itemView);
        tvDotsTop = itemView.findViewById(R.id.tvDotsTop);
        tvDotsBottom = itemView.findViewById(R.id.tvDotsBottom);
        tvIndexLetter = itemView.findViewById(R.id.tvIndexLetter);
        tvAddress = itemView.findViewById(R.id.tvAddress);
        spinnerDeliveryGroup = itemView.findViewById(R.id.spinnerDeliveryGroup);
        tvWindowStart = itemView.findViewById(R.id.tvWindowStart);
        tvWindowEnd = itemView.findViewById(R.id.tvWindowEnd);
        spinnerDeliveryGroupAdapter = createAndConfigureSpinnerDeliveryGroupAdapter(itemView.getContext());
        spinnerDeliveryGroup.setAdapter(spinnerDeliveryGroupAdapter);
    }

    public void setDots(final int position, final int numStops) {
        tvDotsTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        tvDotsBottom.setVisibility(position == numStops - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    public void setIndexLetterForPosition(final int position) {
        tvIndexLetter.setText(getIndexLetter(position));
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

    private String getIndexLetter(final int position) {
        if (position < 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        int p = position;
        do {
            sb.insert(0, (char) ('A' + (p % 26)));
            p = (p / 26) - 1;
        } while (p >= 0);
        return sb.toString();
    }
}
