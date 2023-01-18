package com.example.projectlfg

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.projectlfg.MainActivity.Companion.authenticator
import com.google.firebase.auth.FirebaseAuth

class MsgAdapter(val context: Context, val msgList: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    class SentMsgHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val msg = itemView.findViewById<TextView>(R.id.sentmsgtext)
        val imgview = itemView.findViewById<ImageView>(R.id.chatsendimg)
    }

    class ReceiveMsgHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val msg = itemView.findViewById<TextView>(R.id.receivemsgtext)
        val imgview = itemView.findViewById<ImageView>(R.id.chatrcvimg)
    }

    // return proper view type to determin sent/receive msg type
    override fun getItemViewType(position: Int): Int {

        val currentTxt = msgList[position]
        val curruserid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        // sent msg
        if(curruserid == currentTxt.sender){
            return 1

        //  receive msg
        }
        return 2
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        lateinit var view: View

        // if it's a sent msg
        if(viewType == 1){
            view = LayoutInflater.from(context).inflate(R.layout.sentmsg, parent, false)
            return SentMsgHolder(view)
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.receivemsg, parent, false)
            return ReceiveMsgHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentTxt = msgList[position]

        // if it's a sent msg
        if(holder.javaClass == SentMsgHolder::class.java){
            val viewHolder = holder as SentMsgHolder
            holder.msg.text = currentTxt.msg
            if(currentTxt.imguri == ""){
                holder.imgview.setImageResource(R.drawable.cryingcat)
            }else{
                Glide.with(context).load(currentTxt.imguri).apply(RequestOptions.circleCropTransform()).into(holder.imgview);
            }
        // if it's a receive msg
        }else{
            val viewHolder = holder as ReceiveMsgHolder
            holder.msg.text = currentTxt.msg
            if(currentTxt.imguri == ""){
                holder.imgview.setImageResource(R.drawable.cryingcat)
            }else{
                Glide.with(context).load(currentTxt.imguri).apply(RequestOptions.circleCropTransform()).into(holder.imgview);
            }
        }
    }


    override fun getItemCount(): Int {
        return msgList.size
    }
}