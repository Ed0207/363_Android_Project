package com.example.projectlfg

import android.content.Intent
import android.os.Bundle
import androidx.preference.*

class SettingPreferenceFragment: PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    lateinit var UserPref:Preference;
    lateinit var History:Preference;
    companion object{
        val USERINFO="UserInfo"
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return true;
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.user_preference,rootKey);
        UserPref = findPreference<Preference>(USERINFO)!!


        UserPref.onPreferenceClickListener = object :Preference.OnPreferenceClickListener{
            override fun onPreferenceClick(preference: Preference): Boolean {
                val intent = Intent(activity,UserInfoActivity::class.java);
                startActivity(intent);
                return true;
            }
        }


    }
}