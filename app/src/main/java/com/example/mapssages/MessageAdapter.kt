package com.example.mapssages

import com.example.mapssages.ui.ViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mapssages.model.Message

// Interface pour gérer les clics sur les éléments de la liste
interface OnItemClickListenerInterface {
    fun onItemClick(itemView: View?, position: Int)
}



class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder?>() {

    private var mTodo: List<Message>? = null
    private lateinit var viewModel: ViewModel


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_one_line, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val message: Message = mTodo!![position]
        if (message != null) {
            holder.messageUsername.text = "${message.lastName}, ${message.firstName}"
            holder.messageContent.text = message.message

            holder.messageUserPfp.load("https://robohash.org/_${message.lastName}${message.firstName}?set=set4") {
                placeholder(R.drawable.placeholder_pfp)
                error(R.drawable.error_pfp)
            }
        }
    }

    override fun getItemCount(): Int {
        return mTodo?.size ?: 0
    }

    fun setTodos(todoList: List<Message>) {
        mTodo = todoList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageUsername: TextView = itemView.findViewById(R.id.message_username)
        val messageContent: TextView = itemView.findViewById(R.id.message_content)
        val messageUserPfp: ImageView = itemView.findViewById(R.id.message_userpfp)

        init {}
    }
}
