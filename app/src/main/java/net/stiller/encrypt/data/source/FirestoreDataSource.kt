package net.stiller.encrypt.data.source

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Flowable
import net.stiller.encrypt.data.models.User
import net.stiller.encrypt.utils.AppConstants
import net.stiller.encrypt.utils.Crypt

class FirestoreDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection(AppConstants.USERS_NODE)
    private val crypt = Crypt()

    fun register(user: User): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val euser = crypt.encrypt(user)
            val document = collection.document(euser.id!!)
            document.set(euser)
                .addOnFailureListener { t: Exception? -> emitter.onError(t!!) }
                .addOnSuccessListener { emitter.onComplete() }
        }
    }

    fun login(userEmail: String, password: String): Flowable<User> {
        return Flowable.create({ emitter ->
            val login =
                collection.addSnapshotListener { documentSnapshot, e ->
                    if (e != null) emitter.onError(e)
                    if (documentSnapshot != null) {
                        var found = false
                        val users = documentSnapshot.toObjects(User::class.java)
                        for (user in users) {
                            val duser = crypt.decrypt(user)
                            Log.d(TAG, duser.email)
                            Log.d(TAG, duser.password)
                            if ((duser.username == userEmail || duser.email == userEmail) &&
                                duser.password == password) {
                                emitter.onNext(duser)
                                found = true
                                break
                            }
                        }
                        if (!found) emitter.onError(Throwable("Incorrect email or password"))
                        emitter.onComplete()
                    }
                }
            emitter.setCancellable { login.remove() }
        }, BackpressureStrategy.BUFFER)
    }

    fun read(id: String): Flowable<User> {
        return Flowable.create({ emitter ->
            val document = collection.document(id)
            document.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null) {
                        val user = result.toObject(User::class.java)
                            ?.let { crypt.decrypt(it) }
                        emitter.onNext(user!!)
                    }
                }
            }
                .addOnFailureListener { error: Exception? -> emitter.onError(error!!) }
                .addOnSuccessListener { emitter.onComplete() }
        }, BackpressureStrategy.BUFFER)
    }

    fun update(id: String, field: String, value: String): Completable {
        return Completable.create { emitter ->
            val document = collection.document(id)
            document.update(field, value)
                .addOnFailureListener { t: Exception? -> emitter.onError(t!!) }
                .addOnSuccessListener { emitter.onComplete() }
        }
    }

    companion object {
        private const val TAG: String = "FirestoreDataSource"
    }
}