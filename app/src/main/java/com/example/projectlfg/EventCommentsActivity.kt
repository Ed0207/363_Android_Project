package com.example.projectlfg

import CommentInformation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.projectlfg.databinding.ActivityCommentListviewBinding
import com.example.projectlfg.databinding.ActivityEventInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class EventCommentsActivity:AppCompatActivity() {
    private lateinit var binding:ActivityCommentListviewBinding
    private lateinit var CommentView:ListView;
    private var EventId = ""
    var curruserid:String= "";

    private lateinit var commentsViewModel: CommentsViewModel;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentListviewBinding.inflate(layoutInflater)
        val view = binding.root;
        setContentView(view);
        CommentView = binding.commentslistview;

        EventId = intent.getStringExtra("key")!!;
        curruserid = FirebaseAuth.getInstance().currentUser!!.uid;
        val commentsViewModelFactory = CommentsViewModelFactory();
        commentsViewModel = ViewModelProvider(this,commentsViewModelFactory).get(CommentsViewModel::class.java);
        commentsViewModel.getMyComments(EventId,curruserid)

        commentsViewModel.CommentsLiveData.observe(this, Observer {
            val tmpval = it;
            val adapter = CommentAdapter(it);
            CommentView.adapter = adapter;
        })
    }

}


class CommentAdapter(mlist:ArrayList<CommentInformation>): BaseAdapter(){
    private lateinit var commentlist:ArrayList<CommentInformation>;
    init{
        commentlist = mlist;
    }
    override fun getCount(): Int {
        return commentlist.size;
    }

    override fun getItem(position: Int): Any {
        return commentlist.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0;
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.user_comment_view,parent,false);
        view.findViewById<TextView>(R.id.comment_date_time).setText(commentlist[position].date)
        view.findViewById<TextView>(R.id.comment_user).setText(commentlist[position].creator)
        view.findViewById<TextView>(R.id.comment_text).setText(commentlist[position].comments)
        view.findViewById<RatingBar>(R.id.fixedratingbar).rating = commentlist.get(position).rating
        view.findViewById<TextView>(R.id.titletextview).setText("Review: ${commentlist.get(position).titletext}   Would Go Again? ${commentlist.get(position).goagainstr}")
        Glide.with(view).load(commentlist.get(position).imguri.toUri()).apply(RequestOptions().circleCrop()).into(view.findViewById(R.id.cryingcatimg))
        return view;

    }

}