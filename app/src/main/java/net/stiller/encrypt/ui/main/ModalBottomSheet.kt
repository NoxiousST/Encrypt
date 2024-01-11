package net.stiller.encrypt.ui.main

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import net.stiller.encrypt.data.models.User
import net.stiller.encrypt.databinding.SheetDeleteBinding
import net.stiller.encrypt.databinding.SheetPasswordBinding
import net.stiller.encrypt.databinding.SheetPhoneBinding
import net.stiller.encrypt.databinding.SheetUsernameBinding
import net.stiller.encrypt.ui.main.profile.ProfileViewModel
import net.stiller.encrypt.utils.InputStatus
import net.stiller.encrypt.utils.StateInput


class ModalBottomSheet : BottomSheetDialogFragment() {

    private var viewModel: ProfileViewModel? = null
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var input: StateInput<*>
    private lateinit var dBinding: SheetDeleteBinding
    private lateinit var pBinding: SheetPasswordBinding
    private lateinit var phBinding: SheetPhoneBinding
    private lateinit var uBinding: SheetUsernameBinding
    private var listener: OnUserUpdateListener? = null
    private var isPassword = false
    private var isCPassword = false

    companion object {
        const val TAG = "ModalBottomSheet"
        fun newInstance(input: StateInput<*>) = ModalBottomSheet().apply {
            this.input = input
        }
    }

