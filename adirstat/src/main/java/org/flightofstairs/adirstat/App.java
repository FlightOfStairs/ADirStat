package org.flightofstairs.adirstat;

import android.app.Application;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import roboguice.RoboGuice;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Module busModule = binder -> binder.bind(Bus.class).toProvider(BusProvider.class).asEagerSingleton();
        RoboGuice.overrideApplicationInjector(this, RoboGuice.newDefaultRoboModule(this), busModule);
    }

    private static class BusProvider implements Provider<Bus> {
        @Override
        public Bus get() {
            return new Bus(ThreadEnforcer.ANY);
        }
    }
}
