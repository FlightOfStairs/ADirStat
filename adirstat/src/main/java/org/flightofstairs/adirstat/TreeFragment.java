package org.flightofstairs.adirstat;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.DeleteListener;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.GoToListener;
import org.flightofstairs.adirstat.view.drawing.Cushions;
import org.flightofstairs.adirstat.view.packing.SquarifiedPacking;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

import javax.annotation.Nonnull;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static com.google.common.base.Predicates.*;
import static java.util.Locale.UK;

@SuppressWarnings("BindingAnnotationWithoutInject")
public class TreeFragment extends RoboFragment {

    @Inject private Bus bus;
    @InjectView(R.id.treemap) private ImageView imageView;
    @InjectView(R.id.loadingSpinner) private ProgressBar loadingSpinner;

    @InjectView(R.id.fileDetails) private TextView fileDetails;

    @InjectView(R.id.goToButton) private ImageView goToButton;
    @InjectView(R.id.deleteButton) private ImageView deleteButton;
    @InjectView(R.id.upButton) private ImageView upButton;
    @InjectView(R.id.downButton) private ImageView downButton;

    @InjectView(R.id.toolTip) private LinearLayout toolTip;

    @InjectView(R.id.upperNub) private View upperNub;
    @InjectView(R.id.lowerNub) private View lowerNub;

    @InjectView(R.id.highlight) private View highlight;

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

    @SuppressWarnings("unchecked")
    @Subscribe
    public void onFsScanComplete(@Nonnull Optional<Tree<FilesystemSummary>> node) {
        String message = node.isPresent()
                ? String.format(UK, "Found %d files (%dmb).", node.get().getValue().getSubTreeCount(), node.get().getValue().getSubTreeBytes() / (int) Math.pow(1024, 2))
                : "Failed to list files.";

        Toast.makeText(getActivity().getApplicationContext(), message, LENGTH_LONG).show();

        if (!node.isPresent()) return;

        AsyncTask<Tree<DisplayNode>, Void, Bitmap> draw = new SimpleAsyncTask<>(
                i -> Cushions.draw(i, imageView.getWidth(), imageView.getHeight()),
                o -> {
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), o));

                    imageView.setVisibility(VISIBLE);
                    loadingSpinner.setVisibility(INVISIBLE);

                    AppRate.with(getActivity())
                            .initialLaunchCount(3)
                            .retryPolicy(RetryPolicy.INCREMENTAL)
                            .checkAndShow();
                });

        AsyncTask<Tree<FilesystemSummary>, Void, Tree<DisplayNode>> pack = new SimpleAsyncTask<>(
                i -> SquarifiedPacking.pack(i, new Rect(0, 0, imageView.getWidth(), imageView.getHeight())),
                o -> {
                    imageView.setOnTouchListener((v, event) -> {
                        if (event.getAction() == ACTION_DOWN) handleClick(o, event);
                        return true;
                    });

                    draw.execute(o);
                });

        pack.execute(node.get());
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

        deleteButton.setOnClickListener(new DeleteListener(getActivity(), toaster, selected));
        goToButton.setOnClickListener(new GoToListener(this::startActivity, toaster, root, selected));

        upButton.setEnabled(!root.equals(selected));
        downButton.setEnabled(!clicked.equals(selected));

        upButton.setOnClickListener(navigateUpListener(root, selected, clicked));
        downButton.setOnClickListener(navigateDownListener(root, selected, clicked));

        bus.post(selected.getValue());
    }

    private void displayToolTip(Rect bounds) {
        toolTip.setVisibility(VISIBLE);

        boolean usingUpperNub = bounds.centerY() + upperNub.getHeight() + lowerNub.getHeight() + toolTip.getHeight() < imageView.getHeight();

        // Position tooltip + nubs vertically with appropriate offset.
        int top = bounds.centerY() - upperNub.getHeight() / 2 - (usingUpperNub ? 0 : toolTip.getHeight() - upperNub.getHeight());
        ((FrameLayout.LayoutParams) toolTip.getLayoutParams()).setMargins(0, top, 0, 0);

        // Hide unused nub.
        upperNub.setVisibility(usingUpperNub ? VISIBLE : INVISIBLE);
        lowerNub.setVisibility(usingUpperNub ? INVISIBLE : VISIBLE);

        // Position both nubs horizontally.
        LinearLayout.LayoutParams nubLayout = new LinearLayout.LayoutParams(upperNub.getLayoutParams());
        nubLayout.setMargins(bounds.centerX() - upperNub.getWidth() / 2, 0, 0, 0);
        upperNub.setLayoutParams(nubLayout);
        lowerNub.setLayoutParams(nubLayout);
    }

    private void displayHighlight(Rect bounds) {
        highlight.setVisibility(VISIBLE);

        ((FrameLayout.LayoutParams) highlight.getLayoutParams()).setMargins(bounds.left, bounds.top, 0, 0);
        ((FrameLayout.LayoutParams) highlight.getLayoutParams()).width = bounds.width();
        ((FrameLayout.LayoutParams) highlight.getLayoutParams()).height = bounds.height();
    }

    private View.OnClickListener navigateUpListener(Tree<DisplayNode> root, Tree<DisplayNode> selected, Tree<DisplayNode> clicked) {
        return v -> {
            if (root.equals(selected)) return;

            int x = selected.getValue().getBounds().centerX();
            int y = selected.getValue().getBounds().centerY();
            Predicate<DisplayNode> searchPredicate = and(findByPosition(x, y), not(equalTo(selected.getValue())));

            selectNode(root, root.descendWhile(searchPredicate).get(), clicked);
        };
    }

    private View.OnClickListener navigateDownListener(Tree<DisplayNode> root, Tree<DisplayNode> selected, Tree<DisplayNode> clicked) {
        return v -> {
            if (clicked.equals(selected)) return;

            Predicate<DisplayNode> searchPredicate = findByPosition(clicked.getValue().getBounds().centerX(), clicked.getValue().getBounds().centerY());
            selectNode(root, selected.stepDown(searchPredicate).get(), clicked);
        };
    }

    private static Predicate<DisplayNode> findByPosition(int x, int y) {
        return (node) -> node.getBounds().contains(x, y);
    }
}
