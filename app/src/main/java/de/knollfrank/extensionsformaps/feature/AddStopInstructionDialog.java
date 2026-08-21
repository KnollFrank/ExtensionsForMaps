package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.databinding.DialogAddStopInstructionBinding;

public class AddStopInstructionDialog {

    public interface Callback {

        void onOk();
    }

    public static void show(final Context context,
                            final LayoutInflater layoutInflater,
                            final Callback callback) {
        final DialogAddStopInstructionBinding binding = DialogAddStopInstructionBinding.inflate(layoutInflater);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.add_stop_instruction_title)
                .setView(binding.getRoot())
                .setPositiveButton(
                        R.string.ok,
                        (dialog, which) -> {
                            if (binding.cbDontShowAgain.isChecked()) {
                                SortConfig.setShouldShowAddStopInstruction(context, false);
                            }
                            callback.onOk();
                        })
                .setCancelable(false)
                .show();
    }
}
