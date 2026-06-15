package de.KnollFrank.routeoptimizerforgooglemaps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

	private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
	private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1002;

	private final ArrayList<String> addressList = new ArrayList<>();
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
				.setOnClickListener(v -> startOptimizationFlow());
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
		if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
			final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (sharedText != null) {
				final String parsedAddress = parseAddress(sharedText);
				if (!parsedAddress.isEmpty() && !addressList.contains(parsedAddress)) {
					addressList.add(parsedAddress);
					addressAdapter.notifyItemInserted(addressList.size() - 1);
				}
			}
		}
	}

	// Public for testing
	public static String parseAddress(final String sharedText) {
		// Remove URLs
		final String urlRegex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		final Pattern pattern = Pattern.compile(urlRegex);
		final Matcher matcher = pattern.matcher(sharedText);
		String textWithoutUrl = matcher.replaceAll("").trim();

		// Google Maps sometimes shares "Name \n Address \n URL". We want to keep it simple and clean.
		// If there are multiple lines, the address is often the last or second to last.
		// For MVP, we will just take the cleaned text, maybe remove multiple newlines.
		final String cleanedText = textWithoutUrl.replaceAll("\n+", ", ").replaceAll(",\\s*,", ",").trim();
		if (cleanedText.endsWith(",")) {
			return cleanedText.substring(0, cleanedText.length() - 1).trim();
		}
		return cleanedText;
	}

	private void startOptimizationFlow() {
		if (addressList.isEmpty()) {
			Toast.makeText(this, "Add some stops first!", Toast.LENGTH_SHORT).show();
			return;
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
			if (location != null) {
				optimizeRoute(location);
			} else {
				Toast.makeText(this, "Could not get current location", Toast.LENGTH_LONG).show();
			}
		}).addOnFailureListener(this, e -> Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_LONG).show());
	}

	private void optimizeRoute(final Location startLocation) {
		final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		final List<RouteOptimizer.Stop> stops = new ArrayList<>();

		new Thread(() -> {
			try {
				for (final String addressStr : addressList) {
					final List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
					if (addresses != null && !addresses.isEmpty()) {
						final Address address = addresses.get(0);
						stops.add(new RouteOptimizer.Stop(addressStr, address.getLatitude(), address.getLongitude()));
					} else {
						runOnUiThread(() -> Toast.makeText(this, "Geocoding failed for: " + addressStr, Toast.LENGTH_LONG).show());
						return; // Abort
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

			} catch (final IOException e) {
				runOnUiThread(() -> Toast.makeText(this, "Network error during Geocoding: " + e.getMessage(), Toast.LENGTH_LONG).show());
			} catch (final Exception e) {
				runOnUiThread(() -> Toast.makeText(this, "Optimization error: " + e.getMessage(), Toast.LENGTH_LONG).show());
			}
		}).start();
	}

	private void startFloatingService() {
		final Intent serviceIntent = new Intent(this, FloatingWidgetService.class);
		serviceIntent.putStringArrayListExtra("OPTIMIZED_STOPS", addressList);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			ContextCompat.startForegroundService(this, serviceIntent);
		} else {
			startService(serviceIntent);
		}

		// Minimize the app
		moveTaskToBack(true);
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startOptimizationFlow();
			} else {
				Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
			if (Settings.canDrawOverlays(this)) {
				startOptimizationFlow();
			} else {
				Toast.makeText(this, "Overlay permission required", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
		private final ArrayList<String> data;

		AddressAdapter(final ArrayList<String> data) {
			this.data = data;
		}

		@Override
		public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position) {
			holder.tvAddress.setText(data.get(position));
			holder.ivDelete.setOnClickListener(v -> {
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
			final TextView tvAddress;
			final View ivDelete;
			final View ivDragHandle;

			ViewHolder(final View itemView) {
				super(itemView);
				tvAddress = itemView.findViewById(R.id.tvAddress);
				ivDelete = itemView.findViewById(R.id.ivDelete);
				ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
			}
		}
	}
}