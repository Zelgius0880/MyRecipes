package zelgius.com.myrecipes.data.repository

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class RemoteConfigRepository {
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    private val _stableHordeKey = MutableStateFlow<String?>(null)
    val stableHordeKey = _stableHordeKey.filterNotNull()


    private val _billingProductKey = MutableStateFlow<String?>(null)
    val billingProductKey = _billingProductKey.filterNotNull()

    private val _billingSubscriptionKey = MutableStateFlow<String?>(null)
    val billingSubscriptionKey = _billingSubscriptionKey.filterNotNull()


    init {
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _stableHordeKey.value = remoteConfig.getString("stable_horde_key")
                    _billingProductKey.value = remoteConfig.getString("billing_product_key")
                    _billingSubscriptionKey.value = remoteConfig.getString("billing_subscription_key")
                }
            }

    }

}