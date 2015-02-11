package org.flightofstairs.adirstat;

import android.os.AsyncTask;
import com.google.common.base.Function;

public class SimpleAsyncTask<I, O> extends AsyncTask<I, Void, O> {
    private final Function<I, O> backgroundTask;
    private final Sink<O> onPostExecute;

    public SimpleAsyncTask(Function<I, O> backgroundTask, Sink<O> onPostExecute) {
        this.backgroundTask = backgroundTask;
        this.onPostExecute = onPostExecute;
    }

    @SafeVarargs
    @Override
    protected final O doInBackground(I... params) {
        return backgroundTask.apply(params[0]);
    }

    @Override
    protected void onPostExecute(O o) {
        onPostExecute.apply(o);
    }
}
