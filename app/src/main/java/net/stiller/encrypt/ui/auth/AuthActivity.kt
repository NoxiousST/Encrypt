package net.stiller.encrypt.ui.auth


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import net.stiller.encrypt.databinding.ActivityAuthBinding
import net.stiller.encrypt.ui.auth.login.LoginFragment
import net.stiller.encrypt.ui.auth.splash.SplashFragment


class AuthActivity : AppCompatActivity() {

    private lateinit var b: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(b.fragAuth.id, SplashFragment.newInstance())
                .commitNow()
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(b.fragAuth.id, fragment)
            .setReorderingAllowed(true)
            .commitNow()
        Log.d(TAG, supportFragmentManager.fragments.size.toString())
    }

    companion object {
        private const val TAG = "AuthActivity"
        fun newInstance() = LoginFragment()
    }
}