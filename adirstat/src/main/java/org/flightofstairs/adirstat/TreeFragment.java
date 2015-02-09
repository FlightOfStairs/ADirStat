package org.flightofstairs.adirstat;

import android.graphics.Rect;
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
import static com.google.common.base.Predicates.*;
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
    @InjectView(R.id.upButton) private ImageView upButton;
    @InjectView(R.id.downButton) private ImageView downButton;

    @InjectView(R.id.toolTip) private LinearLayout toolTip;

    @InjectView(R.id.nub) private View nub;
    @InjectView(R.id.highlight) private View hightlight;

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
        Optional<Tree<DisplayNode>> possibleTree = root.descendWhile(findByPosition((int) event.getX(), (int) event.getY()));
        if (!possibleTree.isPresent()) return;

        Tree<DisplayNode> node = possibleTree.get();

        selectNode(root, node, node);
    }

    private void selectNode(Tree<DisplayNode> root, Tree<DisplayNode> selected, Tree<DisplayNode> clicked) {
        displayHighlight(selected.getValue().getBounds());
        displayToolTip(clicked.getValue().getBounds());

        fileDetails.setText(selected.getValue().getFile().toString());

        Sink<Integer> toaster = x -> Toast.makeText(getActivity().getApplicationContext(), x, LENGTH_SHORT).show();

        deleteButton.setOnClickListener(new DeleteListener(this::startActivity, toaster, selected));
        goToButton.setOnClickListener(new GoToListener(this::startActivity, toaster, root, selected));

        upButton.setEnabled(!root.equals(selected));

        upButton.setOnClickListener(v -> {
            if (root.equals(selected)) return;

            int x = selected.getValue().getBounds().centerX();
            int y = selected.getValue().getBounds().centerY();
            Predicate<DisplayNode> searchPredicate = and(findByPosition(x, y), not(equalTo(selected.getValue())));

            selectNode(root, root.descendWhile(searchPredicate).get(), clicked);
        });

        downButton.setEnabled(!clicked.equals(selected));

        downButton.setOnClickListener(v -> {
            if (clicked.equals(selected)) return;

            Predicate<DisplayNode> searchPredicate = findByPosition(clicked.getValue().getBounds().centerX(), clicked.getValue().getBounds().centerY());
            selectNode(root, selected.stepDown(searchPredicate).get(), clicked);
        });

        bus.post(selected.getValue());
    }

    private void displayToolTip(Rect bounds) {
        toolTip.setVisibility(VISIBLE);

        ((FrameLayout.LayoutParams) toolTip.getLayoutParams()).setMargins(0, bounds.centerY() - nub.getHeight() / 2, 0, 0);

        LinearLayout.LayoutParams nubLayout = new LinearLayout.LayoutParams(nub.getLayoutParams());
        nubLayout.setMargins(bounds.centerX() - nub.getWidth() / 2, 0, 0, 0);
        nub.setLayoutParams(nubLayout);
    }

    private void displayHighlight(Rect bounds) {
        hightlight.setVisibility(VISIBLE);

        ((FrameLayout.LayoutParams) hightlight.getLayoutParams()).setMargins(bounds.left, bounds.top, 0, 0);
        ((FrameLayout.LayoutParams) hightlight.getLayoutParams()).width = bounds.width();
        ((FrameLayout.LayoutParams) hightlight.getLayoutParams()).height = bounds.height();
    }

    private static Predicate<DisplayNode> findByPosition(int x, int y) {
        return (node) -> node.getBounds().contains(x, y) && min(node.getBounds().width(), node.getBounds().height()) >= MIN_CLICK_TARGET_WIDTH;
    }
}
