package com.bhenning.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bhenning.simple.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.perimeterx.mobile_sdk.PerimeterX
import com.perimeterx.mobile_sdk.PerimeterXDelegate
import com.perimeterx.mobile_sdk.main.PXPolicy

class MainActivity : AppCompatActivity(), PerimeterXDelegate {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        val policy = PXPolicy()
        policy.setDomains(arrayListOf("my-domain.com"), "<APP_ID>")

//        try {
//            PerimeterX.start(this, "<APP_ID>", this, policy)
//        }
//        catch (exception: Exception) {
//            println("failed to start. error: ${exception.message}")
//        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun perimeterxChallengeCancelledHandler(appId: String) {
        TODO("Not yet implemented")
    }

    override fun perimeterxChallengeSolvedHandler(appId: String) {
        TODO("Not yet implemented")
    }

    override fun perimeterxHeadersWereUpdated(headers: HashMap<String, String>, appId: String) {
        TODO("Not yet implemented")
    }

    override fun perimeterxRequestBlockedHandler(url: String?, appId: String) {
        TODO("Not yet implemented")
    }
}