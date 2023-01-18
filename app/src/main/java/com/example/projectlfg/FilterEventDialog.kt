package com.example.projectlfg

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

class FilterEventDialog:DialogFragment() ,DialogInterface.OnClickListener{

    private lateinit var ActivityTypeSpinner: Spinner;
    private lateinit var ActivityTypeNumberofPeople:Spinner;
    private lateinit var RemoveFiltersButton: Button;

    private lateinit var eventsViewModel: EventsViewModel;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
         val view = requireActivity().layoutInflater.inflate(R.layout.activity_filter_dialog, null)

        ActivityTypeSpinner = view.findViewById(R.id.filter_activity_type_spinner)
        ActivityTypeNumberofPeople = view.findViewById(R.id.filter_number_of_people)
        RemoveFiltersButton = view.findViewById(R.id.RemoveFilters);



        //activity type arr
        val arrayadapter1 = ArrayAdapter.createFromResource(requireContext(),R.array.ActivityTypeSpinner,android.R.layout.simple_spinner_item)
        val arrayadapter2 = ArrayAdapter.createFromResource(requireContext(),R.array.numofpeople,android.R.layout.simple_spinner_item)


        ActivityTypeSpinner.adapter = arrayadapter1
        ActivityTypeNumberofPeople.adapter = arrayadapter2


        val factory = ProjectViewModelFactory();
        eventsViewModel = ViewModelProvider(this,factory).get(EventsViewModel::class.java)
        RemoveFiltersButton.setOnClickListener {
            eventsViewModel.RemoveFilters()
            dismiss();
        }

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setPositiveButton("ok",this)
        builder.setNegativeButton("cancel", this)
        val dialog = builder.create()
        return dialog;
    }



    override fun onClick(dialog: DialogInterface?, which: Int) {
        if(which == Dialog.BUTTON_POSITIVE){
            // filter events
            val activitytype = ActivityTypeSpinner.selectedItem.toString()
            val numofppl = ActivityTypeNumberofPeople.selectedItem.toString();
            eventsViewModel.SortEvents(activitytype,numofppl);
            dismiss();
        }
    }
}
