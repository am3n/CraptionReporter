package ir.am3n.craptionreporter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi

class NetworkStateReceiver(
    private var context: Context?,
    private var transportTypes: Array<Int>? = null,
    val listener: Listener?
) : BroadcastReceiver() {


    enum class State {
        AVAILABLE,
        LOST,
        UNAVAILABLE
    }

    interface Listener {
        fun onChanged(state: State, network: Network? = null)
        fun onChangedOnLowApi(state: State)
        fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities)
    }


    private var connectivityManager: ConnectivityManager? = null
    private var networkInfo: NetworkInfo? = null
    var state = State.LOST
        private set

    private var handler: Handler? = Handler(Looper.getMainLooper())
    private var runnable = Runnable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listener?.onChanged(state)
        } else {
            onReceive(context, null)
        }
    }

    init {

        handler?.postDelayed(runnable, 500)

        connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        start()

    }

    private val allTransportTypes @RequiresApi(Build.VERSION_CODES.LOLLIPOP) get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                arrayOf(
                        NetworkCapabilities.TRANSPORT_BLUETOOTH,
                        NetworkCapabilities.TRANSPORT_CELLULAR,
                        NetworkCapabilities.TRANSPORT_ETHERNET,
                        NetworkCapabilities.TRANSPORT_LOWPAN,
                        NetworkCapabilities.TRANSPORT_VPN,
                        NetworkCapabilities.TRANSPORT_WIFI,
                        NetworkCapabilities.TRANSPORT_WIFI_AWARE
                )
            } else {
                arrayOf(
                        NetworkCapabilities.TRANSPORT_BLUETOOTH,
                        NetworkCapabilities.TRANSPORT_CELLULAR,
                        NetworkCapabilities.TRANSPORT_ETHERNET,
                        NetworkCapabilities.TRANSPORT_VPN,
                        NetworkCapabilities.TRANSPORT_WIFI,
                        NetworkCapabilities.TRANSPORT_WIFI_AWARE
                )
            }
        } else {
            arrayOf(
                    NetworkCapabilities.TRANSPORT_BLUETOOTH,
                    NetworkCapabilities.TRANSPORT_CELLULAR,
                    NetworkCapabilities.TRANSPORT_ETHERNET,
                    NetworkCapabilities.TRANSPORT_VPN,
                    NetworkCapabilities.TRANSPORT_WIFI
            )
        }
    private val networkCallback get() = @RequiresApi(Build.VERSION_CODES.LOLLIPOP) object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            //Log.d("Me-NetStateReceiver", "onAvailable()")
            if (state != State.AVAILABLE) {
                state = State.AVAILABLE
                listener?.onChanged(state, network)
                handler?.removeCallbacks(runnable)
            }
        }
        override fun onUnavailable() {
            super.onUnavailable()
            //Log.d("Me-NetStateReceiver", "onUnavailable()")
            if (state != State.UNAVAILABLE) {
                state = State.UNAVAILABLE
                listener?.onChanged(state)
                handler?.removeCallbacks(runnable)
            }
        }
        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            //Log.d("Me-NetStateReceiver", "onBlockedStatusChanged()")
        }
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            listener?.onCapabilitiesChanged(network, networkCapabilities)
        }
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            //Log.d("Me-NetStateReceiver", "onLinkPropertiesChanged()")
        }
        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            //Log.d("Me-NetStateReceiver", "onLosing()")
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            //Log.d("Me-NetStateReceiver", "onLost()")
            if (state != State.LOST) {
                state = State.LOST
                listener?.onChanged(state, network)
                handler?.removeCallbacks(runnable)
            }
        }
    }


    override fun onReceive(context: Context?, intent: Intent?) {

        if (connectivityManager == null)
            connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        connectivityManager?.let {
            networkInfo = it.activeNetworkInfo
            if (networkInfo != null && networkInfo!!.isConnected) {
                state = State.AVAILABLE
                try {
                    listener?.onChanged(state)
                } catch (t: Throwable) {
                    listener?.onChangedOnLowApi(state)
                }
                handler?.removeCallbacks(runnable)

            } else if (networkInfo == null || intent?.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) == true) {
                state = State.LOST
                try {
                    listener?.onChanged(state)
                } catch (t: Throwable) {
                    listener?.onChangedOnLowApi(state)
                }
                handler?.removeCallbacks(runnable)
            }
            return@let
        }

    }

    private fun start() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                val builder = NetworkRequest.Builder()
                if (transportTypes != null) {
                    allTransportTypes.forEach { builder.removeTransportType(it) }
                    transportTypes!!.forEach { builder.addTransportType(it) }
                }
                connectivityManager?.registerNetworkCallback(builder.build(), networkCallback)

            } else {
                context?.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            } else {
                context?.unregisterReceiver(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restart() {
        stop()
        start()
    }

}
