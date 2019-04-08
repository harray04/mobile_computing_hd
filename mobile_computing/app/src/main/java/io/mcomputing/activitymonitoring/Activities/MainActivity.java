package io.mcomputing.activitymonitoring.Activities;

import android.support.annotation.IdRes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import io.mcomputing.activitymonitoring.Fragments.MonitoringFragment;
import io.mcomputing.activitymonitoring.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(R.id.content_main, MonitoringFragment.newInstance(), MonitoringFragment.TAG);

    }


    public void loadFragment(@IdRes int layout, Fragment fragment, String tag){
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(fragmentManager != null){
            FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
            fragmentTransaction.replace(layout, fragment, tag).addToBackStack(tag).commit();
        }

    }
}
