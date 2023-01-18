package com.example.projectlfg

import CommentInformation
import DBEventsInformation
import android.content.Intent
//import EventsInformation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.projectlfg.databinding.ActivityEventInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface OnGetDataListener{
    fun onSuccess(commentInformation: CommentInformation,name:String,imguri:String);
}

class EventInfoActivity:AppCompatActivity() {


    private lateinit var  EventName:EditText;
    private lateinit var DateAndTime:EditText;
    private lateinit var EndDateAndTime:EditText;
    private lateinit var Attendees:EditText;
    private lateinit var Location:EditText;
    private lateinit var Infotext:EditText;
    private lateinit var eventChatButton: Button

    private lateinit var CommentButton:Button;
    private lateinit var SignUpButton: Button;
    private lateinit var GotoComments:Button;
    private lateinit var myratingbar:RatingBar;
    private lateinit var totalratingbar:RatingBar
    private lateinit var db:DatabaseReference

    private lateinit var curruserid:String;
    private var Eventid = "";

    private lateinit var binding:ActivityEventInfoBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventInfoBinding.inflate(layoutInflater);
        val view = binding.root;
        setContentView(view);
        curruserid = FirebaseAuth.getInstance().currentUser!!.uid.toString()


        EventName = binding.EventName;
        DateAndTime = binding.DateAndTime;
        EndDateAndTime = binding.enddate;
        Attendees = binding.Attendees;
        Location = binding.Location;
        SignUpButton = binding.SignUpEvent;
        myratingbar = binding.myratingbar;
        totalratingbar = binding.totalratingbar
        CommentButton = binding.CommentEvent
        GotoComments = binding.goviewcomments
        Infotext = binding.EventInformationtext

        eventChatButton = binding.eventChatButton

        Eventid = intent.getStringExtra("key") !!

        exists();
        getMyRatings()
        getTotalRatings()

        GotoComments.setOnClickListener {
            val intent = Intent(this,EventCommentsActivity::class.java);
            intent.putExtra("key",Eventid)
            startActivity(intent);
        }

        myratingbar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            val key = intent.getStringExtra("key")
            val db = FirebaseDatabase.getInstance().reference.child("events1").child(key!!).child("ratings")
            val curruser = FirebaseAuth.getInstance().currentUser!!.uid.toString()
            db.child(curruser).setValue(rating);
        }


        CommentButton.setOnClickListener {
            val createcommentdialog = CommentDialogFragment();
            val bundle = Bundle();
            bundle.putString("key",intent.getStringExtra("key"))
            bundle.putFloat("rating",myratingbar.rating);
            createcommentdialog.arguments = bundle;
            createcommentdialog.show(supportFragmentManager,"create comment")
        }




        eventChatButton.setOnClickListener {

            val chatIntent = Intent(this, ChatActivity::class.java)
            chatIntent.putExtra("type", Util.CHAT_GROUP)
            chatIntent.putExtra("name", intent.getStringExtra(MapsActivity.NAME))
            chatIntent.putExtra("eventID", Eventid)
            startActivity(chatIntent)
        }

//        CommentView = view.findViewById(R.id.commentslistview)


        db = FirebaseDatabase.getInstance().reference;

        EventName.setText(intent.getStringExtra(MapsActivity.NAME));
        DateAndTime.setText(intent.getStringExtra(MapsActivity.STARTINGDATE))
        EndDateAndTime.setText(intent.getStringExtra(MapsActivity.ACTIVITYTYPESTR))
        Attendees.setText(intent.getLongExtra("Attendants",0).toString())
        Location.setText(intent.getStringExtra("LOCATION"));
        Infotext.setText(intent.getStringExtra("info"))

    }

    fun getMyRatings(){
        GlobalScope.launch {
            val keystr = intent.getStringExtra("key")
            val tmpdb = FirebaseDatabase.getInstance().reference.child("events1").child(keystr!!).child("ratings").child(curruserid)
            tmpdb.get().addOnSuccessListener {
                if(it.value != null){
                    val data = it.value as Long;
                    myratingbar.rating = data.toFloat();
                }
            }
        }
    }




    fun getTotalRatings(){
        GlobalScope.launch{

            val checklistener = object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot != null  && snapshot.hasChild("ratings")){

                        var total  = 0.0;
                        var people = 0;
                        val data = snapshot.value as HashMap<String,*>
                        val eventdata = data.get("ratings") as HashMap<String,Long>
                        for((key,value) in eventdata){
                            val num = value.toFloat();
                            total = total+num;
                            people+=1;
                        }
                        totalratingbar.rating = (total/people).toFloat();
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            val keystr = intent.getStringExtra("key");
            val tmpdb = FirebaseDatabase.getInstance().reference.child("events1").child(keystr!!)
            tmpdb.addValueEventListener(checklistener)
        }
    }

    fun exists(){

        GlobalScope.launch{

            val checklistener = object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists() && snapshot.hasChild("people")){
                        val data = snapshot.child("people")

                        if(data.hasChild(curruserid)){
                            SignUpButton.setText("Remove")
                            val keystr = intent.getStringExtra("key")
                            SignUpButton.setOnClickListener {
                                FirebaseDatabase.getInstance().reference.child("events1").child(keystr!!).
                                child("people").child(curruserid).removeValue()
                                val userevents =FirebaseDatabase.getInstance().reference.child("users").
                                child(curruserid).child("events").child(keystr!!).removeValue()
                            }
                        }else{
                            SignUpButton.setText("Sign Up")
                            SignUpButton.setOnClickListener {
                                addToDatabase()
                            }
                        }
                    }else{
                        SignUpButton.setText("Sign Up")
                        SignUpButton.setOnClickListener {
                            addToDatabase()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            val tmpdb = FirebaseDatabase.getInstance().reference
            val keystr = intent.getStringExtra("key")
            tmpdb.child("events1").child(keystr!!).addValueEventListener(checklistener);
        }
    }

    fun addToDatabase(){
        val curruser = FirebaseAuth.getInstance().currentUser;
        if(curruser != null){
            val userid = curruser.uid;
            val eventname = EventName.text.toString()
            val startingdate = DateAndTime.text.toString()
            val enddate = EndDateAndTime.text.toString()
            val attendess =  Attendees.text.toString().toLong();
            val locationstr = Location.text.toString();
            val eventinfo = DBEventsInformation(name=eventname,startingdate=startingdate, attendess = attendess,location=locationstr)
            val randomid = UUID.randomUUID().toString()
            val keystr = intent.getStringExtra("key")
            db.child("users").child(userid).child("events").child(keystr!!).setValue(intent.getStringExtra("key"));
            db.child("events1").child(keystr!!).child("people").child(curruserid).setValue(curruserid)
        }
    }

}
