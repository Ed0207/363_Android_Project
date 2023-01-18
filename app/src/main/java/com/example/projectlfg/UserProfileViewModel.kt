package com.example.projectlfg

import DBEventsInformation
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface OnGetUserData{
    fun onSuccess();
}

class UserProfileViewModel: ViewModel() {
    var userdata = MutableLiveData<UserInformation>();


    fun getUserInformationList(){
        GlobalScope.launch {
            val curruserid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
            val db = FirebaseDatabase.getInstance().reference.child("users").child(curruserid)
            val thisuser = UserInformation();



            db.get().addOnSuccessListener {
                val arr = it.value as HashMap<String,*>
                thisuser.email = arr.get("email") as String;
                thisuser.name = arr.get("name") as String;
                thisuser.imageuri = arr.get("imageuri") as String;
                userdata.value = thisuser;
                val db2 = FirebaseDatabase.getInstance().reference.child("events1")
                db2.get().addOnSuccessListener {
                    val data = it.value as HashMap<String,*>
                    val totalsize=data.size
                    var counter = 1;

                    for((key,value) in data){
                        val tmpdata = data.get(key) as HashMap<String,*>;
                        val creator = tmpdata.get("creator") as String
                        if(creator != curruserid) continue;
                        val name:String = tmpdata.get("name") as String
                        val startingdate = tmpdata.get("startingdate") as String
                        val location = tmpdata.get("location") as String
                        val attendants = tmpdata.get("attendess")  as Long;
                        val info = tmpdata.get("information") as String
                        val latlng = tmpdata.get("latLng") as HashMap<String,Float>;
                        val longtitude = latlng.get("longitude") as Double;
                        val latitude = latlng.get("latitude") as Double;
                        val tmpid = tmpdata.get("id") as String;
                        var activitytypes =""
                        if(tmpdata.containsKey("activitytypes")){
                            activitytypes = tmpdata.get("activitytypes") as String;
                        }
                        thisuser.eventsinfolist.add(DBEventsInformation(name = name, startingdate = startingdate, attendess = attendants, location = location,
                            latLng = LatLng(latitude,longtitude), information = info,id=tmpid, creator = creator, activitytypes = activitytypes ))
                        userdata.value= thisuser;
                    }
                }
            }
        }
    }
}