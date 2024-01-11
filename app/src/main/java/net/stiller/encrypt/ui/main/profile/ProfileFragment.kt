package net.stiller.encrypt.ui.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.stiller.encrypt.data.models.User
import net.stiller.encrypt.databinding.FragmentProfileBinding
import net.stiller.encrypt.ui.auth.AuthActivity
import net.stiller.encrypt.ui.main.ModalBottomSheet
import net.stiller.encrypt.utils.StateInput
import net.stiller.encrypt.utils.StateInput.Companion.delete
import net.stiller.encrypt.utils.StateInput.Companion.password
import net.stiller.encrypt.utils.StateInput.Companion.phone
import net.stiller.encrypt.utils.StateInput.Companion.username

class ProfileFragment : Fragment(), ModalBottomSheet.OnUserUpdateListener {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var listener: OnUserProfileListener
    private var binding: FragmentProfileBinding? = null
    private val b get() = binding!!

    interface OnUserProfileListener {
        fun showSheet(input: StateInput<*>?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.viewModel = viewModel
        b.lifecycleOwner = viewLifecycleOwner

        viewModel.userLiveData.observe(viewLifecycleOwner) { user: User ->
            Log.d(TAG, user.phone)
        }

        b.logout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            activity?.finish()
        }

        b.tvUsername.setOnClickListener { listener.showSheet(username<Any>()) }
        b.tvPhone.setOnClickListener { listener.showSheet(phone<Any>()) }
        b.tvPassword.setOnClickListener { listener.showSheet(password<Any>()) }
        b.btnDelete.setOnClickListener { listener.showSheet(delete<Any>()) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUserProfileListener) {
            listener = context
        }
    }

    override fun onUpdate(id: String) {
        viewModel.read(id)
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            b.viewModel = viewModel
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val TAG = "ProfileFragment"
        fun newInstance() = ProfileFragment()
    }
}