package com.example.mapssages.ui.list

import com.example.mapssages.ui.ViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mapssages.model.Message
import com.example.mapssages.MessageAdapter
import com.example.mapssages.databinding.FragmentListBinding

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var messageList: MutableList<Message>
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var viewModel: ViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.getMessages().observe(viewLifecycleOwner) { messages ->
            if (messages != null) {
                messageAdapter.setTodos(messages)
                messageList = messages as MutableList<Message>
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configure RecyclerView
        messageList = ArrayList()
        recyclerView = binding.rvMessages
        recyclerView.layoutManager = GridLayoutManager(context, 1)
        messageAdapter = MessageAdapter()
        recyclerView.adapter = messageAdapter

        /**
         * Gestion du swipe à gauche pour supprimer un enregistrement
         */
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                    val itemPosition = viewHolder.adapterPosition
//                    adapterTodo.notifyItemRemoved(itemPosition)

                    // Suppression de l'enregistrement de Firebase grâce à son id
                    //                    Cr.document(todoList[itemPosition].id!!).delete()
                    Log.d("TAG", "onSwiped: todoList[itemPosition] ${messageList[itemPosition].id}")
                    viewModel.deleteMessage(messageList[itemPosition])
                }
            }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}