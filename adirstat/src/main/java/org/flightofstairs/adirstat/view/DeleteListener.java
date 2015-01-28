package org.flightofstairs.adirstat.view;

import android.content.Intent;
import android.view.View;
import org.flightofstairs.adirstat.R;
import org.flightofstairs.adirstat.Sink;
import org.flightofstairs.adirstat.Tree;

public class DeleteListener implements View.OnClickListener {
    private final Sink<Intent> intentLauncher;
    private final Sink<Integer> toaster;

    private final Tree<DisplayNode> node;

    public DeleteListener(Sink<Intent> intentLauncher, Sink<Integer> toaster, Tree<DisplayNode> node) {
        this.intentLauncher = intentLauncher;
        this.toaster = toaster;
        this.node = node;
    }

    @Override
    public void onClick(View v) {
        toaster.apply(R.string.deleteClicked);
    }
}
