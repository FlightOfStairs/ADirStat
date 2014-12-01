package org.flightofstairs.adirstat.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.R;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.packing.SquarifiedPacking;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

import javax.annotation.Nonnull;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.Math.min;
import static java.util.Locale.UK;

public class TreeFragment extends RoboFragment {
    @Inject private Bus bus;
    @InjectView(R.id.treemap) private ImageView imageView;

    @Override
    @SneakyThrows
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.treeview, container, false);

        bus.register(this);

        return inflate;
    }

    @Override public void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onFsScanComplete(@Nonnull Optional<Tree<FilesystemSummary>> node) {
        String message = node.isPresent()
                ? String.format(UK, "Found %d files (%dmb).", node.get().getValue().getSubTreeCount(), node.get().getValue().getSubTreeBytes() / (int) Math.pow(1024, 2))
                : "Failed to list files.";

        Toast.makeText(getActivity().getApplicationContext(), message, LENGTH_LONG).show();

        if (node.isPresent()) {
            TreeMap drawable = new TreeMap(bus, node.get(), new SquarifiedPacking());
            imageView.setImageDrawable(drawable);
        }
    }

    @Subscribe
    public void onPackedDisplayNodes(@Nonnull Tree<DisplayNode> displayNodes) {
        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) handleClick(displayNodes, event);
            return true;
        });
    }

    private void handleClick(Tree<DisplayNode> displayNodes, MotionEvent event) {
        Predicate<DisplayNode> searchPredicate = (node) -> node.getBounds().contains((int) event.getX(), (int) event.getY()) && min(node.getBounds().width(), node.getBounds().height()) >= event.getToolMajor() / 2;
        Optional<Tree<DisplayNode>> possibleTree = displayNodes.descendWhile(searchPredicate);

        if (!possibleTree.isPresent()) return;
        Tree<DisplayNode> node = possibleTree.get();

        Toast.makeText(getActivity().getApplicationContext(), node.getValue().getFile().toString(), LENGTH_SHORT).show();
    }
}
