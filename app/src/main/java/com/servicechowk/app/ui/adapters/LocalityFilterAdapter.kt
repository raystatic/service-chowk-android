package com.servicechowk.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.servicechowk.app.data.model.ProviderLocality
import com.servicechowk.app.databinding.ItemLocalitySearchBinding

class LocalityFilterAdapter(
        private val onClick:(String) -> Unit
): RecyclerView.Adapter<LocalityFilterAdapter.LocalityFilterViewHolder>() {

    private val TAG = "GLIDEDEBUG"

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

    private val diffCallback = object : DiffUtil.ItemCallback<ProviderLocality>(){
        override fun areItemsTheSame(oldItem: ProviderLocality, newItem: ProviderLocality): Boolean =
            oldItem.value == newItem.value

        override fun areContentsTheSame(
            oldItem: ProviderLocality,
            newItem: ProviderLocality
        ) = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this,diffCallback)

    fun submitData(list: List<ProviderLocality>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalityFilterViewHolder {
        val binding = ItemLocalitySearchBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LocalityFilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocalityFilterViewHolder, position: Int) {
        val item = differ.currentList[position]
        item?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}