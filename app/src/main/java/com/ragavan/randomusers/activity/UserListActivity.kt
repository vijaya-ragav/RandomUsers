package com.ragavan.randomusers.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.ragavan.randomusers.R
import com.ragavan.randomusers.adapter.MyLoadStateAdapter
import com.ragavan.randomusers.adapter.RandomUserListAdapter
import com.ragavan.randomusers.adapter.SearchUserListAdapter
import com.ragavan.randomusers.common.CheckingConnection
import com.ragavan.randomusers.common.Status
import com.ragavan.randomusers.common.isConnected
import com.ragavan.randomusers.databinding.ActivityUserListBinding
import com.ragavan.randomusers.utils.hide
import com.ragavan.randomusers.utils.show
import com.ragavan.randomusers.utils.snackBar
import com.ragavan.randomusers.viewmodel.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
@AndroidEntryPoint
class UserListActivity : AppCompatActivity() {

    private val viewModel: UserListViewModel by viewModels()
    private lateinit var networkConnection: CheckingConnection
    private var networkStatus: Boolean = false
    private lateinit var binding: ActivityUserListBinding
    lateinit var adapter: RandomUserListAdapter
    lateinit var searchAdapter: SearchUserListAdapter
    lateinit var loadStateAdapter: MyLoadStateAdapter

    private val PERMISSIONS_REQUEST = 100

