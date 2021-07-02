package com.servicechowk.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.servicechowk.app.R
import com.servicechowk.app.data.model.Chat
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.ItemChatBinding
import com.servicechowk.app.databinding.ItemProviderAdapterBinding
import com.servicechowk.app.other.Utility

class ChatAdapter: RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(chat: Chat){
            binding.apply {
                if (chat.sentByCustomer == true){
                    relCustomerMessage.isVisible = true
                    relProviderMessage.isVisible = false

                    tvMyMessage.text = chat.text
                    tvMyTime.text = Utility.formatDate(chat.createdAt.toString())

                }else{
                    relCustomerMessage.isVisible = false
                    relProviderMessage.isVisible = true

                    tvOtherMessage.text = chat.text
                    tvOtherTime.text = Utility.formatDate(chat.createdAt.toString())

                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Chat>(){
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Chat,
            newItem: Chat
        ) = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this,diffCallback)

    fun submitData(list: List<Chat>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = differ.currentList[position]
        item?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}