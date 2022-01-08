package org.flightofstairs.adirstat;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.isExternalStorageManager;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.google.inject.Inject;
import com.squareup.otto.Bus;

import org.flightofstairs.adirstat.model.Scanner;

import roboguice.activity.RoboActivity;

public class Main extends RoboActivity {
    @Inject Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

        if (canAccessStorage()) {
            setContentView(R.layout.activity);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, new TreeFragment())
                    .commit();

            new Scanner(bus).execute(getExternalStorageDirectory());
        } else {
            setContentView(R.layout.permissions_screen);
        }
    }

    private boolean canAccessStorage() {
        return VERSION.SDK_INT < VERSION_CODES.R || isExternalStorageManager();
    }

    @RequiresApi(api = VERSION_CODES.R)
    public void requestPermissions(View view) {
        Intent intent = new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        startActivity(intent);
    }
}
