package com.servicechowk.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.servicechowk.app.R
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.ItemProviderAdapterBinding

class ProvidersAdapter(
    private val onStartChat:(User) -> Unit,
    private val onViewProfile:(User) -> Unit
): RecyclerView.Adapter<ProvidersAdapter.ProvidersViewHolder>() {

    inner class ProvidersViewHolder(val binding:ItemProviderAdapterBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(user: User){
            binding.apply {

                Glide.with(itemView)
                    .load(user.photo)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(imgProfile)

                tvName.text = user.name
               // tvVerified.isVisible = user.isVerified == true
                tvWorkField.text = user.workField
                val location = "${user.locality}, ${user.city}"
                tvLocation.text = location

                btnChat.setOnClickListener {
                    onStartChat(user)
                }

                btnViewProfile.setOnClickListener {
                    onViewProfile(user)
                }

            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<User>(){
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: User,
            newItem: User
        ) = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this,diffCallback)

    fun submitData(list: List<User>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvidersViewHolder {
        val binding = ItemProviderAdapterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ProvidersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProvidersViewHolder, position: Int) {
        val item = differ.currentList[position]
        item?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}