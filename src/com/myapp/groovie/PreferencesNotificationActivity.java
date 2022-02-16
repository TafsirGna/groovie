package com.myapp.groovie;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.view.Menu;
import android.widget.Toast;

public class PreferencesNotificationActivity extends PreferenceActivity {

	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	//private CheckBoxPreference vibreur_notifs_lieu_checkBoxPreference;
	//private CheckBoxPreference vibreur_notifs_groupe_checkBoxPreference;
	private ListPreference voyant_notifs_lieu_ListPreference;
	private ListPreference voyant_notifs_groupe_ListPreference;
	private RingtonePreference sonnerie_notifs_lieu_ringtone;
	private RingtonePreference sonnerie_notifs_groupe_ringtone;
	private ListPreference localisation_contenu;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_preferences_notification);
		addPreferencesFromResource(R.xml.notificationspreference);

		//Initialisation des variables déclarées
		preferences=PreferenceManager.getDefaultSharedPreferences(this);
		editor=preferences.edit();
		localisation_contenu= (ListPreference) findPreference("localisation_contenue_listpreference_key");
		//vibreur_notifs_lieu_checkBoxPreference= (CheckBoxPreference) findPreference("vibreur_notifs_lieu_key");
		//vibreur_notifs_groupe_checkBoxPreference= (CheckBoxPreference) findPreference("vibreur_notifs_groupe_key");
		voyant_notifs_lieu_ListPreference= (ListPreference) findPreference("voyant_notifs_lieu_key");
		voyant_notifs_groupe_ListPreference= (ListPreference) findPreference("voyant_notifs_groupe_key");
		sonnerie_notifs_groupe_ringtone= (RingtonePreference) findPreference("sonnerie_notifs_groupe_key");
		sonnerie_notifs_lieu_ringtone= (RingtonePreference) findPreference("sonnerie_notifs_lieu_key");

		voyant_notifs_lieu_ListPreference.setSummary(preferences.getString("voyant_notifs_lieu_key","Rouge"));
		voyant_notifs_groupe_ListPreference.setSummary(preferences.getString("voyant_notifs_groupe_key","Rouge"));

		Uri uri=Uri.parse(preferences.getString("sonnerie_notifs_lieu_key",""));
		Ringtone r= RingtoneManager.getRingtone(PreferencesNotificationActivity.this,uri);
		sonnerie_notifs_lieu_ringtone.setSummary(r.getTitle(getBaseContext()));

		uri=Uri.parse(preferences.getString("sonnerie_notifs_groupe_key",""));
		r= RingtoneManager.getRingtone(PreferencesNotificationActivity.this,uri);
		sonnerie_notifs_groupe_ringtone.setSummary(r.getTitle(getBaseContext()));

		if (!preferences.getString("localisation_contenue_listpreference_key","").equals("Aucun") && !preferences.getString("localisation_contenue_listpreference_key","").equals(""))
			localisation_contenu.setSummary("Le contenu affiché est fonction de la localisation de "+preferences.getString("localisation_contenue_listpreference_key",""));
		
		voyant_notifs_lieu_ListPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				voyant_notifs_lieu_ListPreference.setSummary(newValue.toString());
				editor.putString("voyant_notifs_lieu_key",newValue.toString());
				editor.commit();
				return false;
			}
		});
		voyant_notifs_groupe_ListPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				voyant_notifs_groupe_ListPreference.setSummary(newValue.toString());
				editor.putString("voyant_notifs_groupe_key",newValue.toString());
				editor.commit();
				return false;
			}
		});
		sonnerie_notifs_groupe_ringtone.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				Uri uri=Uri.parse(newValue.toString());
				Ringtone r= RingtoneManager.getRingtone(PreferencesNotificationActivity.this,uri);
				sonnerie_notifs_groupe_ringtone.setSummary(r.getTitle(getBaseContext()));
				editor.putString("sonnerie_notifs_groupe_key",newValue.toString());
				editor.commit();
				return false;
			}
		});
		sonnerie_notifs_lieu_ringtone.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				Uri uri=Uri.parse(newValue.toString());
				Ringtone r= RingtoneManager.getRingtone(PreferencesNotificationActivity.this,uri);
				sonnerie_notifs_lieu_ringtone.setSummary(r.getTitle(getBaseContext()));
				editor.putString("sonnerie_notifs_lieu_key",newValue.toString());
				editor.commit();
				return false;
			}
		});
		localisation_contenu.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				if (!newValue.toString().equals("Aucun"))
					localisation_contenu.setSummary("Le contenu affiché est fonction de la localisation de "+newValue.toString());
				else
					localisation_contenu.setSummary("Modifier le contenu affiché en fonction de la localisation");
				editor.putString("localisation_contenue_listpreference_key",newValue.toString());
				editor.commit();
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.preferences_notification, menu);
		return true;
	}

}
