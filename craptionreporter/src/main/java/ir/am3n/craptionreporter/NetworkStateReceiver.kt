package ir.am3n.craptionreporter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkStateReceiver(
    internal var context: Context?,
    var connectivityListener: ConnectivityListener
) : BroadcastReceiver() {

    private var networkIntentFilter = IntentFilter()
    private var connectivityManager: ConnectivityManager? = null
    private var networkInfo: NetworkInfo? = null

    private var state = State.DISCONNECTED

    private val connectionState: NetworkInfo.State get() =
        if (networkInfo != null) {
            networkInfo!!.state
        } else
            NetworkInfo.State.UNKNOWN

    enum class State {
        CONNECTED,
        DISCONNECTED
    }

    init {
        networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        context?.registerReceiver(this, networkIntentFilter)
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        connectivityManager?.let {
            networkInfo = it.activeNetworkInfo
            if (networkInfo != null && networkInfo!!.isConnected) {
                state = State.CONNECTED
                connectivityListener.onConnectionStateChanged(state)

            } else if (networkInfo == null || intent?.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)==true) {
                state = State.DISCONNECTED
                connectivityListener.onConnectionStateChanged(state)
            }
        }
    }

    fun onRestart() {
        try {
            onStop()
            context?.registerReceiver(this, networkIntentFilter)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun onStop() {
        try {
            context?.unregisterReceiver(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface ConnectivityListener {
        fun onConnectionStateChanged(state: State)
    }

}
