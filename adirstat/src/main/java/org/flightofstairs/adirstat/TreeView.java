package org.flightofstairs.adirstat;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.SneakyThrows;

/**
 * A placeholder fragment containing a simple view.
 */
public class TreeView extends Fragment {

    public TreeView() {
    }

    @Override
    @SneakyThrows
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FsNode node = new Scanner().execute(Environment.getExternalStorageDirectory()).get().get();

        View inflate = inflater.inflate(R.layout.treeview, container, false);

        Log.d(getClass().getSimpleName(), node.toString());

        return inflate;
    }
}
