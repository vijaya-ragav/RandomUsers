package com.ragavan.randomusers.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.paging.ExperimentalPagingApi
import com.bumptech.glide.Glide
import com.ragavan.randomusers.R
import com.ragavan.randomusers.activity.UserListActivity.Companion.EXTRA_MESSAGE
import com.ragavan.randomusers.common.CheckingConnection
import com.ragavan.randomusers.common.Status
import com.ragavan.randomusers.common.isConnected
import com.ragavan.randomusers.databinding.ActivityUserDetailsBinding
import com.ragavan.randomusers.utils.snackBar
import com.ragavan.randomusers.viewmodel.UserDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalPagingApi
@AndroidEntryPoint
class UserDetailsActivity : AppCompatActivity() {

    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var networkConnection: CheckingConnection
    private var networkStatus: Boolean = false
    private lateinit var binding: ActivityUserDetailsBinding


    private val sunny = arrayOf(1000, 1003, 1006)
    private val cloudy = arrayOf(1006, 1009, 1030, 1063, 1066, 1069, 1072, 1087)
    private val thunder = arrayOf(1273, 1276, 1279, 1282)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_details)

        /* creating instance of NetworkConnection to start broadcast receiver
         that extents livedata which is lifecycle aware*/
        networkConnection = CheckingConnection(this)
        networkConnection.observe(this) {
            networkStatus = it
        }
        networkStatus = isConnected

        binding.closeImage.setOnClickListener {
            finish()
        }

        // Get the Intent that started this activity and extract the string
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        Log.e("i am from second", message.toString())
        getSearchResult(message.toString())
    }


    //Pass search query argument to database and observes the result
    fun getSearchResult(search: String) {

        viewModel.getUserDetails(search).observe(this, { it ->
            Log.e("i am from response", it.cell)
            if (it.email.equals(search)) {
                binding.personName.text = it.name.first+" "+it.name.last
                binding.locationText.text = it.location.country

                binding.emailText.text = it.email
                binding.phoneText.text = it.phone
                binding.dobText.text = formatDate(it.dob.date)
                binding.genderText.text = it.gender
                binding.addressText.text = it.location.street.number.toString()+","+it.location.street.streetName+","+it.location.city+
                        ","+it.location.state+","+it.location.country

                Glide.with(this)
                    .load(it.picture.large)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(binding.personImage)

                getWeatherReport(it.location.coordinates.latitude+","+it.location.coordinates.longitude)
            }
        })
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

                        binding.weatherLayout.weatherMainLayout.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {

                        binding.mainLayout.snackBar(it.message!!)
                        binding.weatherLayout.weatherMainLayout.visibility = View.GONE
                    }
                    Status.LOADING -> ""
                }
            })
        }

    }

    fun formatDate(date:String):String{
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        var convertedDate: Date? = null
        var formattedDate: String? = null
        try {
            convertedDate = sdf.parse(date)
            formattedDate = SimpleDateFormat("MMMM dd,yyyy").format(convertedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return formattedDate.toString()
    }

}