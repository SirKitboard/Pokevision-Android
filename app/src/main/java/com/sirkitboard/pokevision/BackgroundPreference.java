package com.sirkitboard.pokevision;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.sirkitboard.pokevision.util.PokemonAdapter;

import java.util.HashMap;
import java.util.List;

public class BackgroundPreference extends AppCompatActivity {

	private JobScheduler mJobScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_preference);
	    mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
        ListView listView = (ListView) findViewById(R.id.pokeList);

        listView.setAdapter(new PokemonAdapter(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preference_menu, menu);
        MenuItem item = menu.findItem(R.id.scan_switch);
        item.setActionView(R.layout.menu_switch);
	    View view = MenuItemCompat.getActionView(item);
	    SwitchCompat switchCompat = (SwitchCompat) view.findViewById(R.id.switchForActionBar);
	    switchCompat.isActivated();
	    switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			    if(isChecked) {
				    JobInfo.Builder builder = new JobInfo.Builder( 1,
						    new ComponentName( getPackageName(), PokeCheck.class.getName() ) );
				    System.out.println("START");
				    builder.setPeriodic( 6000 );
				    if( mJobScheduler.schedule( builder.build() ) <= 0 ) {
					    //If something goes wrong
				    }
			    } else {
				    mJobScheduler.cancelAll();
			    }
		    }
	    });
        return true;
    }

//    public void toggleService(View v) {
//        boolean selected = ((Switch)v).isSelected();
//        Log.d("Switch state", selected ? "True" : "False");
//    }

}
