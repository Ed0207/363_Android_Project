package com.example.projectlfg

import DBEventsInformation
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.projectlfg.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import okhttp3.internal.cache.DiskLruCache


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener ,GoogleMap.InfoWindowAdapter{

    companion object {
        val latLngKey = "lat_lng_key"
        val NAME = "NAME"
        val STARTINGDATE = "STARTDINGDATE"
        val LOCATION = "LOCATION"
        val ACTIVITYTYPESTR="activitytype"
    }

    private lateinit var createEventButton: Button
    private lateinit var FilterEventButton:Button;
    // Set location variables to use if a event address/latlng not given
    private lateinit var locationManager: LocationManager

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var givenLocation = false
    private var DBHashMap = HashMap<String,DBEventsInformation>();
    private var MarkerHashMap = HashMap<String,Marker>();

    private lateinit var eventsViewModel: EventsViewModel;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FilterEventButton = binding.sortEventMapButton


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val factory = ProjectViewModelFactory();
        eventsViewModel = ViewModelProvider(this,factory).get(EventsViewModel::class.java);
        eventsViewModel.listenUpdates();

        eventsViewModel.filteredList.observe(this,Observer<ArrayList<DBEventsInformation>>{
            val arrlist= it;
            it.forEach {
                val key = it.id;
                if(MarkerHashMap.containsKey(key)){
                    val marker = MarkerHashMap.get(key) as Marker;
                    marker.isVisible = false;
                }

            }
        })

        createEventButton = findViewById(R.id.create_event_map_button)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!givenLocation) {
            initLocationManager()
        }
//        listenUpdates()
        eventsViewModel.lst.observe(this,Observer<ArrayList<DBEventsInformation>>{
            it.forEach {
                val latlng = it.latLng;
                val key = it.id;
                val tmpmarkeroption = MarkerOptions().position(latlng).title(key)
                if(!DBHashMap.containsKey(key)){
                    val marker = mMap.addMarker(tmpmarkeroption)
                    DBHashMap.put(key,it);
                    marker!!.title = key;
                    marker!!.snippet = it.name;
                    MarkerHashMap.put(key,marker!!);
                }else{
                    val marker = MarkerHashMap.get(key)
                    marker!!.isVisible = true;
                }
            }
            mMap.setInfoWindowAdapter(this);
        })
        mMap.setOnInfoWindowClickListener {
                    val name = it.title
                    val info = DBHashMap.get(name);
                    val intent = Intent(applicationContext,EventInfoActivity::class.java);
                    intent.putExtra(NAME,info!!.name);
                    intent.putExtra(LOCATION,info.location);
                    intent.putExtra(STARTINGDATE,info.startingdate);
                    intent.putExtra("Attendants",info.attendess);
                    intent.putExtra("info",info.information);
                    intent.putExtra("key",info.id)
                    intent.putExtra("info",info.information);
                    intent.putExtra(ACTIVITYTYPESTR,info.activitytypes)
                    startActivity(intent);

        }
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener{
            override fun onMarkerClick(p0: Marker): Boolean {
                p0.showInfoWindow()
                return true;
            }
        })

        mMap.setOnMapLongClickListener(this)

        createEventButton.setOnClickListener(View.OnClickListener {
            var createEventDialog = CreateEventDialog()
            createEventDialog.show(supportFragmentManager, "createEvent")
        })

        FilterEventButton.setOnClickListener {
            var filterEventDialog= FilterEventDialog();
            filterEventDialog.show(supportFragmentManager,"filterevents");
        }

    }


    @SuppressLint("MissingPermission")
    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider = locationManager.getBestProvider(criteria,true)
            if (provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    onLocationChanged(location)
                }
            }
        }catch (e: Exception) {
            println("Debug: exception initLocationManager")
        }
    }

    //override
    fun onLocationChanged(location: Location)  {
        val lat = location.latitude
        val lng = location.longitude
        val latlng = LatLng(lat, lng)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 13f)
        mMap.animateCamera(cameraUpdate)
    }

    // call create new event dialog
    override fun onMapLongClick(latLng: LatLng) {
        println("Debug: lat=${latLng.latitude}, lng=${latLng.longitude}")
        var bundle = Bundle()
        bundle.putParcelable(latLngKey, latLng)
        var createEventDialog = CreateEventDialog()
        createEventDialog.arguments = bundle
        createEventDialog.show(supportFragmentManager, "createEvent")
    }

    override fun getInfoContents(p0: Marker): View? {
         lateinit var infowindowview :View;
        var averagerating :Long= -1;

         suspend fun jobA():View{
             val date = DBHashMap.get(p0.title)!!.startingdate;
             val view= layoutInflater.inflate(R.layout.activity_info_window,null);
             val title = view.findViewById<TextView>(R.id.InfoWindowTitle)
             val ratingbar = view.findViewById<RatingBar>(R.id.InfoWindowtotalratingbar);
             val datetext = view.findViewById<TextView>(R.id.InfoWindowDate)
             val checkbtn = view.findViewById<Button>(R.id.checkitoutbutton)
             title.setText(DBHashMap.get(p0.title)!!.name);
             datetext.setText(date);
             return view;
         }
        suspend fun totalrating():HashMap<String,Long> {
            val db = FirebaseDatabase.getInstance().reference.child("events1").child(p0.title!!)
//                .child("ratings")

            val data = db.get().await()
            val eventdata = data.value as HashMap<String,*>
            if(!eventdata.containsKey("ratings")){
                val tmp = HashMap<String,Long>();
                return tmp
            }
            val tmp = eventdata.get("ratings") as HashMap<String,Long>
            return tmp;
        }

        runBlocking {
                 infowindowview =jobA();
                val data = totalrating() as HashMap<String,Long>
                var total :Long= 0;
                var count = 0;
                 for((key,value) in data){
                    total += value;
                    count ++
                }
                if(count > 0){
                    println("3.${total/count}");
                    infowindowview.findViewById<RatingBar>(R.id.InfoWindowtotalratingbar).rating = (total/count).toFloat()
                }

        }

        return   infowindowview;
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null;
    }

}
