package zelgius.com.myrecipes.data.repository.share

import android.content.Context
import androidx.collection.SimpleArrayMap
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.qualifiers.ApplicationContext
import zelgius.com.myrecipes.data.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        const val SERVICE_ID = "zelgius.com.myrecipes.sharing.service"
    }

    private val client = Nearby.getConnectionsClient(context)
    private var emitter: Boolean = false

    private class DataReceivedCallback : PayloadCallback() {
        private val incomingFilePayloads = SimpleArrayMap<Long, Payload>()
        private val completedFilePayloads = SimpleArrayMap<Long, Payload>()

        override fun onPayloadReceived(endPointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    payload.asBytes()?.let {
                        Logger.i(String(it, Charsets.UTF_8))
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endPointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                val payload = incomingFilePayloads.remove(update.payloadId)
                completedFilePayloads.put(update.payloadId, payload)
            }
        }
    }

    private inner class ConnectingProcessCallback : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endPointId: String, info: ConnectionInfo) {

            Nearby.getConnectionsClient(context)
                .acceptConnection(endPointId, DataReceivedCallback())
            //Nearby.getConnectionsClient(context).rejectConnection(endPointId)

        }

        override fun onConnectionResult(endPointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {

                ConnectionsStatusCodes.STATUS_OK -> {
                    if (emitter) {
                        sendText("Hello world !", endPointId)
                    }
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Logger.i( "conexion rechazada")
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Logger.i(  "DESCONEXION")
                }
            }
        }

        override fun onDisconnected(endPointId: String) {
            Logger.i( "DESCONEXION OK")
        }

    }

    private fun sendText(text: String, endPointId: String) {
        val payload = Payload.fromBytes(text.toByteArray())
        client.sendPayload(endPointId, payload)
    }

    fun startDiscovering() {
        val discoveryOptions =
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        client
            .startDiscovery(SERVICE_ID, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endPointId: String, info: DiscoveredEndpointInfo) {
                    client.requestConnection(
                        android.os.Build.MODEL,
                        endPointId,
                        ConnectingProcessCallback()
                    )
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener {
                        }
                }

                override fun onEndpointLost(endPointId: String) {
                    Logger.i("endpoint lost")
                }
            }, discoveryOptions)
            .addOnSuccessListener { Logger.i("discovering...") }
            .addOnFailureListener {
                Logger.i("failure discovering...")
            }
    }

    fun stopAdvertising() {
        client.stopAdvertising()
        emitter = false
    }
    fun stopDiscovering() {
        client.stopDiscovery()
        emitter = false

    }
}