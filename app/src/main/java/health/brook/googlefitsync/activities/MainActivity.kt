package health.brook.googlefitsync.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.data.HealthFields.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.material.snackbar.Snackbar
import health.brook.googlefitsync.R
import health.brook.googlefitsync.adapters.BloodReadingAdapter
import health.brook.googlefitsync.models.BloodReading
import health.brook.googlefitsync.utils.DateUtils
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    // Global properties

    private lateinit var mFitnessOptions: FitnessOptions
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = 1

    // View declarations

    private lateinit var bloodPressureRecyclerView: RecyclerView
    private lateinit var lastUpdateTextView: TextView
    private lateinit var swipeToRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // View binding

        bloodPressureRecyclerView = findViewById(R.id.bloodPressureRecyclerView)
        swipeToRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        lastUpdateTextView = findViewById(R.id.lastSyncTextView)

        // Layout initializations

        supportActionBar?.hide()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        bloodPressureRecyclerView.layoutManager = layoutManager

        // Google Fit initializations

        mFitnessOptions = FitnessOptions.builder().addDataType(
            HealthDataTypes.TYPE_BLOOD_PRESSURE,
            FitnessOptions.ACCESS_READ).build()

        val account = GoogleSignIn.getAccountForExtension(this, mFitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, mFitnessOptions)) {

            lastUpdateTextView.text = getString(R.string.please_enable_google_fit)

            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.enable_google_fit))
            builder.setMessage(getString(R.string.enable_google_fit_dialog_description))
            builder.setPositiveButton(android.R.string.ok) { _, _ -> }
            builder.show()

        } else {
            loadReadings()
        }

        // Swipe to refresh setup

        swipeToRefreshLayout.setOnRefreshListener {
               loadReadings()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // TODO: Update deprecated usage of Activity Result
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                    loadReadings()
                }
                else -> {
                    Snackbar.make(window.decorView, getString(R.string.we_need_permissions), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    // Request permissions on button click

    fun clickButton(v: View) {
        val account = GoogleSignIn.getAccountForExtension(this, mFitnessOptions)
        GoogleSignIn.requestPermissions(
            this,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            account,
            mFitnessOptions)
    }

    override fun onResume() {
        super.onResume()
        loadReadings()
    }

    private fun loadReadings () {

        // Define a time range of one month

        val today = Calendar.getInstance()
        val lastMonth = Calendar.getInstance()
        lastMonth.add(Calendar.DAY_OF_YEAR, -30)

        // Create a Request of buckets to Google Fit

        val readRequest = DataReadRequest.Builder()
            .aggregate(HealthDataTypes.TYPE_BLOOD_PRESSURE)
            .setTimeRange(lastMonth.timeInMillis, today.timeInMillis, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .enableServerQueries()
            .build()

        // Get account to be used
        val account = GoogleSignIn.getAccountForExtension(this, mFitnessOptions)

        // Read data and create a new list with the bucket values.
        swipeToRefreshLayout.isRefreshing = true
        val readingsList = mutableListOf<BloodReading>()

        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                response.buckets.forEach { bucket ->
                    bucket.dataSets.forEach {
                        if (!it.isEmpty) {
                            val systolic = it.dataPoints[0].getValue(
                                FIELD_BLOOD_PRESSURE_SYSTOLIC_MIN).asFloat()
                            val diastolic = it.dataPoints[0].getValue(
                                FIELD_BLOOD_PRESSURE_DIASTOLIC_MIN).asFloat()
                            val date = it.dataPoints[0].getEndTime(TimeUnit.MILLISECONDS)
                            readingsList.add(BloodReading(systolic, diastolic, date))
                        }
                    }
                }

                // Reverse list to get descending date order
                readingsList.reverse()

                // Create and assign adapter
                bloodPressureRecyclerView.adapter = BloodReadingAdapter(readingsList)

                // Finish loading records
                lastUpdateTextView.text = getString(R.string.last_sync, DateUtils.getNow())
                swipeToRefreshLayout.isRefreshing = false
                Snackbar.make(window.decorView, getString(R.string.loaded), Snackbar.LENGTH_LONG).show()

            }
            .addOnFailureListener{ e ->
                println(e)
                swipeToRefreshLayout.isRefreshing = false
                Snackbar.make(window.decorView, getString(R.string.failed_to_load), Snackbar.LENGTH_LONG).show()
            }

        Fitness.getSensorsClient(this, account).add(SensorRequest.Builder().setDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE).setSamplingRate(1, TimeUnit.SECONDS).build(), OnDataPointListener {
            println(it)
        })


        /* TODO: This is the progress for real-time changes tracking

        Fitness.getRecordingClient(this, account).subscribe(HealthDataTypes.TYPE_BLOOD_PRESSURE).addOnSuccessListener { }

        Fitness.getRecordingClient(this, account)
            .listSubscriptions()
            .addOnSuccessListener { subscriptions ->
                for (sc in subscriptions) {
                    val dt = sc.dataType
                }
        } */
    }
}