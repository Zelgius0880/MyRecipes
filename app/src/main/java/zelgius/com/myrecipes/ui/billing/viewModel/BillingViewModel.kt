package zelgius.com.myrecipes.ui.billing.viewModel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zelgius.billing.model.Product
import com.zelgius.billing.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {
    val products = billingRepository.products
    val isPremium = billingRepository.isPremium

    init {
        viewModelScope.launch {
            billingRepository.fetchProducts()
        }
    }

    fun startPurchase(activity: Activity, product: Product) = viewModelScope.launch {
        billingRepository.startPurchase(activity, product.type)
    }


}