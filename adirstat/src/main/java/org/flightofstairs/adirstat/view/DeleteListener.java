package org.flightofstairs.adirstat.view;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.R;
import org.flightofstairs.adirstat.Sink;
import org.flightofstairs.adirstat.Tree;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Predicates.not;
import static com.google.common.io.Files.fileTreeTraverser;

public class DeleteListener implements View.OnClickListener {
    private final Context activity;
    private final Sink<Integer> toaster;

    private final Tree<DisplayNode> node;

    public DeleteListener(Context activity, Sink<Integer> toaster, Tree<DisplayNode> node) {
        this.activity = activity;
        this.toaster = toaster;
        this.node = node;
    }

    @Override
    public void onClick(View v) {
        File file = node.getValue().getFile();

        int files = fileTreeTraverser().postOrderTraversal(file).filter(not(File::isDirectory)).size();

        String confirmationMessage = "I really want to delete " + (file.isDirectory() ? "this directory and all its contents (" + files + " file" + (files > 1 ? "s" : "") + ")" : "this file") + ":\n\n" + file.getAbsolutePath();

        AtomicBoolean deleteConfirmed = new AtomicBoolean(); // would like strong reference class please

        new AlertDialog.Builder(activity)
                .setTitle("Delete " + (file.isDirectory() ? "directory" : "file") + "?")
                .setMultiChoiceItems(new String[]{confirmationMessage}, null, (dialog, which, isChecked) -> {
                    deleteConfirmed.set(isChecked);
                })
                .setPositiveButton("Delete", (dialog, id) -> {
                    if (deleteConfirmed.get()) {
                        toaster.apply(R.string.deleteConfirmed);
                        delete(file);
                    }
                    else toaster.apply(R.string.deleteNotConfirmed);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    toaster.apply(R.string.deleteCanceled);
                })
                .create()
                .show();
    }

    // This method really deletes your files and directories. Be careful.
    @SneakyThrows
    public static void delete(File file) {
        for (File traversedFile : fileTreeTraverser().postOrderTraversal(file)) {
            boolean deleted = traversedFile.delete();

            if (deleted) Log.i(DeleteListener.class.getSimpleName(), "Deleted " + traversedFile);
            else Log.w(DeleteListener.class.getSimpleName(), "Failed to delete " + traversedFile);
        }
    }
}
