package de.KnollFrank.routeoptimizerforgooglemaps;

import android.Manifest;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {

	private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
	private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1002;

	private final List<String> addressList = new ArrayList<>();
	private final AddressAdapter addressAdapter = new AddressAdapter(addressList);
	private FusedLocationProviderClient fusedLocationClient;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		setupRecyclerView();
		this
				.<ExtendedFloatingActionButton>findViewById(R.id.fabStartTour)
				.setOnClickListener(view -> startOptimizationFlow());
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(@NonNull final Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void setupRecyclerView() {
		final RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(addressAdapter);
		createItemTouchHelper().attachToRecyclerView(recyclerView);
	}

	private ItemTouchHelper createItemTouchHelper() {
		return new ItemTouchHelper(
				new ItemTouchHelper.SimpleCallback(
						ItemTouchHelper.UP | ItemTouchHelper.DOWN,
						ItemTouchHelper.LEFT) {

					@Override
					public boolean onMove(@NonNull final RecyclerView recyclerView,
					                      @NonNull final RecyclerView.ViewHolder viewHolder,
					                      @NonNull final RecyclerView.ViewHolder target) {
						final int fromPos = viewHolder.getAdapterPosition();
						final int toPos = target.getAdapterPosition();
						Collections.swap(addressList, fromPos, toPos);
						addressAdapter.notifyItemMoved(fromPos, toPos);
						return true;
					}

					@Override
					public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder,
					                     final int direction) {
						final int position = viewHolder.getAdapterPosition();
						addressList.remove(position);
						addressAdapter.notifyItemRemoved(position);
					}
				});
	}

	private void handleIntent(final Intent intent) {
		if (Intent.ACTION_SEND.equals(intent.getAction()) && ClipDescription.MIMETYPE_TEXT_PLAIN.equals(intent.getType())) {
			Optional
					.ofNullable(intent.getStringExtra(Intent.EXTRA_TEXT))
					.ifPresent(
							sharedText -> {
								final String parsedAddress = parseAddress(sharedText);
								if (!parsedAddress.isEmpty() && !addressList.contains(parsedAddress)) {
									addressList.add(parsedAddress);
									addressAdapter.notifyItemInserted(addressList.size() - 1);
								}
							});
		}
	}

	// Public for testing
	public static String parseAddress(final String sharedText) {
		// 1. Remove URLs
		final String urlRegex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		final String textWithoutUrl =
				sharedText
						.replaceAll(urlRegex, "")
						.trim();

		// 2. Replace line breaks (and surrounding whitespace) with a comma and space
		// This handles cases like "Name \n Address" -> "Name, Address"
		String cleanedText = textWithoutUrl.replaceAll("\\s*\\n+\\s*", ", ");

		// 3. Clean up multiple commas or spaces that might have resulted
		cleanedText = cleanedText.replaceAll(",(\\s*,)+", ","); // Remove duplicate commas
		cleanedText = cleanedText.replaceAll("\\s+", " ");      // Collapse multiple spaces

		// 4. Final trim of the result and removal of leading/trailing commas
		cleanedText = cleanedText.trim();
		if (cleanedText.startsWith(",")) {
			cleanedText = cleanedText.substring(1).trim();
		}
		if (cleanedText.endsWith(",")) {
			cleanedText = cleanedText.substring(0, cleanedText.length() - 1).trim();
		}

		return cleanedText;
	}

	private void startOptimizationFlow() {
		if (addressList.isEmpty()) {
			Toast
					.makeText(this, "Add some stops first!", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(
					this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					LOCATION_PERMISSION_REQUEST_CODE);
			return;
		}
		if (!Settings.canDrawOverlays(this)) {
			final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
			return;
		}
		performOptimization();
	}

	private void performOptimization() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		fusedLocationClient
				.getLastLocation()
				.addOnSuccessListener(
						this,
						location -> {
							if (location == null) {
								Toast
										.makeText(this, "Could not get current location", Toast.LENGTH_LONG)
										.show();
								return;
							}
							optimizeRoute(location);
						})
				.addOnFailureListener(
						this,
						exception ->
								Toast
										.makeText(this, "Location error: " + exception.getMessage(), Toast.LENGTH_LONG)
										.show());
	}

	private void optimizeRoute(final Location startLocation) {
		final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		final List<RouteOptimizer.Stop> stops = new ArrayList<>();
		final Thread thread =
				new Thread(() -> {
					try {
						for (final String addressStr : addressList) {
							final List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
							if (addresses != null && !addresses.isEmpty()) {
								final Address address = addresses.get(0);
								stops.add(new RouteOptimizer.Stop(addressStr, address.getLatitude(), address.getLongitude()));
							} else {
								runOnUiThread(() ->
										Toast
												.makeText(this, "Geocoding failed for: " + addressStr, Toast.LENGTH_LONG)
												.show());
								return;
							}
						}
						// Call the optimizer
						final List<String> optimizedAddresses = RouteOptimizer.optimize(startLocation.getLatitude(), startLocation.getLongitude(), stops);
						runOnUiThread(() -> {
							addressList.clear();
							addressList.addAll(optimizedAddresses);
							addressAdapter.notifyDataSetChanged();
							startFloatingService();
						});
					} catch (final IOException exception) {
						runOnUiThread(() ->
								Toast
										.makeText(this, "Network error during Geocoding: " + exception.getMessage(), Toast.LENGTH_LONG)
										.show());
					} catch (final Exception exception) {
						runOnUiThread(() ->
								Toast
										.makeText(this, "Optimization error: " + exception.getMessage(), Toast.LENGTH_LONG)
										.show());
					}
				});
		thread.start();
	}

	private void startFloatingService() {
		final Intent serviceIntent = new Intent(this, FloatingWidgetService.class);
		serviceIntent.putStringArrayListExtra(FloatingWidgetService.OPTIMIZED_STOPS, new ArrayList<>(addressList));
		ContextCompat.startForegroundService(this, serviceIntent);
		// Minimize the app
		moveTaskToBack(true);
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode,
	                                       @NonNull final String[] permissions,
	                                       @NonNull final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startOptimizationFlow();
			} else {
				Toast
						.makeText(this, "Location permission required", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
			if (Settings.canDrawOverlays(this)) {
				startOptimizationFlow();
			} else {
				Toast
						.makeText(this, "Overlay permission required", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	private static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

		private final List<String> data;

		public AddressAdapter(final List<String> data) {
			this.data = data;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
			final View view =
					LayoutInflater
							.from(parent.getContext())
							.inflate(R.layout.item_address, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
			holder.tvAddress.setText(data.get(position));
			holder.ivDelete.setOnClickListener(
					view -> {
						final int currentPos = holder.getAdapterPosition();
						if (currentPos != RecyclerView.NO_POSITION) {
							data.remove(currentPos);
							notifyItemRemoved(currentPos);
						}
					});
		}

		@Override
		public int getItemCount() {
			return data.size();
		}

		static class ViewHolder extends RecyclerView.ViewHolder {

			public final TextView tvAddress;
			public final View ivDelete;
			public final View ivDragHandle;

			public ViewHolder(final View itemView) {
				super(itemView);
				tvAddress = itemView.findViewById(R.id.tvAddress);
				ivDelete = itemView.findViewById(R.id.ivDelete);
				ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
			}
		}
	}
}