package com.example.mapssages.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mapssages.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ViewModel : ViewModel() {
    // Connexion à la base de données Firestore.
    private val db = FirebaseFirestore.getInstance()

    // Crée un alias pour la collection "messages" de Firestore pour faciliter l'accès.
    private val collectionMessages = db.collection("messages")

    // Initialise FirebaseAuth pour gérer l'authentification.
    private val auth: FirebaseAuth

    // Variable LiveData pour surveiller l'état de connexion de l'utilisateur.
    private var isConnected: MutableLiveData<Boolean> = MutableLiveData(false)

    // Stocke une liste de tâches (todos) observables.
    private val messages = MutableLiveData<List<Message>?>()

    init {
        // Charge les messages au démarrage du com.example.mapssages.ui.ViewModel.
        loadMessages()
        // Initialise l'authentification Firebase.
        auth = FirebaseAuth.getInstance()
    }

    // Fournit un accès aux messages.
    fun getMessages(): MutableLiveData<List<Message>?> = messages

    // Fournit un accès à l'état de connexion de l'utilisateur.
    fun isConnected(): MutableLiveData<Boolean> = isConnected

    private fun loadMessages() {
        // Écoute les modifications dans la collection "messages" et met à jour `messages`.
        collectionMessages.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("com.example.mapssages.ui.ViewModel", "Erreur lors de la lecture des messages", e)
                    messages.value = null
                    return@addSnapshotListener
                }

                // Transforme chaque document en objet Message.
                val messageList = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Message::class.java)?.apply {
                        this.id = document.id
                        println(this.id)
                    }
                }
                messages.value = messageList
            }
    }

    // Ajoute un nouveau message à Firestore.
    fun addMessage(message: Message) {
        db.collection("messages").add(message)
            .addOnSuccessListener {
                Log.d("com.example.mapssages.ui.ViewModel", "DocumentSnapshot ajouté avec ID: ${it.id}")
            }.addOnFailureListener { e ->
                Log.e("com.example.mapssages.ui.ViewModel", "Erreur lors de l'ajout du document", e)
            }
    }

    // Supprime un message spécifique de Firestore.
    fun deleteMessage(message: Message) {
        message.id?.let {
            db.collection("messages").document(it).delete()
                .addOnSuccessListener {
                    Log.d("com.example.mapssages.ui.ViewModel", "DocumentSnapshot supprimé avec ID: $it")
                }.addOnFailureListener { e ->
                    Log.e("com.example.mapssages.ui.ViewModel", "Erreur lors de la suppression du document", e)
                }
        }
    }

    // Supprime la base de donnée Firestore
    fun deleteAllMessages() {
        getMessages().value?.forEach { message ->
            deleteMessage(message)
        }
    }

    // Déconnecte l'utilisateur et met à jour `isConnected`.
    fun signOut() {
        auth.signOut()
        isConnected.value = false
    }

    // Met à jour l'état de connexion de l'utilisateur.
    fun setConnected(value: Boolean) {
        isConnected.value = value
    }


}
