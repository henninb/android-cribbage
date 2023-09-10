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
        .addInterceptor(PXInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fixturedownload.com")
        //.baseUrl("https://cflare.bhenning.com/")
        //.client(okHttpClient)
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
//            val loginRequest = LoginRequest("henninb@gmail.com", "monday1")
//            val loginService = retrofit.create(LoginService::class.java)
//            val call = loginService.login(loginRequest)

            val scheduleService = retrofit.create(ScheduleService::class.java)
            val call = scheduleService.schedule()
            call.enqueue(object : Callback<Array<ApiResponse>> {


                override fun onResponse(
                    call: Call<Array<ApiResponse>>,
                    response: Response<Array<ApiResponse>>
                ) {
                    if (response.isSuccessful) {
                        val responseData: Array<ApiResponse>? = response.body()

                        if (!responseData.isNullOrEmpty()) {
                            for (row in responseData) {
                                Log.d("SecondFragment", "API response row: $row")
                            }
                        } else {
                            Log.d("SecondFragment", "API response is empty or null")
                        }
                    } else {
                        Log.d("SecondFragment", "API response: failure")
                    }
//                    if (response.isSuccessful) {
//                        val responseData: Array<ApiResponse>? = response.body()
//
//                        responseData?.firstOrNull()?.let { firstRow ->
//                            Log.d("SecondFragment", "API response: $firstRow")
//                        } ?: run {
//                            Log.d("SecondFragment", "API response is empty or null")
//                        }
//                        //val firstRow: ApiResponse = responseData[0]
//                        //Log.d("SecondFragment", "API response: $firstRow")
//                    } else {
//                        Log.d("SecondFragment", "API response: failure")
//                    }
                }

                override fun onFailure(call: Call<Array<ApiResponse>>, t: Throwable) {
                    Log.e("SecondFragment", "API call failed", t)
                }
            })

            // Disable the button or show a loading spinner to indicate the ongoing request
            nextButton.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


