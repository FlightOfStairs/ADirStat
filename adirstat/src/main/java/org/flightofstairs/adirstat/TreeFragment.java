package org.flightofstairs.adirstat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import lombok.SneakyThrows;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

import javax.annotation.Nonnull;

import static android.widget.Toast.LENGTH_LONG;
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
    public void onFsScanComplete(@Nonnull Optional<FsNode> node) {
        String logMessage = node.isPresent()
                ? String.format(UK, "Found %d files (%dmb).", node.get().getSubTreeCount(), node.get().getSubTreeBytes() / (int) Math.pow(1024, 2))
                : "Failed to list files.";

        Toast.makeText(getActivity().getApplicationContext(), logMessage, LENGTH_LONG).show();

        if (node.isPresent()) imageView.setImageDrawable(new TreeMap(node.get()));
    }
}
