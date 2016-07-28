package com.sirkitboard.pokevision.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sirkitboard.pokevision.R;

/**
 * Created by abalwani on 27/07/2016.
 */
public class PokemonAdapter extends BaseAdapter {
	private final Context context;
	LayoutInflater inflater;
	SharedPreferences prefs;
	SharedPreferences.Editor prefEditor;
	boolean[] pokemonPref;

	public PokemonAdapter(Context context) {
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		prefs = context.getSharedPreferences("pokemon_notif_prefs", Context.MODE_PRIVATE);
		String currentPrefs = prefs.getString("notif_prefs", null);
		prefEditor = prefs.edit();
		if (currentPrefs == null) {
			pokemonPref = new boolean[151];
			prefEditor.putString("notif_prefs", boolArrayToString(pokemonPref));
			prefEditor.apply();
		} else {
			pokemonPref = stringToBoolArray(currentPrefs);
		}
	}

	private String boolArrayToString(boolean[] bools) {
		String[] strings = new String[bools.length];
		for (int i = 0; i < bools.length; i++) {
			strings[i] = (bools[i] ? "1" : "0");
		}
		return TextUtils.join(",", strings);
	}

	private boolean[] stringToBoolArray(String string) {
		String[] strings = string.split(",");
		boolean[] bools = new boolean[strings.length];
		for (int i = 0; i < strings.length; i++) {
			String bool = strings[i];
			bools[i] = bool.equalsIgnoreCase("1");
		}
		return bools;
	}

	@Override
	public int getCount() {
		return 151;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (convertView == null) {
			rowView = inflater.inflate(R.layout.pokemon_adapter, parent, false);
		}

		TextView textView = (TextView) rowView.findViewById(R.id.pokeName);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.pokeEnabled);
		textView.setText(getStringResourceByName("pokemonId" + (position + 1)));
		imageView.setBackgroundResource(getDrawableResourceByName("pokemon_" + (position + 1)));
		checkBox.setChecked(pokemonPref[position]);

		checkBox.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP) {
					pokemonPref[position] = !pokemonPref[position];
					String prefsString = boolArrayToString(pokemonPref);
					prefEditor.putString("notif_prefs", prefsString);
					Log.d("Adapter Position", "" + position);
					System.out.println(prefsString);
					prefEditor.apply();
				}
				return false;
			}
		});

		return rowView;
	}

	private String getStringResourceByName(String aString) {
		String packageName = context.getPackageName();
		int resId = context.getResources().getIdentifier(aString, "string", packageName);
		return context.getString(resId);
	}

	private int getDrawableResourceByName(String name) {
		String packageName = context.getPackageName();
		int resId = context.getResources().getIdentifier(name, "drawable", packageName);
		return resId;
	}
}