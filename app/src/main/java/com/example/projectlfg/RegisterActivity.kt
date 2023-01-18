package com.example.projectlfg

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.projectlfg.Util.popUp
import com.example.projectlfg.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class RegisterActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterBinding


    private lateinit var NameEditText: EditText;
    private lateinit var EmailEditText: EditText;
    private lateinit var PasswordEditText: EditText;
    private lateinit var RegisterButton: Button;
    private lateinit var SelectImgButton:Button;
    private lateinit var UserImageView : ImageView;

    private lateinit var authenticator: FirebaseAuth;
    private lateinit var myref : DatabaseReference;
    private lateinit var storage: FirebaseStorage;
    private lateinit var storageRef : StorageReference

    private var imageUri: Uri?=null;
    private lateinit var ImageGalleryIntent : ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root;
        setContentView(view);

        NameEditText  = binding.RegisterName
        EmailEditText = binding.RegisterEmail
        PasswordEditText = binding.RegisterPassword
        RegisterButton = binding.RegisterButton
        UserImageView = binding.UserImageView;
        SelectImgButton = binding.RegisterImgButton;


        storage = Firebase.storage;
        storageRef = storage.reference
        authenticator = FirebaseAuth.getInstance()
        myref = Firebase.database.reference

        Util.checkPermissions(this);


        SelectImgButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            ImageGalleryIntent.launch(gallery);
        }

        ImageGalleryIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),{
            if(it.resultCode == Activity.RESULT_OK){
                imageUri= it.data!!.data;
                UserImageView.setImageURI(imageUri);
            }
        })

        RegisterButton.setOnClickListener {
            val NameNotEmpty = TextUtils.isEmpty(NameEditText.text.toString())
            val EmailNotEmpty = TextUtils.isEmpty(EmailEditText.text.toString())
            val PasswordNotEmpty = TextUtils.isEmpty(PasswordEditText.text.toString())

            if(!NameNotEmpty && !EmailNotEmpty && !PasswordNotEmpty && imageUri != null){
                signUp(NameEditText.text.toString(),EmailEditText.text.toString(),PasswordEditText.text.toString())
            }else{
                popUp(this, "please fill in user information")
            }
        }
    }



    private fun signUp(name: String, email: String, password: String){

        // Pre-config firebase signup method
        authenticator.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && imageUri != null) {

                    // store user profile
                    val uniqueid = UUID.randomUUID();
                    val ref= storageRef.child("images/"+ uniqueid);
                    val uploadTask = ref.putFile(imageUri!!);

                    // temporary replacement
                    val user = authenticator.currentUser;
                    val userinfo = UserInformation(name,email,"",user!!.uid);
                    myref.child("users").child(user!!.uid).setValue(userinfo);
                    Toast.makeText(this,"You've Signed Up Successfully", Toast.LENGTH_LONG).show();

                    val urlTask = uploadTask.continueWithTask {
                        if(!task.isSuccessful){
                            task.exception?.let{
                                throw it
                            }
                        }
                        ref.downloadUrl
                    }.addOnCompleteListener {
                        if(it.isSuccessful){
                            // register user to the "user" database

                            val downloadUri= it.result;
                            val user = authenticator.currentUser;
                            myref.child("users").child(user!!.uid).child("imageuri").setValue(downloadUri.toString())
                            finish()
                        }

                    }.addOnFailureListener {
                        Toast.makeText(this,it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }

            }.addOnFailureListener {
                Toast.makeText(this,it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    companion object{
        val PICK_IMAGE = 100;
    }
}