    private val sunny = arrayOf(1000, 1003, 1006)
    private val cloudy = arrayOf(1006, 1009, 1030, 1063, 1066, 1069, 1072, 1087)
    private val thunder = arrayOf(1273, 1276, 1279, 1282)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_list)
        binding.uiModel = viewModel
        binding.lifecycleOwner = this

        /* creating instance of NetworkConnection to start broadcast receiver
         that extents livedata which is lifecycle aware*/
        networkConnection = CheckingConnection(this)
        networkConnection.observe(this) {
            networkStatus = it
        }
        networkStatus = isConnected

        binding.searchBar.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {

                if (text?.isEmpty()!!) {
                    setUserList()
                } else {
                    getSearchResult(text.toString())
                }

            }

        })

        binding.searchBar.clear.setOnClickListener {
            if (binding.searchBar.searchText.text.isNotEmpty())
                binding.searchBar.searchText.setText("")
        }

        binding.retryButton.setOnClickListener {
            getTrendingRepository()
        }

        checkOrAskLocationPermission {  getWeatherReport(getLastKnownLocation(this)) }

    }


    private fun getWeatherReport(coordinates: String) {
        if (networkStatus) {
            // Observers changes in the TrendingDataModelItem and update recyclerview
            viewModel.getWeatherReport(coordinates).observe(this, {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.weatherLayout.temperatureView.text =
                            it.data?.current?.temp_c.toString()
                        binding.weatherLayout.locationView.text = it.data?.location?.name.toString()
                        binding.weatherLayout.weatherDescription.text =
                            it.data?.current?.condition?.text
                        binding.weatherLayout.feelLike.text =
                            "feel's like " + (it.data?.current?.feelslike_c)
                        binding.weatherLayout.humidityText.text = it.data?.current?.humidity.toString()+" %"
                        binding.weatherLayout.visibilityText.text = it.data?.current?.vis_km.toString()+" km"
                        if (it.data?.current?.condition?.code in sunny) {
                            binding.weatherLayout.weatherImageView.setImageResource(R.drawable.cloudy)
                            if (it.data?.current?.is_day == 0) {
                                binding.weatherLayout.weatherImageView.setImageResource(R.drawable.night)
                            }
                        } else if (it.data?.current?.condition?.code in cloudy) {
                            binding.weatherLayout.weatherImageView.setImageResource(R.drawable.cloud)
                            if (it.data?.current?.is_day == 0) {
                                binding.weatherLayout.weatherImageView.setImageResource(R.drawable.cloudy_night)
                            }
                        } else if (it.data?.current?.condition?.code in thunder) {
                            binding.weatherLayout.weatherImageView.setImageResource(R.drawable.storm)
                        } else {
                            binding.weatherLayout.weatherImageView.setImageResource(R.drawable.rain)
                        }
                        if (it.data?.current?.is_day == 0) {
                            binding.weatherLayout.mainLayout.setBackgroundResource(R.drawable.weather_background_night)
                        }
                        binding.progressBar.hide()
                        binding.weatherLayout.weatherMainLayout.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        binding.progressBar.hide()
                        binding.mainLayout.snackBar(it.message!!)
                        binding.weatherLayout.weatherMainLayout.visibility = View.GONE
                    }
                    Status.LOADING -> binding.progressBar.show()
                }
            })
            setUserList()
        }

    }

    private fun setUserList() {
        viewModel.noResult.set(false)
        binding.progressBar.hide()

        adapter = RandomUserListAdapter()
        loadStateAdapter = MyLoadStateAdapter { adapter.retry() }
        binding.userRecyclerView.adapter = adapter.withLoadStateFooter(loadStateAdapter)

        lifecycleScope.launch {
            viewModel.getUserList().distinctUntilChanged().collectLatest {
                adapter.submitData(it)
            }
        }
        adapter.onItemClick = {
            startNextActivity(it.email)
        }

    }

    // this method check the internet connection and request viewmodel for response
    private fun getTrendingRepository() {
        if (networkStatus) {
            viewModel.noConnection.set(false)

//            getTrendingResult()
        } else {
            viewModel.getDatabaseCount().observe(this, {
                if (it) {

                    viewModel.noConnection.set(false)
//                    getOfflineData()
                    binding.progressBar.hide()
                    binding.mainLayout.snackBar("No internet connection. Loading offline data...")
                } else {
                    binding.progressBar.hide()
                    viewModel.noConnection.set(true)
                }

            })

        }
    }

    //Pass search query argument to database and observes the result
    fun getSearchResult(search: String) {

        viewModel.getSearchResponse(search).observe(this, { it ->
            if (it.isEmpty()) {
                viewModel.noResult.set(true)
            } else {
                viewModel.noResult.set(false)
                binding.userRecyclerView.apply {
                    searchAdapter = SearchUserListAdapter(it)
                    adapter = searchAdapter
                    searchAdapter.onItemClick = {
                        startNextActivity(it.email)
                    }
                }
            }

        })
    }

    private fun startNextActivity(email:String){
        val intent = Intent(this, UserDetailsActivity::class.java).apply {
            putExtra(Companion.EXTRA_MESSAGE, email)
        }
        startActivity(intent)
    }


    private fun getLastKnownLocation(context: Context):String {
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager.getProviders(true)
        var location: Location? = null
        for (i in providers.size - 1 downTo 0) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
            } else {
                location= locationManager.getLastKnownLocation(providers[i])
                if (location != null)
                    break
            }

        }
        val gps = DoubleArray(2)
        if (location != null) {
            gps[0] = location.getLatitude()
            gps[1] = location.getLongitude()
            Log.e("gpsLat",gps[0].toString())
            Log.e("gpsLong",gps[1].toString())

        }
        return gps[0].toString()+","+gps[1].toString()
    }

    private fun checkOrAskLocationPermission(callback: () -> Unit) {
        // Check GPS is enabled
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
            buildAlertMessageNoGps(this)
            return
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            callback.invoke()
        } else {
            // callback will be inside the activity's onRequestPermissionsResult(
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastKnownLocation(this)
            }
        }
    }
    private fun buildAlertMessageNoGps(context: Context) {
        val builder = AlertDialog.Builder(context);
        builder.setMessage("Your GPS is disabled. Do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel(); }
        val alert = builder.create();
        alert.show();
    }

    companion object {
        const val EXTRA_MESSAGE = "com.ragavan.randomusers.MESSAGE"
    }
}