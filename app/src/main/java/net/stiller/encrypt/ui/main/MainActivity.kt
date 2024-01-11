package net.stiller.encrypt.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.stiller.encrypt.R
import net.stiller.encrypt.databinding.ActivityMainBinding
import net.stiller.encrypt.ui.auth.login.LoginFragment
import net.stiller.encrypt.ui.main.ModalBottomSheet.Companion.newInstance
import net.stiller.encrypt.ui.main.ModalBottomSheet.OnUserUpdateListener
import net.stiller.encrypt.ui.main.home.HomeFragment
import net.stiller.encrypt.ui.main.profile.ProfileFragment
import net.stiller.encrypt.ui.main.profile.ProfileFragment.OnUserProfileListener
import net.stiller.encrypt.utils.StateInput


class MainActivity : AppCompatActivity(), OnUserProfileListener, OnUserUpdateListener {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        window.navigationBarColor = getColor(R.color.md_theme_dark_background)
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_home, HomeFragment())
            .commit()

        b.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    supportFragmentManager
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_home, HomeFragment())
                        .commit()

                    return@setOnItemSelectedListener true
                }

                R.id.activity, R.id.cart -> {
                    return@setOnItemSelectedListener false
                }

                R.id.profile -> {
                    supportFragmentManager
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_home, ProfileFragment())
                        .commit()

                    return@setOnItemSelectedListener true
                }
            }
            true
        }
    }

    fun refreshFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_home, ProfileFragment())
            .commit()
    }

    override fun showSheet(input: StateInput<*>?) {
        newInstance(input!!)
            .show(supportFragmentManager, ModalBottomSheet.TAG)
    }

    override fun onUpdate(id: String) {
        val fragmentTag = ProfileFragment::class.java.simpleName
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag) as? ProfileFragment
        fragment?.onUpdate(id)
    }

    companion object {
        private const val TAG = "MainActivity"
        fun newInstance() = LoginFragment()
    }
}