package health.brook.googlefitsync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private var mFitnessOptions: FitnessOptions? = null
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFitnessOptions = FitnessOptions.builder().addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ).build()

        val account = GoogleSignIn.getAccountForExtension(this, mFitnessOptions!!)

        if (!GoogleSignIn.hasPermissions(account, mFitnessOptions!!)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                mFitnessOptions!!)
        } else {
            // TODO: Granted permissions behavior
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                    // TODO: Permissions were granted
                }
                else -> {
                    // TODO: Failed to grant permissions
                }
            }
            else -> {
                // TODO: Result Code is different
            }
        }
    }

    fun clickButton(v: View) {

        // TODO: Clean dates implementation
        val today = Calendar.getInstance()
        val twoDaysAgo = Calendar.getInstance()
        twoDaysAgo.add(Calendar.DAY_OF_YEAR, -2)

        val readRequest = DataReadRequest.Builder()
            .aggregate(HealthDataTypes.TYPE_BLOOD_PRESSURE)
            .setTimeRange(twoDaysAgo.timeInMillis, today.timeInMillis, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .enableServerQueries()
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, mFitnessOptions!!)

        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                response.buckets.forEach {
                    println(it.dataSets)
                }
            }
            .addOnFailureListener{ e ->
                println(e)
                // TODO: Display reading error to user.
            }

    }
}