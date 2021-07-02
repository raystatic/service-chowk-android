package com.servicechowk.app.ui.adapters

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.servicechowk.app.R
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.ItemProviderAdapterBinding
import com.servicechowk.app.other.GlideApp
import com.servicechowk.app.other.GlideApp.with
import com.servicechowk.app.other.MyGlideApp
import com.squareup.picasso.Picasso

class ProvidersAdapter(
    private val onStartChat:(User) -> Unit,
    private val onViewProfile:(User) -> Unit
): RecyclerView.Adapter<ProvidersAdapter.ProvidersViewHolder>() {

    private val TAG = "GLIDEDEBUG"

    inner class ProvidersViewHolder(val binding: ItemProviderAdapterBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(user: User){
            binding.apply {

                val url = "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
                Glide.with(itemView)
                        .load(user.photo)
                        .placeholder(R.drawable.person)
                        .error(R.drawable.person)
                        .into(imgProfile)


//                Picasso.get()
//                        .load("https://firebasestorage.googleapis.com/v0/b/service-chowk.appspot.com/o/images%2F%2B918810319452%2Faadhar.jpg?alt=media&token=a58daf65-274e-4f46-80a0-a945cb235134")
//                        .placeholder(R.drawable.person)
//                        .error(R.drawable.ic_launcher_background)
//                        .into(imgProfile)


                tvName.text = user.name
                tvVerified.isVisible = user.isVerified == true
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