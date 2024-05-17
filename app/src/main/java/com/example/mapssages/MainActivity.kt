package com.example.mapssages


import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.view.Menu
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mapssages.databinding.ActivityMainBinding
import com.example.mapssages.ui.ViewModel
import com.google.android.material.navigation.NavigationView
import android.Manifest


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var prefs: SharedPreferences

    // br est une instance de la classe MyBroadCastReceiver
    private val SMSbr: BroadcastReceiver = SMSBroadcastReceiver()
    private val smsPermissionRequestCode = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        binding.appBarMain.toolbar
        prefs = getSharedPreferences("mapssages", MODE_PRIVATE)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_map, R.id.nav_list, R.id.nav_connect
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Vérifier la permission au lancement de l'application
        checkForSmsPermission()

    }

    override fun onStart() {
        super.onStart()
        val smsFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(SMSbr, smsFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(SMSbr)
    }

    private fun checkForSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED) {

            // Si la permission n'est pas accordée, la demander
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS),
                smsPermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == smsPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                Toast.makeText(this, "SMS receive permission autorisée", Toast.LENGTH_SHORT).show()
            } else {
                // Permission refusée
                Toast.makeText(this, "SMS receive permission refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val viewModel = ViewModelProvider(this)[ViewModel::class.java]
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.toolbar_disconnect).setOnMenuItemClickListener {
            viewModel.signOut()
            Toast.makeText(this, "Vous êtes déconnecté", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_global_nav_connect)
            true
        }

        menu.findItem(R.id.toolbar_delete).setOnMenuItemClickListener {
            viewModel.deleteAllMessages()
            Toast.makeText(this, "Base de donnée supprimée", Toast.LENGTH_SHORT).show()
            true
        }

        menu.findItem(R.id.toolbar_settings).setOnMenuItemClickListener {
            createForm()
            true
        }
        return true
    }

    fun getUserInfos(): Array<String?> {
        val firstName = prefs.getString("firstName", "Cégep")
        val lastName = prefs.getString("lastName", "Garneau")
        val returnArray: Array<String?> = arrayOf(firstName, lastName)
        return returnArray
    }

    private fun createForm() {
        return this.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater.
            val inflater = it.layoutInflater;
            val dialogView = inflater.inflate(R.layout.fragment_settings, null)
            dialogView.findViewById<EditText>(R.id.settingsFirstName).setText(getUserInfos()[0])
            dialogView.findViewById<EditText>(R.id.settingsLastName).setText(getUserInfos()[1])
            // Inflate and set the layout for the dialog.
            // Pass null as the parent view because it's going in the dialog
            // layout.
            builder.setView(dialogView)
                .setTitle("SETTINGS")
                .setPositiveButton("OK") { _, _ ->
                    val firstName = dialogView.findViewById<EditText>(R.id.settingsFirstName).text.toString()
                    val lastName = dialogView.findViewById<EditText>(R.id.settingsLastName).text.toString()

                    if (firstName.isEmpty() || lastName.isEmpty()) {
                        Toast.makeText(this, "Informations invalides", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Informations mises à jour", Toast.LENGTH_SHORT).show()
                        setUserInfos(firstName, lastName)
                    }

                }
            builder.create()
                .show()

        }
    }

    private fun setUserInfos(firstName: String, lastName: String) {
        val editor = prefs.edit() // Obtention d'un éditeur pour SharedPreferences.
        editor.putString("firstName", firstName)
        editor.putString("lastName", lastName)
        editor.apply() // Application des changements.
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}