    interface OnUserUpdateListener {
        fun onUpdate(id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = activity as OnUserUpdateListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                activity.toString()
                        + " must implement TextClicked"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        when (input.inputStatus) {
            InputStatus.DELETE -> {
                dBinding = SheetDeleteBinding.inflate(inflater, container, false)
                return dBinding.root
            }

            InputStatus.PASSWORD -> {
                pBinding = SheetPasswordBinding.inflate(inflater, container, false)
                return pBinding.root
            }

            InputStatus.PHONE -> {
                phBinding = SheetPhoneBinding.inflate(inflater, container, false)
                return phBinding.root
            }

            InputStatus.USERNAME -> {
                uBinding = SheetUsernameBinding.inflate(inflater, container, false)
                return uBinding.root
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        viewModel?.userLiveData?.observe(viewLifecycleOwner) { user: User ->
            when (input.inputStatus) {
                InputStatus.DELETE -> {
                    dBinding.viewModel = viewModel

                    val passwordObservable: Observable<CharSequence> =
                        dBinding.etPassword.textChanges().skipInitialValue()
                    val mapPasswordObservables =
                        passwordObservable.map { it.toString() == user.password }

                    mapPasswordObservables.subscribe(object : DisposableObserver<Boolean>() {
                        override fun onNext(isEnabled: Boolean) {
                            dBinding.btnDelete.isEnabled = isEnabled
                        }

                        override fun onError(e: Throwable) {}
                        override fun onComplete() {}
                    })
                }

                InputStatus.PASSWORD -> {
                    pBinding.viewModel = viewModel
                    pBinding.btnPassword.setOnClickListener {
                        viewModel?.update(
                            user.id!!,
                            "password",
                            pBinding.etPassword.text.toString()
                        )
                        listener?.onUpdate(user.id!!)
                        dialog.dismiss()
                    }

                    val passwordObservable: Observable<Boolean> =
                        pBinding.etPassword.textChanges().skipInitialValue()
                            .map { isValidForm(it.toString(), 0, user) }
                    val cPasswordObservable: Observable<Boolean> =
                        pBinding.etCPassword.textChanges().skipInitialValue()
                            .map { isValidForm(it.toString(), 1, user) }

                    val combinedObservable =
                        Observable.merge(passwordObservable, cPasswordObservable)
                    combinedObservable.subscribe(object : DisposableObserver<Boolean>() {
                        override fun onNext(t: Boolean) {
                            pBinding.btnPassword.isEnabled = t
                        }

                        override fun onError(e: Throwable) {}
                        override fun onComplete() {}
                    })
                }

                InputStatus.PHONE -> {
                    phBinding.viewModel = viewModel

                    phBinding.btnAdd.setOnClickListener {
                        viewModel?.update(
                            user.id!!,
                            "phoneNumber",
                            phBinding.etPhone.text.toString()
                        )
                        listener?.onUpdate(user.id!!)

                        dialog.dismiss()
                    }

                    val phoneObservable: Observable<CharSequence> =
                        phBinding.etPhone.textChanges().skipInitialValue()
                    phoneObservable.subscribe(object : DisposableObserver<CharSequence>() {
                        override fun onNext(t: CharSequence) {
                            val s = phBinding.etPhone.editableText
                            val prefixLength = 4

                            // Disable delete prefix
                            if (t.length <= prefixLength - 1) {
                                s.insert(prefixLength - 1, " ")
                            }

                            // Replace starting 0
                            if (t.length == prefixLength + 2 && t[prefixLength] == '0') {
                                s.delete(prefixLength, prefixLength + 1)
                            }

                            // Insert char where needed.
                            if (t.length == 4 + prefixLength || t.length == 9 + prefixLength) {
                                val c = t[t.length - 1]
                                // Only if its a digit where there should be a space we insert a space
                                if (Character.isDigit(c) && TextUtils.split(
                                        t.toString(),
                                        " "
                                    ).size <= 3
                                ) {
                                    s.insert(
                                        t.length - 1, "-"
                                    )
                                }
                            }

                            // Remove spacing char
                            if (t.length == 4 + prefixLength || t.length == 9 + prefixLength) {
                                val c = t[t.length - 1]
                                if (c == '-') {
                                    s.delete(t.length - 1, t.length)
                                }
                            }

                            phBinding.btnAdd.isEnabled = t.length == 17
                        }

                        override fun onError(e: Throwable) {}
                        override fun onComplete() {}
                    })


                }

                InputStatus.USERNAME -> {
                    uBinding.viewModel = viewModel

                    val usernameObservable: Observable<CharSequence> =
                        uBinding.etUsername.textChanges().skipInitialValue()
                    val mapUsernameObservables =
                        usernameObservable.map { it.toString() != user.username }

                    mapUsernameObservables.subscribe(object : DisposableObserver<Boolean>() {
                        override fun onNext(t: Boolean) {
                            if (t) uBinding.tlUsername.isErrorEnabled = false
                        }

                        override fun onError(e: Throwable) {}
                        override fun onComplete() {}
                    })

                    uBinding.btnUpdate.setOnClickListener {
                        val t = uBinding.etUsername.text.toString() == user.username
                        uBinding.btnUpdate.isEnabled = t
                        uBinding.tlUsername.apply {
                            isErrorEnabled = !t
                            error = if (!t) "New username must be different" else null
                        }
                    }
                }
            }
        }
    }


    private fun isValidForm(text: String, index: Int, user: User): Boolean {
        when (index) {
            0 -> {
                pBinding.tlPassword.isErrorEnabled = false

                isPassword = true
                if (text.length < 6 && text.isNotEmpty()) {
                    isPassword = false
                    pBinding.tlPassword.error = "Password must contains 6 or more digit"
                } else if (text == user.password) {
                    isPassword = false
                    pBinding.tlPassword.error =
                        "New password must be different from current password"
                }

                pBinding.tlCPassword.isErrorEnabled = false
                isCPassword =
                    text == pBinding.etCPassword.text.toString() || pBinding.etCPassword.text.toString()
                        .isEmpty()
                if (!isCPassword) pBinding.tlCPassword.error =
                    "Password confirmation does not match"
            }

            1 -> {
                pBinding.tlCPassword.isErrorEnabled = false
                isCPassword = text == pBinding.etPassword.text.toString() || text.isEmpty()
                if (!isCPassword) pBinding.tlCPassword.error =
                    "Password confirmation does not match"
            }
        }

        return isPassword && isCPassword
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            val sheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            behavior = BottomSheetBehavior.from(sheet!!)
            behavior.isHideable = true
            behavior.isDraggable = true
            behavior.isShouldRemoveExpandedCorners = true
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return dialog
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}