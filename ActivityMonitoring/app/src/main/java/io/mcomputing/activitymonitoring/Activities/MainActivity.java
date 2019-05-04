package io.mcomputing.activitymonitoring.Activities;

import android.support.annotation.IdRes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.mcomputing.activitymonitoring.Fragments.MonitoringFragment;
import io.mcomputing.activitymonitoring.R;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();



    }

	@Override
	public void onStart() {
		super.onStart();
		// Check if user is signed in (non-null) and update UI accordingly.
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			// do your stuff
			loadFragment(R.id.content_main, MonitoringFragment.newInstance(), MonitoringFragment.TAG);
		} else {
			signInAnonymously();
		}
	}

    private void signInAnonymously() {

		mAuth.signInAnonymously()
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in user's information
							Log.d("MAINACTIVITY", "signInAnonymously:success");
							//FirebaseUser user = mAuth.getCurrentUser();
							loadFragment(R.id.content_main, MonitoringFragment.newInstance(), MonitoringFragment.TAG);
						} else {
							// If sign in fails, display a message to the user.
							Log.w("MAINACTIVITY", "signInAnonymously:failure", task.getException());
							Toast.makeText(MainActivity.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
						}

						// ...
					}
				});
    }


    public void loadFragment(@IdRes int layout, Fragment fragment, String tag){
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(fragmentManager != null){
            FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
            fragmentTransaction.replace(layout, fragment, tag).addToBackStack(tag).commit();
        }

    }
}
