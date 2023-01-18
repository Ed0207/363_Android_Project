package com.example.projectlfg

//import EventsInformation
import DBEventsInformation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectlfg.databinding.ActivityEventHistoryRowBinding
import com.example.projectlfg.databinding.ActivityEventsHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface FirebaseCallBackList{
    fun onCallBack(list:List<DBEventsInformation>)

}



class UserHistoryActivity:AppCompatActivity() {
    private lateinit var binding:ActivityEventsHistoryBinding

    private lateinit var listview: ListView

    fun readFromDB(firebaseCallBackList: FirebaseCallBackList,db:DatabaseReference,curruserid:String){

        db.child("users").child(curruserid).child("events").get().addOnSuccessListener {
            val eventList : ArrayList<DBEventsInformation> = ArrayList();
            if(it.value == null) {

            }else{
                val listEvents = it.value as HashMap<String, *>
                for((key,value) in listEvents){
                    val tmp = listEvents.get(key) as String;
                    db.child("events1").child(tmp).get().addOnSuccessListener {
                        val data = it.value
                        if(data != null){
                            val tmp = it.value as HashMap<String,*>
                            val name = tmp.get("name") as String;
                            val startindate = tmp.get("startingdate") as String;
                            val location = tmp.get("location") as String;
                            val attendess = tmp.get("attendess") as Long;
                            val info = tmp.get("information") as String
                            val id = tmp.get("id") as String;
                            var activitytype = ""
                            if(tmp.containsKey("activitytypes")){
                                 activitytype = tmp.get("activitytypes") as String
                            }
                            eventList.add(DBEventsInformation(name=name,startingdate=startindate,
                                attendess=attendess,location=location, id = id, information = info, activitytypes = activitytype));
                        }
                        firebaseCallBackList.onCallBack(eventList);
                    }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsHistoryBinding.inflate(layoutInflater);
        val view = binding.root;

        val curruser = FirebaseAuth.getInstance().currentUser;
        val curruserid = curruser!!.uid;

        listview = view.findViewById(R.id.EventsHistoryRecyclerView)
        val db = FirebaseDatabase.getInstance().reference

        CoroutineScope(Dispatchers.IO).launch{
            readFromDB(object: FirebaseCallBackList{
                override fun onCallBack(list: List<DBEventsInformation>) {
                    listview.adapter = CustomAdapter(list);
                }
            },db,curruserid);
        }
        setContentView(view);
    }
}
class CustomAdapter( mlist:List<DBEventsInformation>) : BaseAdapter(){
    private lateinit var eventlist:List<DBEventsInformation>;
    init{
        eventlist = mlist;
    }

    override fun getCount(): Int {
        return eventlist.size;
    }

    override fun getItem(position: Int): Any {
        return eventlist[position];
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.activity_event_history_row,parent,false);
        val namelabel = view.findViewById<TextView>(R.id.HistoryEventName)
        val historyinfo = view.findViewById<TextView>(R.id.HistoryInfo);
        namelabel.setText(eventlist[position].name)
        historyinfo.setText("${eventlist[position].location.toString()} · ${eventlist[position].startingdate.toString()}")
        view.setOnClickListener {
            val intent = Intent(view.context,EventInfoActivity::class.java);
            intent.putExtra(MapsActivity.NAME,eventlist[position].name);
            intent.putExtra(MapsActivity.STARTINGDATE,eventlist[position].startingdate)
            intent.putExtra("Attendants",eventlist[position].attendess)
            intent.putExtra("key",eventlist[position].id)
            intent.putExtra("LOCATION",eventlist[position].location);
            intent.putExtra("info",eventlist[position].information);
            intent.putExtra(MapsActivity.ACTIVITYTYPESTR,eventlist.get(position).activitytypes)
            intent.putExtra(MapsActivity.ACTIVITYTYPESTR,eventlist.get(position).activitytypes)
            view.context.startActivity(intent);
        }
        return view;
    }

}