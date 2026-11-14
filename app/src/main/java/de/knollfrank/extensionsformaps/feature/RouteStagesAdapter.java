package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.knollfrank.extensionsformaps.GoogleMapsNavigator;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.databinding.ItemRouteStageBinding;
import de.knollfrank.extensionsformaps.route.Route;

public class RouteStagesAdapter extends RecyclerView.Adapter<RouteStagesAdapter.StageViewHolder> {

    private final List<Route> stages;

    public RouteStagesAdapter(final List<Route> stages) {
        this.stages = stages;
    }

    @NonNull
    @Override
    public StageViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final ItemRouteStageBinding binding =
                ItemRouteStageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final StageViewHolder holder, final int position) {
        final Route stageRoute = stages.get(position);
        final Context context = holder.binding.getRoot().getContext();

        holder.binding.tvStageTitle.setText(
                context.getString(
                        R.string.stage_title_format,
                        position + 1,
                        getItemCount(),
                        stageRoute.stops().size()));

        holder.binding.tvStageDetails.setText(
                context.getString(
                        R.string.stage_detail_format,
                        stageRoute.origin().address(),
                        stageRoute.destination().address()));

        holder.binding.btnOpenStage.setOnClickListener(v ->
                GoogleMapsNavigator.launchRouteOverview(stageRoute, context));
    }

    @Override
    public int getItemCount() {
        return stages != null ? stages.size() : 0;
    }

    static class StageViewHolder extends RecyclerView.ViewHolder {
        final ItemRouteStageBinding binding;

        StageViewHolder(final ItemRouteStageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
