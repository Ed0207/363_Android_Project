package com.example.projectlfg

import CommentInformation
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
import com.example.projectlfg.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserInfoActivity:AppCompatActivity() {
    private lateinit var binding:ActivityUserBinding;


    private lateinit var UserNameField:EditText;
    private lateinit var passwordfield:EditText
    private lateinit var emailfield:EditText;
    private lateinit var SaveButton : Button;
    private lateinit var UserImgView:ImageView;
    private lateinit var SelectImgButton:Button;
    private lateinit var imguri: Uri;



    private lateinit var galleryResult:ActivityResultLauncher<Intent>;
    private lateinit var sharedPreferences: SharedPreferences;
    private lateinit var db  : DatabaseReference;
    private  var newImageSelected = false;

    companion object{
        val NEW_IMAGE_SELECTED = "newImageSelected"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NEW_IMAGE_SELECTED,newImageSelected);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater);
        val view = binding.root;
        setContentView(view);

        UserNameField = view.findViewById(R.id.UserNameInfo)
        emailfield = view.findViewById(R.id.UserEmailInfo)
         SaveButton = view.findViewById(R.id.SaveButton);
        SelectImgButton = view.findViewById(R.id.selectimgbutton);
        UserImgView = view.findViewById(R.id.userimgview)
        passwordfield= view.findViewById(R.id.InsertUserPasswordInfo);





        if(savedInstanceState != null){
            newImageSelected = savedInstanceState.getBoolean(NEW_IMAGE_SELECTED);
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),{
            if(it.resultCode == Activity.RESULT_OK){

                   imguri = it.data!!.data as Uri;
                newImageSelected = true;
                 UserImgView.setImageURI(imguri);
            }
        })

        SelectImgButton.setOnClickListener {
            val selectImgIntent = Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryResult.launch(selectImgIntent);
        }


        db= FirebaseDatabase.getInstance().reference;


        val curruser = FirebaseAuth.getInstance().currentUser;
//        UserNameField.text = curruser.email;
        if (curruser != null) {
            val currid = curruser.uid;
            db.child("users").child(currid).get().addOnSuccessListener {
//                val tmp = it.getValue(Map::class.java) as Map<String,String>;

                val tmp = it.value as HashMap<String,String>;
                UserNameField.setText(tmp.get("name"))
                emailfield.setText(tmp.get("email"))

                val imgURL = tmp.get("imageuri")
//                Glide.with(this).load(imgURL).into(UserImgView);
            }.addOnFailureListener {
                Log.e("firebase","error getting data")
            }
        };

        SaveButton.setOnClickListener {
            val newName = UserNameField.text.toString()
            val newEmail = emailfield.text.toString()
            db.child("users").child(curruser!!.uid).child("name").setValue(newName).addOnSuccessListener {
                Toast.makeText(this,"Name is Saved",Toast.LENGTH_LONG)
            };
            db.child("users").child(curruser!!.uid).child("email").setValue(newEmail).addOnSuccessListener {
                Toast.makeText(this,"Email is Saved",Toast.LENGTH_LONG)
            };

            val user = FirebaseAuth.getInstance().currentUser
            user!!.updateEmail(newEmail);
            if(!TextUtils.isEmpty(passwordfield.text))
                user!!.updatePassword(passwordfield.text.toString())
            if(newImageSelected){
                val tmpid = UUID.randomUUID()
                val storageref = FirebaseStorage.getInstance().reference.child("images/${tmpid}")
                storageref.putFile(imguri).continueWith {
                    if(!it.isSuccessful){
                        it.exception?.let{
                            throw it
                        }
                    }
                    storageref.downloadUrl
                }.addOnCompleteListener{
                    if(it.isSuccessful){
                        db.child("users").child(curruser!!.uid).child("imageuri").setValue(it.result);
                    }
                }
            }
            sharedPreferences = getSharedPreferences("sharedpref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit();
            editor.putString("name",newName);
            editor.putString("email",newEmail);

        }
    }
}
