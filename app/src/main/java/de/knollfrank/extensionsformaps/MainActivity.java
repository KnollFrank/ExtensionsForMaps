package de.knollfrank.extensionsformaps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.knollfrank.extensionsformaps.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configureCoffeeButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.cardPermissions.onResume();
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void configureCoffeeButton() {
        binding.btnCoffee.setOnClickListener(
                view ->
                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://paypal.me/KnollFrank"))));
    }
}
