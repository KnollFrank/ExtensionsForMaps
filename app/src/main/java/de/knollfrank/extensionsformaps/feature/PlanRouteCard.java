package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;

import de.knollfrank.extensionsformaps.BuildConfig;
import de.knollfrank.extensionsformaps.GoogleMapsNavigator;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.databinding.ViewPlanRouteBinding;
import de.knollfrank.extensionsformaps.route.RouteDirectionsUrlConverter;
import de.knollfrank.extensionsformaps.route.RouteTemplateFactory;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class PlanRouteCard extends MaterialCardView {

    private ViewPlanRouteBinding binding;

    public PlanRouteCard(@NonNull Context context) {
        this(context, null);
    }

    public PlanRouteCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialCardViewFilledStyle);
    }

    public PlanRouteCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = ViewPlanRouteBinding.inflate(LayoutInflater.from(context), this);
        configurePlanRoute();
    }

    private void configurePlanRoute() {
        if (!BuildConfig.SHOW_PLANNING_CARD) {
            setVisibility(View.GONE);
            return;
        }
        binding.tvTotalStopsLabel.setText(getContext().getString(R.string.total_stops_label, (int) binding.sliderTotalStops.getValue()));
        binding.sliderTotalStops.addOnChangeListener(
                new Slider.OnChangeListener() {

                    @Override
                    public void onValueChange(@NonNull final Slider slider,
                                              final float value,
                                              final boolean fromUser) {
                        binding.tvTotalStopsLabel.setText(PlanRouteCard.this.getContext().getString(R.string.total_stops_label, (int) value));
                    }
                });
        binding.btnGenerateTemplate.setOnClickListener(onBtnGenerateTemplateClick(binding.sliderTotalStops, getContext()));
    }

    private static View.OnClickListener onBtnGenerateTemplateClick(final Slider sliderTotalStops,
                                                                   final Context context) {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                GoogleMapsNavigator.launchDirectionsUrl(
                        createDirectionsUrlTemplate(getSliderTotalStops()),
                        context);
            }

            private DirectionsUrl createDirectionsUrlTemplate(final int totalStops) {
                return RouteDirectionsUrlConverter.getDirectionsUrl(
                        RouteTemplateFactory.createRouteTemplate(
                                totalStops));
            }

            private int getSliderTotalStops() {
                return (int) sliderTotalStops.getValue();
            }
        };
    }
}
