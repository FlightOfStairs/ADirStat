package org.flightofstairs.adirstat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.DeleteListener;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.GoToListener;
import org.flightofstairs.adirstat.view.TreeMap;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

import javax.annotation.Nonnull;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.Math.min;
import static java.util.Locale.UK;

@SuppressWarnings("BindingAnnotationWithoutInject")
public class TreeFragment extends RoboFragment {

    public static final int MIN_CLICK_TARGET_WIDTH = 10;

    @Inject private Bus bus;
    @InjectView(R.id.treemap) private ImageView imageView;

    @InjectView(R.id.fileDetails) private TextView fileDetails;
    @InjectView(R.id.goToButton) private ImageView goToButton;
    @InjectView(R.id.deleteButton) private ImageView deleteButton;
    @InjectView(R.id.toolTip) private LinearLayout toolTip;

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
            TreeMap drawable = new TreeMap(bus, node.get());
            imageView.setImageDrawable(drawable);
        }
    }

    @Subscribe
    public void onPackedDisplayNodes(@Nonnull Tree<DisplayNode> displayNodes) {
        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == ACTION_DOWN) handleClick(displayNodes, event);
            return true;
        });
    }

    private void handleClick(final Tree<DisplayNode> root, MotionEvent event) {
        Optional<Tree<DisplayNode>> possibleTree = findClickedNode(root, event);
        if (!possibleTree.isPresent()) return;

        Tree<DisplayNode> node = possibleTree.get();

        displayToolTip(node);

        fileDetails.setText(node.getValue().getFile().toString());

        Sink<Integer> toaster = x -> Toast.makeText(getActivity().getApplicationContext(), x, LENGTH_SHORT).show();

        deleteButton.setOnClickListener(new DeleteListener(this::startActivity, toaster, node));
        goToButton.setOnClickListener(new GoToListener(this::startActivity, toaster, root, node));

        bus.post(node.getValue());
    }

    private void displayToolTip(Tree<DisplayNode> node) {
        toolTip.setVisibility(VISIBLE);

        int top = node.getValue().getBounds().centerY();

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) toolTip.getLayoutParams();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(layoutParams);
        params.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, 0);
        toolTip.setLayoutParams(params);
    }

    private static Optional<Tree<DisplayNode>> findClickedNode(Tree<DisplayNode> displayNodes, MotionEvent event) {
        Predicate<DisplayNode> searchPredicate = (node) -> node.getBounds().contains((int) event.getX(), (int) event.getY()) && min(node.getBounds().width(), node.getBounds().height()) >= MIN_CLICK_TARGET_WIDTH;
        return displayNodes.descendWhile(searchPredicate);
    }
}
