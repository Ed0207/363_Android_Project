package com.example.projectlfg

import DBEventsInformation
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


interface  OnDataReceived{
    fun onSuccess(tmp:ArrayList<DBEventsInformation>)
}

class EventsViewModel:ViewModel() {

    var lst = MutableLiveData<ArrayList<DBEventsInformation>>();
    var filteredList = MutableLiveData<ArrayList<DBEventsInformation>>();
    var tmplist = ArrayList<DBEventsInformation>();
    lateinit var eventsViewModel :EventsViewModel


    fun GetFromEventDatabase(onDataReceived: OnDataReceived,data:HashMap<String,*>){
        tmplist.clear()
        for((key,value) in data){
            val childvalues= data.get(key) as HashMap<String,*>
            val name:String = childvalues.get("name") as String
            val startingdate = childvalues.get("startingdate") as String
            val location = childvalues.get("location") as String
            val attendants = childvalues.get("attendess")  as Long;
            val creator = childvalues.get("creator") as String
            val info = childvalues.get("information") as String
            val latlng = childvalues.get("latLng") as HashMap<String,Float>;
            val longtitude = latlng.get("longitude") as Double;
            val latitude = latlng.get("latitude") as Double;
            var activitytype = "";
            if(childvalues.containsKey("activitytypes")){
                 activitytype = childvalues.get("activitytypes") as String;
            }
            val tmplatlng = LatLng(latitude,longtitude);
            val dbinfo= DBEventsInformation(
                name =name, startingdate = startingdate, attendess = attendants
                ,
                location = location, latLng = tmplatlng, information =  info,
                creator =creator,id=key, activitytypes = activitytype)
            tmplist.add(dbinfo);
        }
        onDataReceived.onSuccess(tmplist);
    }

    fun listenUpdates(){
        GlobalScope.launch {
            val postlistener = object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var eventsarr = ArrayList<DBEventsInformation>();
                    if(snapshot.exists() ){
                        val data = snapshot.value as HashMap<String,*>;
                        GetFromEventDatabase(object:OnDataReceived{
                            override fun onSuccess(tmp: ArrayList<DBEventsInformation>) {
                                lst.value = tmp;
                            }

                        },data);
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
            val EventsDB = FirebaseDatabase.getInstance().reference.child("events1")
            EventsDB.addValueEventListener(postlistener)
        }
    }

    fun SortEvents(activitytype:String,numofppl:String){

        val curr = lst.value;
        if(curr == null) return;
        when(numofppl){
            ">100"->{
                 val filtered = curr!!.filterNot{ info-> info.attendess > 100 && info.activitytypes == activitytype }
                filteredList.value = ArrayList(filtered);
            }
            ">1000"->{
                val filtered = curr!!.filterNot { info->info.attendess>1000 && info.activitytypes == activitytype }
                filteredList.value = ArrayList(filtered);
            }
            "All"->{
                val filtered = curr!!.filterNot { info->info.activitytypes == activitytype }
                filteredList.value= ArrayList(filtered);
            }
        }

    }

    fun RemoveFilters(){
        lst.value= tmplist;
    }

}

class ProjectViewModelFactory():ViewModelProvider.Factory{


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EventsViewModel::class.java)){
            val key = "EventsVM"
            if(hashmapvm.containsKey(key)){
                return getvm(key) as T
            }else{
                addVM(key,EventsViewModel());
                return getvm(key) as T;
            }
        }else if(modelClass.isAssignableFrom(UserProfileViewModel::class.java)){
            val key = "UserProfileVM"
            if(hashmapvm.containsKey(key)){
                return getvm(key) as T
            }else{
                addVM(key,UserProfileViewModel());
                return getvm(key) as T;
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object{
        val hashmapvm = HashMap<String,ViewModel>()
        fun addVM(key:String,vm:ViewModel){
            hashmapvm.put(key,vm);
        }
        fun getvm(key:String):ViewModel?{
            return hashmapvm.get(key);
        }

    }
}
