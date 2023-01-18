package com.example.projectlfg

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectlfg.MainActivity.Companion.currentUser
import com.example.projectlfg.MainActivity.Companion.myref
import com.example.projectlfg.Util.CHAT_INDIVIDUAL
import com.example.projectlfg.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: MsgAdapter
    private lateinit var chatView:RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: Button

    private lateinit var receiverUID: String
    private lateinit var sendRoomID: String
    private lateinit var receiveRoomID: String

    private lateinit var binding:ActivityChatBinding
    private lateinit var msgList: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageBox = binding.chatRoomInput
        sendButton = binding.chatRoomSend
        msgList = arrayListOf()

        chatView = binding.chatRoomMsgs
        chatView.layoutManager = LinearLayoutManager(this)
        adapter = MsgAdapter(this, msgList)
        chatView.adapter = adapter


        val type = intent.getStringExtra("type")
        val name = intent.getStringExtra("name")
        println(name)
        binding.chatRoomName.text = name

        // Individual chat
        if(type == CHAT_INDIVIDUAL){
            receiverUID = intent.getStringExtra("receiver")!!

            sendRoomID = currentUser!!.uid + receiverUID
            receiveRoomID = receiverUID + currentUser!!.uid

            myref.child("chatroom").child(receiveRoomID).child("msgs")
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        msgList.clear()

                        for(msg in snapshot.children){

                            val text = msg.child("msg").getValue()
                            val sender = msg.child("sender").getValue()
                            val imguri = msg.child("imguri").getValue() as String
                            val tmpmsg = Message(text.toString(),sender.toString())
                            tmpmsg.imguri = imguri;
                            msgList.add(tmpmsg)
                        }

                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error)
                    }
                })

            sendButton.setOnClickListener {

                val curreuserid=FirebaseAuth.getInstance().currentUser!!.uid.toString()
                val message = Message(messageBox.text.toString(), curreuserid)


                FirebaseDatabase.getInstance().reference.child("users").child(curreuserid).get().addOnSuccessListener {
                    val data = it.value as HashMap<String,*>
                    message.imguri = data.get("imageuri") as String;
                    myref.child("chatroom").child(sendRoomID).child("msgs").push()
                        .setValue(message).addOnCompleteListener {
                            myref.child("chatroom").child(receiveRoomID).child("msgs").push()
                                .setValue(message)
                        }
                }
                messageBox.setText("")
                messageBox.hint = ""
            }

        // group chat
        }else{

            receiveRoomID = intent.getStringExtra("eventID").toString()

            myref.child("events1").child(receiveRoomID).child("chat")
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        msgList.clear()

                        for(msg in snapshot.children){

                            val text = msg.child("msg").getValue()
                            val sender = msg.child("sender").getValue()
                            val img = msg.child("imguri").getValue();
                            val tmpmsg= Message(text.toString(), sender.toString())
                            tmpmsg.imguri = img as String;
                            msgList.add(tmpmsg);
                        }

                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error)
                    }
                })

            sendButton.setOnClickListener {


                val tmpuser = FirebaseAuth.getInstance().currentUser!!.uid.toString()
                val message = Message(messageBox.text.toString(), tmpuser);

                FirebaseDatabase.getInstance().reference.child("users").child(tmpuser).get().addOnSuccessListener {
                    val data = it.value as HashMap<String,*>
                    message.imguri = data.get("imageuri") as String;
                    myref.child("events1").child(receiveRoomID).child("chat").push()
                        .setValue(message)

                    messageBox.setText("")
                    messageBox.hint = ""
                }


            }
        }
    }
}