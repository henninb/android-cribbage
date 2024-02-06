package com.bhenning.example

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bhenning.example.databinding.SecondFragmentBinding
import com.perimeterx.mobile_sdk.main.PXInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SecondFragment : Fragment() {
    private var _binding: SecondFragmentBinding? = null
    private val binding get() = _binding!!

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor("BrianMobile-1.0"))
        .addInterceptor(PXInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fixturedownload.com")
        //.baseUrl("https://cflare.bhenning.com")
        .client(okHttpClient.newBuilder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SecondFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = binding.ApiCall

        nextButton.setOnClickListener {
            val scheduleService = retrofit.create(ScheduleService::class.java)
            val call = scheduleService.schedule()

            Log.i("SecondFragment", "Schedule API clicked.")

            call.enqueue(object : Callback<Array<ScheduleResponse>> {

                override fun onResponse(
                    call: Call<Array<ScheduleResponse>>,
                    response: Response<Array<ScheduleResponse>>
                ) {
                    if (response.isSuccessful) {
                        val responseData: Array<ScheduleResponse>? = response.body()

                        if (!responseData.isNullOrEmpty()) {
                            for (row in responseData) {
                                Log.i("SecondFragment", "Schedule API response row: $row")
                            }
                        } else {
                            Log.i("SecondFragment", "Schedule API response is empty or null")
                        }
                    } else {
                        Log.i("SecondFragment", "Schedule API response: ${response.code()}")
                    }

                    // Enable the button on the main thread
                    requireActivity().runOnUiThread {
                        nextButton.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<Array<ScheduleResponse>>, t: Throwable) {
                    Log.e("SecondFragment", "API call failed", t)

                    // Enable the button on the main thread
                    requireActivity().runOnUiThread {
                        nextButton.isEnabled = true
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


