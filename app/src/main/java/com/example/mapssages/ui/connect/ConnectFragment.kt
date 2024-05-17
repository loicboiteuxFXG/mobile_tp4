package com.example.mapssages.ui.connect

import android.media.MediaPlayer
import android.os.Build
import com.example.mapssages.ui.ViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mapssages.R
import com.example.mapssages.databinding.FragmentConnectBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ConnectFragment : Fragment() {

    private var _binding: FragmentConnectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var viewModel: ViewModel
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var btnSubmit: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtPasssword: EditText

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        firebaseAuth = Firebase.auth

        _binding = FragmentConnectBinding.inflate(inflater, container, false)
        val root: View = binding.root

        btnSubmit = root.findViewById(R.id.connectSubmit)
        txtEmail = root.findViewById(R.id.connectEmail)
        txtPasssword = root.findViewById(R.id.connectPassword)

        viewModel.isConnected().observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                Toast.makeText(this.context, "Vous êtes connecté", Toast.LENGTH_SHORT).show()
                val action = ConnectFragmentDirections.actionNavConnectToNavList()
                findNavController().navigate(action)
            } else {
                Toast.makeText(this.context, "Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubmit.setOnClickListener() {
            if (txtEmail.text.toString() == "" || txtPasssword.text.toString() == "") {
                Toast.makeText(this.context, "Courriel ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(
                    txtEmail.text.toString(),
                    txtPasssword.text.toString()
                ).addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val mp = MediaPlayer.create(context, R.raw.connect)
                        mp.start()
                        viewModel.setConnected(true)
                    } else {
                        Toast.makeText(this.context, "Courriel ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}