package com.example.signinregister.ui.main.profile

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavOptions
import com.example.signinregister.databinding.ActivityProfileBinding
import com.example.signinregister.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var navController: NavController

    private lateinit var tabs: List<TextView>

    private val tabToFragmentMap = mapOf(
        R.id.homeFragment to 0,
        R.id.personalInfoFragment to 1,
        R.id.securityFragment to 2,
        R.id.privacyDataFragment to 3
    )
    private var currentTabIndex = 0
    private var tabWidth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.content_container) as NavHostFragment
        navController = navHostFragment.navController

        tabs = listOf(
            binding.tabHome,
            binding.tabPersonalInfo,
            binding.tabSecurity,
            binding.tabPrivacyData
        )

        setupIndicatorInitialState()
        setupTabListeners()
        setupBackButton()
    }

    private fun setupIndicatorInitialState() {
        binding.tabsContainer.doOnLayout {
            if (tabs.isNotEmpty()) {
                tabWidth = binding.tabsContainer.width / tabs.size

                val initialDestinationId = navController.currentDestination?.id ?: R.id.homeFragment
                updateTabSelection(initialDestinationId)
            }
        }
    }

    private fun selectTab(newIndex: Int) {
        tabs[currentTabIndex].setTextColor(Color.BLACK)
        tabs[currentTabIndex].setTypeface(null, Typeface.NORMAL)

        tabs[newIndex].setTextColor(ContextCompat.getColor(this, R.color.black))
        tabs[newIndex].setTypeface(null, Typeface.BOLD)

        currentTabIndex = newIndex
    }

    private fun animateIndicator(targetIndex: Int) {
        if (tabWidth == 0) return

        val targetX = tabWidth * targetIndex

        ObjectAnimator.ofFloat(binding.tabIndicator, View.TRANSLATION_X, targetX.toFloat()).apply {
            ObjectAnimator.setFrameDelay(300)
            start()
        }
    }

    private fun setupTabListeners() {
        binding.tabHome.setOnClickListener {
            handleTabNavigation(R.id.homeFragment)
        }

        binding.tabPersonalInfo.setOnClickListener {
            handleTabNavigation(R.id.personalInfoFragment)
        }

        binding.tabSecurity.setOnClickListener {
            handleTabNavigation(R.id.securityFragment)
        }

        binding.tabPrivacyData.setOnClickListener {
            handleTabNavigation(R.id.privacyDataFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateTabSelection(destination.id)
        }
    }

    private fun handleTabNavigation(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return

        val newIndex = tabToFragmentMap[destinationId] ?: return
        updateTabSelection(destinationId)

        binding.contentContainer.visibility = View.INVISIBLE
        binding.loadingProgressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            delay(500)

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, inclusive = false, saveState = true)
                .setLaunchSingleTop(true)
                .build()

            navController.navigate(destinationId, null, navOptions)

            binding.loadingProgressBar.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            if (!navController.popBackStack()) {
                finish()
            }
        }
    }

    private fun updateTabSelection(destinationId: Int) {
        val newIndex = tabToFragmentMap[destinationId] ?: return

        if (newIndex != currentTabIndex || tabWidth == 0) {
            selectTab(newIndex)
            animateIndicator(newIndex)
        }
    }
}