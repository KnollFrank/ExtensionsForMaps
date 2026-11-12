package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import de.knollfrank.extensionsformaps.BuildConfig;
import de.knollfrank.extensionsformaps.GoogleMapsNavigator;
import de.knollfrank.extensionsformaps.databinding.ViewPlanRouteBinding;
import de.knollfrank.extensionsformaps.route.RouteDirectionsUrlConverter;
import de.knollfrank.extensionsformaps.route.RouteTemplateFactory;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class PlanRouteCard extends MaterialCardView {

    private ViewPlanRouteBinding binding;

    public PlanRouteCard(@NonNull final Context context) {
        this(context, null);
    }

    public PlanRouteCard(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialCardViewFilledStyle);
    }

    public PlanRouteCard(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        binding = ViewPlanRouteBinding.inflate(LayoutInflater.from(context), this);
        configurePlanRoute();
    }

    private void configurePlanRoute() {
        if (!BuildConfig.SHOW_PLANNING_CARD) {
            setVisibility(View.GONE);
            return;
        }
        binding.btnGenerateTemplate.setOnClickListener(onBtnGenerateTemplateClick(getContext()));
    }

    private static View.OnClickListener onBtnGenerateTemplateClick(final Context context) {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                GoogleMapsNavigator.launchDirectionsUrl(
                        createDirectionsUrlTemplate(),
                        context);
            }

            private DirectionsUrl createDirectionsUrlTemplate() {
                return RouteDirectionsUrlConverter.getDirectionsUrl(
                        RouteTemplateFactory.createRouteTemplate(10));
            }
        };
    }
}
