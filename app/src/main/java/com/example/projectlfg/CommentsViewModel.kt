package com.example.projectlfg

import CommentInformation
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CommentsViewModel:ViewModel() {

    var CommentsLiveData = MutableLiveData<ArrayList<CommentInformation>>();

    fun GetFromUserDb(creatorid:String,commentInformation: CommentInformation,listener: OnGetDataListener){
        val userdb = FirebaseDatabase.getInstance().reference.child("users").child(creatorid)
        userdb.get().addOnSuccessListener {
            if(it.value != null){
                val data= it.value as HashMap<String,*>;
                val name = data.get("name") as String;
                val imguri = data.get("imageuri") as String;
                listener.onSuccess(commentInformation, name = name,imguri=imguri);
            }
        };
    }
    


    fun getMyComments(keystr:String,curruserid:String){
        GlobalScope.launch {

            val checklistener = object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var commentsarr = ArrayList<CommentInformation>();
                    var count = 1;
                    if(snapshot.exists() && snapshot.hasChild("comments") ){

                        val tmp = snapshot.child("comments").value as HashMap<String,*>
                        val totsize = tmp.size;
                        var ratingspath = HashMap<String,Long>();
                        if(snapshot.hasChild("ratings")){
                             ratingspath = snapshot.child("ratings").value as HashMap<String,Long>
                        }
                        var rating :Long= 0 ;
                        if(ratingspath.containsKey(curruserid)){
                            rating = ratingspath.get(curruserid)!!
                        }
                        for((key,value) in tmp){
                            val tmpvalue = value as HashMap<String,*>
                            val creatorid = tmpvalue.get("creatorid") as String;
                            val datestr= tmpvalue.get("date") as String
                            val title = tmpvalue.get("titletext") as String
                            val comment = tmpvalue.get("comments") as String
                            val goagainstr= tmpvalue.get("goagainstr") as String;

                            var tmpcomment = CommentInformation(comments = comment ,date=datestr,
                                rating= rating.toFloat(), titletext = title, goagainstr = goagainstr)

                            GetFromUserDb(creatorid,tmpcomment,object:OnGetDataListener{
                                override fun onSuccess(
                                    commentInformation: CommentInformation,
                                    name: String,
                                    imguri: String
                                ) {
                                    commentInformation.creator = name;
                                    commentInformation.imguri=imguri;
                                    commentsarr.add(commentInformation);
                                    if(count == totsize){
                                        CommentsLiveData.value = commentsarr;
                                    }else{
                                        count++;
                                    }
                                }
                            })
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }

            }
            FirebaseDatabase.getInstance().reference.child("events1")
                .child(keystr!!).addValueEventListener(checklistener)
        }
    }
}


class CommentsViewModelFactory(): ViewModelProvider.Factory{


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CommentsViewModel::class.java)){
            val key = "CommentsVM"
//            if(hashmapvm.containsKey(key)){
//                return getvm(key) as T
//            }else{
//                addVM(key,CommentsViewModel());
//                return getvm(key) as T;
//            }
            addVM(key,CommentsViewModel())
            return getvm(key) as T;
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