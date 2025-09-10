package com.lifelink.lifelink.ui.main


import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.lifelink.lifelink.R
import com.lifelink.lifelink.databinding.ActivityMainBinding
import com.lifelink.lifelink.ui.main.fragments.dashboard.DashboardFragment
import com.lifelink.lifelink.ui.main.fragments.donate.DonateFragment
import com.lifelink.lifelink.ui.main.fragments.profile.ProfileFragment
import com.lifelink.lifelink.ui.main.fragments.requesthelp.RequestHelpFragment
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dashboardStack = Stack<Fragment>()
    private val donateStack = Stack<Fragment>()
    private val requestStack = Stack<Fragment>()
    private val profileStack = Stack<Fragment>()

    private var activeStack: Stack<Fragment> = dashboardStack

    companion object {
        private const val ACTIVE_STACK_TAG_KEY = "active_stack_tag_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val dashboardFragment = DashboardFragment()
            val donateFragment = DonateFragment()
            val requestFragment = RequestHelpFragment()
            val profileFragment = ProfileFragment()

            dashboardStack.push(dashboardFragment)
            donateStack.push(donateFragment)
            requestStack.push(requestFragment)
            profileStack.push(profileFragment)

            supportFragmentManager.beginTransaction()
                .add(binding.navHostFrameLayout.id, profileFragment, "PROFILE").hide(profileFragment)
                .add(binding.navHostFrameLayout.id, requestFragment, "REQUEST").hide(requestFragment)
                .add(binding.navHostFrameLayout.id, donateFragment, "DONATE").hide(donateFragment)
                .add(binding.navHostFrameLayout.id, dashboardFragment, "DASHBOARD") // Don't hide the first one
                .commit()

            activeStack = dashboardStack

        } else {
            val dashboardFragment = supportFragmentManager.findFragmentByTag("DASHBOARD") as DashboardFragment
            val donateFragment = supportFragmentManager.findFragmentByTag("DONATE") as DonateFragment
            val requestFragment = supportFragmentManager.findFragmentByTag("REQUEST") as RequestHelpFragment
            val profileFragment = supportFragmentManager.findFragmentByTag("PROFILE") as ProfileFragment

            // 2. Re-populate your stacks with these existing fragments
            dashboardStack.push(dashboardFragment)
            donateStack.push(donateFragment)
            requestStack.push(requestFragment)
            profileStack.push(profileFragment)

            // 3. Restore the active stack
            val activeTag = savedInstanceState.getString(ACTIVE_STACK_TAG_KEY)
            activeStack = when (activeTag) {
                "DONATE" -> donateStack
                "REQUEST" -> requestStack
                "PROFILE" -> profileStack
                else -> dashboardStack
            }
        }


        updateBottomNavUI(activeStack)

        // Navigation clicks
        binding.dashboardNav.setOnClickListener { switchStack(dashboardStack) }
        binding.donateNav.setOnClickListener { switchStack(donateStack) }
        binding.requestHelpNav.setOnClickListener { switchStack(requestStack) }
        binding.profileSettingNav.setOnClickListener { switchStack(profileStack) }

        // Handle back press
        onBackPressedDispatcher.addCallback {
            when {
                // Case 1: deeper in a tab → pop fragment
                activeStack.size > 1 -> {
                    val fragment = activeStack.pop()
                    supportFragmentManager.beginTransaction()
                        .remove(fragment)
                        .show(activeStack.peek())
                        .commit()
                    updateBottomNavUI(activeStack)
                }

                activeStack != dashboardStack -> {
                    switchStack(dashboardStack)
                    updateBottomNavUI(dashboardStack)
                }

                else -> {
                    finish()
                }
            }
        }
    }

    private fun switchStack(targetStack: Stack<Fragment>) {
        if (targetStack == activeStack) return

        val transaction = supportFragmentManager.beginTransaction()
        if (activeStack.isNotEmpty()) {
            transaction.hide(activeStack.peek())
        }

        // The fragment should already be added from onCreate, so we just show it.
        transaction.show(targetStack.peek())

        transaction.commit()
        activeStack = targetStack
        updateBottomNavUI(activeStack)
    }

    private fun updateBottomNavUI(stack: Stack<Fragment>) {

        // Reset all textviews to gone first
        binding.dashboardNavTextView.visibility = View.GONE
        binding.donateNavTextView.visibility = View.GONE
        binding.requestHelpNavTextView.visibility = View.GONE
        binding.profileSettingNavTextView.visibility = View.GONE

        // Layout color reset
        binding.dashboardNav.backgroundTintList = null
        binding.donateNav.backgroundTintList = null
        binding.requestHelpNav.backgroundTintList = null
        binding.profileSettingNav.backgroundTintList = null

        val blackColor = ContextCompat.getColorStateList(this, R.color.black)
        // Tint reset
        binding.dashboardNavLogo.imageTintList = blackColor
        binding.donateNavLogo.imageTintList = blackColor
        binding.requestHelpNavLogo.imageTintList = blackColor
        binding.profileSettingNavLogo.imageTintList = blackColor


        // Check the top fragment of the stack
        val activeColor = ContextCompat.getColorStateList(this, R.color.app_theme_orange)
        val activityTintLogo = ContextCompat.getColorStateList(this, R.color.white)
        val textColor = ContextCompat.getColorStateList(this, R.color.white)

        when (stack) {
            dashboardStack -> {
                binding.dashboardNavTextView.visibility = View.VISIBLE
                binding.dashboardNavTextView.setTextColor(textColor)
                binding.dashboardNav.backgroundTintList = activeColor
                binding.dashboardNavLogo.imageTintList = activityTintLogo
            }
            donateStack -> {
                binding.donateNavTextView.visibility = View.VISIBLE
                binding.donateNavTextView.setTextColor(textColor)
                binding.donateNav.backgroundTintList = activeColor
                binding.donateNavLogo.imageTintList = activityTintLogo
            }
            requestStack -> {
                binding.requestHelpNavTextView.visibility = View.VISIBLE
                binding.requestHelpNavTextView.setTextColor(textColor)
                binding.requestHelpNav.backgroundTintList = activeColor
                binding.requestHelpNavLogo.imageTintList = activityTintLogo
            }
            profileStack -> {
                binding.profileSettingNavTextView.visibility = View.VISIBLE
                binding.profileSettingNavTextView.setTextColor(textColor)
                binding.profileSettingNav.backgroundTintList = activeColor
                binding.profileSettingNavLogo.imageTintList = activityTintLogo
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the tag of the fragment at the top of the active stack
        outState.putString(ACTIVE_STACK_TAG_KEY, activeStack.peek().tag)
    }


    // Push new fragment inside current tab
    fun pushFragment(fragment: Fragment, tag: String? = null) {
        activeStack.push(fragment)
        supportFragmentManager.beginTransaction()
            .hide(activeStack[activeStack.size - 2]) // hide previous
            .add(binding.navHostFrameLayout.id, fragment, tag)
            .commit()
    }

    // Navigate to a new fragment in a potentially different tab
    fun navigateToFragment(fragment: Fragment, targetStackIdentifier: String, tag: String? = null) {
        val targetStack = when (targetStackIdentifier.uppercase()) {
            "DASHBOARD" -> dashboardStack
            "DONATE" -> donateStack
            "REQUEST" -> requestStack
            "PROFILE" -> profileStack
            else -> throw IllegalArgumentException("Invalid targetStackIdentifier: $targetStackIdentifier")
        }

        val transaction = supportFragmentManager.beginTransaction()

        // Hide the currently visible fragment from active stack
        if (activeStack.isNotEmpty()) {
            transaction.hide(activeStack.peek())
        }

        // Switch stack if needed
        if (activeStack != targetStack) {
            activeStack = targetStack
        }

        // Push new fragment inside the chosen stack
        activeStack.push(fragment)
        transaction.add(binding.navHostFrameLayout.id, fragment, tag)

        transaction.commit()
        updateBottomNavUI(activeStack)
    }

    override fun onResume() {
        super.onResume()
        updateBottomNavUI(activeStack)
    }

}