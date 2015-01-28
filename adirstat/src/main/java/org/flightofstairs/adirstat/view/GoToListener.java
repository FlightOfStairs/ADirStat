package org.flightofstairs.adirstat.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.flightofstairs.adirstat.R;
import org.flightofstairs.adirstat.Sink;
import org.flightofstairs.adirstat.Tree;

import javax.annotation.Nullable;
import java.io.File;

import static org.flightofstairs.adirstat.FileUtils.DIRECTORY_MIMETYPE;
import static org.flightofstairs.adirstat.FileUtils.getMimeType;

public class GoToListener implements View.OnClickListener {
    private final Sink<Intent> intentLauncher;
    private final Sink<Integer> toaster;

    private final Tree<DisplayNode> root;
    private final Tree<DisplayNode> node;

    public GoToListener(Sink<Intent> intentLauncher, Sink<Integer> toaster, Tree<DisplayNode> root, Tree<DisplayNode> node) {
        this.intentLauncher = intentLauncher;
        this.toaster = toaster;
        this.root = root;
        this.node = node;
    }

    @Override
    public void onClick(View v) {
        Intent parentDirectoryIntent = goToIntent(parent(root, node.getValue()).getValue().getFile()).get();
        Intent fileIntent = goToIntent(node.getValue().getFile()).or(parentDirectoryIntent);

        try {
            intentLauncher.apply(fileIntent);
        } catch (ActivityNotFoundException e) {
            try {
                intentLauncher.apply(parentDirectoryIntent);
                toaster.apply(R.string.failedToOpenFileMessage);
            } catch (ActivityNotFoundException f) {
                toaster.apply(R.string.failedToOpenFileOrDirectoryMessage);
            }
        }
    }

    private Optional<Intent> goToIntent(File file) {
        return getMimeType(file).transform(new Function<String, Intent>() {
            @Nullable
            @Override
            public Intent apply(String mimeType) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), DIRECTORY_MIMETYPE.equals(mimeType) ? "*/*" : mimeType);
                return intent;
            }
        });
    }

    private Tree<DisplayNode> parent(Tree<DisplayNode> root, DisplayNode child) {
        return root.descendWhile((displayNode) -> displayNode != child && displayNode.getBounds().contains(child.getBounds().centerX(), child.getBounds().centerY())).get();
    }
}
