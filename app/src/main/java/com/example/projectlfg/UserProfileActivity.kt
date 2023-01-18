package com.example.projectlfg

import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.projectlfg.databinding.ActivityUserProfileBinding

class UserProfileActivity:AppCompatActivity() {
    private lateinit var binding:ActivityUserProfileBinding;
    private lateinit var userimgview:ImageView;
    private lateinit var usernametextview : TextView;
    private lateinit var emailtextview :TextView;
    private lateinit var eventslistview:ListView;

    lateinit var userProfileViewModel :UserProfileViewModel;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater);
        val root = binding.root;
        setContentView(root);
        userimgview = binding.userprofileimgview;
        usernametextview = binding.nametextview
        emailtextview = binding.emailtextview
        eventslistview = binding.userprofilelistview;
        val userProfileViewModelFactory = ProjectViewModelFactory();
        userProfileViewModel = ViewModelProvider(this,userProfileViewModelFactory).get(UserProfileViewModel::class.java);
        userProfileViewModel.getUserInformationList()
        userProfileViewModel.userdata.observe(this,{
            val tmp =it;
            val tmpadapter = CustomAdapter(tmp.eventsinfolist);
            eventslistview.adapter = tmpadapter;
            emailtextview.setText(it.email);
            usernametextview.setText(it.name)
            Glide.with(this).load(it.imageuri).
            apply(RequestOptions.circleCropTransform()).into(userimgview);
        })
    }
}

