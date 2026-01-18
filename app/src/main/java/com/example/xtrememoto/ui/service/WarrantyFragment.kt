package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R

class WarrantyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_warranty, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvWarranty = view.findViewById<RecyclerView>(R.id.rvWarranty)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val policies = listOf(
            WarrantyPolicy("Standard Warranty", "Covers manufacturing defects for 2 years or 30,000 km, whichever occurs first."),
            WarrantyPolicy("Engine Warranty", "5-year extended warranty on critical engine components if all periodic services are done at authorized centers."),
            WarrantyPolicy("Electrical Components", "6 months warranty on battery and 1 year on major electrical parts like ECU and wiring harness."),
            WarrantyPolicy("Frame & Chassis", "Life-time warranty against frame breakage under normal riding conditions (excludes accidents)."),
            WarrantyPolicy("Service Requirement", "Warranty is only valid if the bike is serviced at XtremeMoto authorized centers as per the schedule."),
            WarrantyPolicy("Excluded Items", "Consumables like tires, spark plugs, oil filters, and brake pads are not covered under warranty.")
        )

        rvWarranty.layoutManager = LinearLayoutManager(context)
        rvWarranty.adapter = WarrantyAdapter(policies)
    }
}

data class WarrantyPolicy(val title: String, val desc: String)

class WarrantyAdapter(private val list: List<WarrantyPolicy>) :
    RecyclerView.Adapter<WarrantyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvWarrantyTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvWarrantyDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_warranty, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTitle.text = list[position].title
        holder.tvDesc.text = list[position].desc
    }

    override fun getItemCount() = list.size
}
