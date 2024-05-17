package com.example.mapssages.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.mapssages.MainActivity
import com.example.mapssages.R
import com.example.mapssages.databinding.FragmentMapBinding
import com.example.mapssages.model.Message
import com.example.mapssages.ui.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Date

class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnInfoWindowClickListener,
    GoogleMap.InfoWindowAdapter {

    private lateinit var viewModel: ViewModel
    private val TAG = "TAG"
    private lateinit var mMap: GoogleMap

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // pour enregistrer la position de l'utilisateur
    private var userLocation: Location? = null

    private var markerCamera: Marker? = null

    // pour suivre position de l'utilisateur
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Déclaration pour le callback de la mise à jour de la position de l'utilisateur
    // Le callback est appelé à chaque fois que la position de l'utilisateur change
    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            userLocation = locationResult.lastLocation!!
            Log.d(TAG, "onLocationResult: ${userLocation?.latitude} ${userLocation?.longitude}")
        }
    }

    private lateinit var locationRequest: LocationRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]


        // Initialisation et gestion des boutons Add
        viewModel.isConnected().observe(viewLifecycleOwner) { isConnected ->
            Log.d("onCreateView", "isConnected $isConnected")
            binding.mapAdd.isEnabled = isConnected
        }
        binding.mapAdd.setOnClickListener { addMarkerAtCenter() }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        mMap = googleMap

        // détection du click sur une fenêtre d'information d'un marqueur
        // (voir méthode onInfoWindowClick)
        mMap.setOnInfoWindowClickListener(this)
        // Permet de modifier l'apparence de la fenêtre d'information d'un marqueur
        // (voir méthode getInfoContents)
        mMap.setInfoWindowAdapter(this)

        // Permet de détecter le click sur le bouton de position de l'utilisateur
        // (voir méthode onMyLocationButtonClick)
        mMap.setOnMyLocationButtonClickListener(this)
        // Permet de détecter le click sur la position de l'utilisateur
        // (voir méthode onMyLocationClick)
        mMap.setOnMyLocationClickListener(this)

        // Détecter clic sur la carte
        // Ajoute un marqueur à l'endroit cliqué
        mMap.setOnMapClickListener { latLng ->
            Log.d(TAG, "onMapClick: $latLng")
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

            // Exemple pour supprimer un marqueur
            // Supprime le marqueur ajouté par le bouton Add s'il y en a un
            markerCamera?.remove()
        }

        // Détecter un click long sur la carte
        mMap.setOnMapLongClickListener { latLng ->
            Log.d(TAG, "onMapClick: $latLng")
            mMap.addMarker(MarkerOptions().position(latLng).title("Marker"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
        }

        // Écouteur pour le drag&drop d'un marqueur
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                Log.d(TAG, "onMarkerDragStart: " + marker.position)
            }

            override fun onMarkerDrag(marker: Marker) {
                Log.d(TAG, "onMarkerDrag: " + marker.position)
            }

            override fun onMarkerDragEnd(marker: Marker) {
                Log.d(TAG, "onMarkerDragEnd: " + marker.position)
            }
        })



        // Vérifie les permissions
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Demande la permission à l'utilisateur
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Les permissions sont déjà accordées
            enableMyLocation()
            lastPosition()
        }


        // ******* Localisation automatique ********
        // Configuration pour mise à jour automatique de la position
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(10000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()

        // Création de la requête pour la mise à jour de la position
        // avec la configuration précédente
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        // Création du client pour la mise à jour de la position.
        // Le client va permettre de vérifier si la configuration est correcte,
        // si l'utilisateur a activé ou désactivé la localisation
        val client = LocationServices.getSettingsClient(requireActivity())

        // Vérifie que la configuration de la mise à jour de la position est correcte
        // Si l'utilisateur a activé ou désactivé la localisation
        client.checkLocationSettings(request)
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: $it")
                // Si la configuration est correcte, on lance la mise à jour de la position
                fusedLocationClient.requestLocationUpdates(
                    // Configuration de la mise à jour de la position
                    locationRequest,
                    // Callback pour la mise à jour de la position
                    locationCallback,
                    null
                )
            }
            .addOnFailureListener {
                Log.d(TAG, "onFailure: $it")
                // Si la configuration n'est pas correcte, on affiche un message
                Toast.makeText(
                    requireActivity(),
                    "Veuillez activer la localisation",
                    Toast.LENGTH_SHORT
                ).show()
            }
        // ******* Fin localisation automatique ********


        // Parcours la liste de messages et positionne les marqueurs
        // On met l'objet message dans le Tag du marqueur
        viewModel.getMessages().observe(this) { messages ->
            if (messages != null) {
                for (message in messages) {
                    var lat: Double = 0.0
                    var lon: Double = 0.0
                    if (message.latitude != null) lat = message.latitude.toDouble()
                    if (message.longitude != null) lon = message.longitude.toDouble()
                    val position = LatLng(lat, lon)

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title("${message.lastName}, ${message.firstName}")
                    )?.tag = message
                }
            }
        }
    }

    // Méthode pour récupérer la dernière position connue de l'utilisateur
    @SuppressLint("MissingPermission")
    private fun lastPosition() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Vérifie que la position n'est pas null
                if (location != null) {
                    Log.d(TAG, "onSuccess: $location")
                    // Centre la carte sur la position de l'utilisateur
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f))
                }
            }
    }


    /**
     * Permet d'activer la localisation de l'utilisateur
     */
    private fun enableMyLocation() {
        // vérification si la permission de localisation est déjà donnée
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Active la localisation de l'utilisateur
            // Affiche le bouton de position de l'utilisateur
            mMap.isMyLocationEnabled = true
        } else {
            // Demande la permission de localisation
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Nouvelle méthode pour demander la permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission est accordée, continuer avec l'opération nécessitant la permission
                enableMyLocation()
                lastPosition()
            } else {
                // Permission refusée, gérer le cas
                handlePermissionDenied()
            }
        }


    private fun handlePermissionDenied() {
        // Vérifie si l'utilisateur a déjà refusé la permission et s'il faut afficher une explication.
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showRationaleDialog()
        } else {
            Toast.makeText(
                requireActivity(), "Localisation non activée", Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**
     * Affiche une boîte de dialogue expliquant pourquoi l'application a besoin de la permission
     * de localisation.
     */
    private fun showRationaleDialog() {
        AlertDialog.Builder(requireActivity()).apply {
            setTitle("Permission requise !")
            setMessage("Cette permission est importante pour la géolocalisation...")
            setPositiveButton("Ok") { _, _ ->
                // Demande à nouveau la permission. si l'utilisateur accepte l'explication.
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            setNegativeButton("Annuler") { _, _ ->
                Toast.makeText(
                    requireActivity(), "Localisation non activée", Toast.LENGTH_SHORT
                ).show()
            }
            // Affiche la boîte de dialogue.
            show()
        }
    }


    /**
     * Méthode pour détecter le clic sur le bouton de position
     */
    override fun onMyLocationButtonClick(): Boolean {
        Log.d(TAG, "onMyLocationButtonClick: ")
        return false
    }

    /**
     * Méthode pour détecter le clic sur la position de l'utilisateur
     */
    override fun onMyLocationClick(location: Location) {
        Log.d(TAG, "onMyLocationClick: $location")
    }

    /**
     * Gestion des clics sur les boutons
     */
    private fun navigateToLocation() {
        // Ajoute un marqueur à Québec et centre la carte sur ce marqueur
        val sydney = LatLng(46.79, -71.26)
        mMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Quebec")
        )
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))
    }

    private fun addMarkerAtCenter() {


        // positionner un marqueur au centre de la carte
        val cameraPosition = mMap.cameraPosition
        val position =
            LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude)

        this.context?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Nouveau Message")

                val messageInput = EditText(this.context)
                setView(messageInput)

                setPositiveButton("OK") { _, _ ->
                    if (messageInput.text.toString() != "") {
                        val firstName = (requireActivity() as MainActivity).getUserInfos()[0]
                        val lastName = (requireActivity() as MainActivity).getUserInfos()[1]
                        val content = messageInput.text.toString()
                        val lat = position.latitude
                        val lon = position.longitude

                        val newMessage = Message(
                            null,
                            firstName,
                            lastName,
                            lat.toFloat(),
                            lon.toFloat(),
                            content,
                            Date()
                        )
                        viewModel.addMessage(newMessage)

                        Toast.makeText(this.context, "Message ajouté", Toast.LENGTH_SHORT).show()
                    }
                }
                show()
            }
        }


    }

    /**
     * Gestion des clics sur les fenêtres d'information des marqueurs
     */
    override fun onInfoWindowClick(marker: Marker) {
        val location = Location("Marker")
        location.latitude = marker.position.latitude
        location.longitude = marker.position.longitude

        // calculer la distance de l'utilisateur avec le marqueur sélectionné
        val distance = userLocation?.distanceTo(location)
        if (distance != null) {

            viewModel.isConnected().observe(viewLifecycleOwner) { isConnected ->
                binding.mapDistance.text = "${distance / 1000} km"
            }
        }
    }

    /**
     * Méthode pour modifier l'apparence d'une fenêtre d'information
     */
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    /**
     * Méthode pour modifier le contenu d'une fenêtre d'information
     * en utilisant un layout
     */
    override fun getInfoContents(marker: Marker): View {
        // Utilise le layout marker_layout.xml pour la fenêtre d'information
        val view: View =
            LayoutInflater.from(requireActivity()).inflate(R.layout.marker_layout, null)
        val txtUsername = view.findViewById<TextView>(R.id.marker_username)
        val txtMessage = view.findViewById<TextView>(R.id.marker_message)
        val imgPfp = view.findViewById<ImageView>(R.id.marker_picture)
        txtUsername.text = marker.title

        // On récupère le message qui est dans le Tag du marqueur
        val message = (marker.tag as Message?)

        txtMessage.text = (marker.tag as Message?)?.message

        // FIX DE TEAMS
        // Utilise Coil pour charger l'image de la propriété depuis l'URL.
        val request = ImageRequest.Builder(requireContext())
            .data("https://robohash.org/_${message?.lastName}${message?.firstName}?set=set4")
            .allowHardware(false) // Évite des problèmes de rendu logiciel
            .target(
                onStart = { it ->
                    imgPfp.setImageResource(R.drawable.placeholder_pfp)
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                        marker.showInfoWindow()
                    }
                },
                onSuccess = { result ->
                    imgPfp.setImageDrawable(result)
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                        marker.showInfoWindow()
                    }
                },
                onError = { error ->
                    imgPfp.setImageResource(R.drawable.error_pfp)
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                        marker.showInfoWindow()
                    }
                }
            )
            .build()
        requireContext().imageLoader.enqueue(request)
        return view
    }




    companion object {
        private const val LOCATION_PERMISSION_CODE = 1
    }


}
