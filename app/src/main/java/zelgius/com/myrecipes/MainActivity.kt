package zelgius.com.myrecipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.facebook.stetho.Stetho
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import net.alhazmy13.mediapicker.Image.ImagePicker
import zelgius.com.myrecipes.fragments.OnBackPressedListener
import zelgius.com.myrecipes.utils.observe


class MainActivity : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN = 4567
        val TAG = MainActivity::class.java.simpleName
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val navController by lazy { findNavController(this, R.id.nav_host_fragment) }

    private val viewModel by lazy { ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(RecipeViewModel::class.java) }

    //private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        Stetho.initializeWithDefaults(this)
        if(intent != null) processIntent(intent)

        //setSupportActionBar(toolbar)

        /*viewModel.connectedUser.observe(this, Observer {
            if (it != null) {
                Picasso.get()
                    .load(it.photoUrl)
                    .into(account)

                accountProgress.visibility = View.GONE
            } else
                with(account) {
                    setImageResource(R.drawable.ic_account_circle_24dp)
                    imageTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.md_grey_500))
                }
        })

        account.setOnClickListener {
            if (viewModel.user == null)
                signIn()
            else
                signOut()
        }*/
    }

    /*override fun onStart() {
        super.onStart()
        viewModel.user = auth.currentUser
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }*/

    /*private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {
            viewModel.user = null
        }
    }*/

    /*private fun revokeAccess() {
        // Firebase sign out
        auth.signOut()

        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            viewModel.user = null
        }
    }*/


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(intent != null) processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        if(intent.hasExtra("ID_FROM_NOTIF")) {
            viewModel.loadRecipe(intent.getLongExtra("ID_FROM_NOTIF", 0L)).observe(this){
                if(it != null) {
                    if(navController.currentDestination?.id != R.id.tabFragment)
                        navController.navigate(R.id.tabFragment)

                    navController.navigate(
                        R.id.action_tabFragment_to_recipeFragment
                        , bundleOf("RECIPE" to it), null, null
                    )

                    viewModel.loadRecipe(it.id!!)
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments?.last()

        if (fragment is OnBackPressedListener) {
            fragment.onBackPressed()
        }

        return navController.navigateUp()
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments?.last()

        if (fragment is OnBackPressedListener) {
            fragment.onBackPressed()
        }

        super.onBackPressed()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                ImagePicker.IMAGE_PICKER_REQUEST_CODE -> {
                    val paths = data?.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH)

                    if (!paths.isNullOrEmpty()) {
                        viewModel.selectedImageUrl.value = Uri.parse("file://${paths.first()}")
                    }
                }
                /*RC_SIGN_IN -> {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account = task.getResult(ApiException::class.java)
                        firebaseAuthWithGoogle(account!!)
                    } catch (e: ApiException) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e)
                        // ...
                    }
                }*/

            }
    }
    /* private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
         Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
         // [START_EXCLUDE silent]
         //accountProgress.visibility = View.VISIBLE
         // [END_EXCLUDE]

         val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
         auth.signInWithCredential(credential)
             .addOnCompleteListener(this) { task ->
                 if (task.isSuccessful) {
                     // Sign in success, update UI with the signed-in user's information
                     Log.d(TAG, "signInWithCredential:success")
                     val user = auth.currentUser
                     viewModel.user = user
                 } else {
                     // If sign in fails, display a message to the user.
                     Log.w(TAG, "signInWithCredential:failure", task.exception)
                     Snackbar.make(coordinator, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                     viewModel.user = null
                 }

                 // [START_EXCLUDE]
                 //accountProgress.visibility = View.GONE

                 // [END_EXCLUDE]
             }
     }*/
}