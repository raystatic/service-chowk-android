package com.servicechowk.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.servicechowk.app.data.model.ProviderLocality
import com.servicechowk.app.databinding.ItemLocalitySearchBinding

class LocalityFilterAdapter(
        private val onClick:(String) -> Unit,
        private val currentList:ArrayList<ProviderLocality>
): RecyclerView.Adapter<LocalityFilterAdapter.LocalityFilterViewHolder>(),Filterable {

    private var filteredList = arrayListOf<ProviderLocality>()


    inner class LocalityFilterViewHolder(val binding: ItemLocalitySearchBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(locality: ProviderLocality){
            binding.apply {

                tvLocality.text = locality.value

                root.setOnClickListener {
                    onClick(locality.value)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalityFilterViewHolder {
        val binding = ItemLocalitySearchBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LocalityFilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocalityFilterViewHolder, position: Int) {
        val item = filteredList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
      //  println("FILTERDEBUG: $filteredList")
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                println("FILTERDEBUG:results $constraint $currentList")
                val charString = constraint.toString()
                filteredList = if (charString.isEmpty()){
                    arrayListOf()
                }else{
                    val list = arrayListOf<ProviderLocality>()
                    for (row in currentList){
                        if (row.value.toLowerCase().contains(charString.toLowerCase())){
                            list.add(row)
                        }
                    }
                    list
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as ArrayList<ProviderLocality>
                notifyDataSetChanged()
            }

        }
    }
}