package com.example.projectlfg

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectlfg.MainActivity.Companion.currentUser
import com.example.projectlfg.MainActivity.Companion.storage
import com.example.projectlfg.Util.CHAT_INDIVIDUAL
import java.io.File


class ContactAdapter(val context: Context, val userList: ArrayList<UserInformation>): RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val contactName: TextView = itemView.findViewById<TextView>(R.id.contactName)
        val image: ImageView = itemView.findViewById<ImageView>(R.id.contactPic)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.contactlistlayout, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {

        val currentContact = userList[position]
        holder.contactName.text = currentContact.name


        if(currentContact.imageuri != null){
            try{
                val ref = storage.getReference("images/"+ currentContact.imageuri )
                val tempFile = File.createTempFile("tempLogo", "jpeg")
                ref.getFile(tempFile).addOnSuccessListener {

                    val img = BitmapFactory.decodeFile(tempFile.absolutePath)
                    holder.image.setImageBitmap(img)

                }.addOnFailureListener{



                }
            }catch(e: Exception){
                println("error loading img source")
                println(e)
            }
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("type", CHAT_INDIVIDUAL)
            intent.putExtra("name", currentContact.name)
            intent.putExtra("receiver", currentContact.uid)
            context.startActivity(intent)
        }
    }



    override fun getItemCount(): Int {
        return userList.size
    }
}