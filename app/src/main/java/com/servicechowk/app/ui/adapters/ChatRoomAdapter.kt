package com.servicechowk.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.servicechowk.app.data.model.Chat
import com.servicechowk.app.data.model.ChatRoom
import com.servicechowk.app.databinding.ItemChatBinding
import com.servicechowk.app.databinding.ItemChatRoomBinding
import com.servicechowk.app.other.Utility

class ChatRoomAdapter(
    private val chatRoom: (ChatRoom) -> Unit
): RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    inner class ChatRoomViewHolder(val binding: ItemChatRoomBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(room: ChatRoom){
            binding.apply {
                tvTime.text = Utility.formatDate(room.time)
                tvMessage.text = room.text

                root.setOnClickListener {
                    chatRoom(room)
                }

            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<ChatRoom>(){
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ChatRoom,
            newItem: ChatRoom
        ) = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this,diffCallback)

    fun submitData(list: List<ChatRoom>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding = ItemChatRoomBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ChatRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holderRoom: ChatRoomViewHolder, position: Int) {
        val item = differ.currentList[position]
        item?.let {
            holderRoom.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}