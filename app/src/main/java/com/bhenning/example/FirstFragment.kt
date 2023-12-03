package com.bhenning.example

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bhenning.example.databinding.FirstFragmentBinding
import com.perimeterx.mobile_sdk.main.PXInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class FirstFragment : Fragment() {

    private var _binding: FirstFragmentBinding? = null
    private val binding get() = _binding!!

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor("BrianMobile-1.0"))
        .addInterceptor(PXInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://cflare.bhenning.com")
        .client(okHttpClient.newBuilder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FirstFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = binding.loginCall

        nextButton.setOnClickListener {
            val loginService = retrofit.create(LoginService::class.java)
            val loginRequest = LoginRequest("henninb@gmail.com", "monday1")
            val call = loginService.login(loginRequest)

            Log.i("SecondFragment", "test info message")

            call.enqueue(object : Callback<String> {

                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        val responseData: String? = response.body()

                        if (!responseData.isNullOrEmpty()) {
                            Log.d("FirstFragment", "API response: $responseData")
                        } else {
                            Log.d("FirstFragment", "API response is empty or null")
                        }
                    } else {
                        Log.d("FirstFragment", "API response: failure")
                    }

                    // Enable the button on the main thread
                    requireActivity().runOnUiThread {
                        nextButton.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("FirstFragment", "API call failed", t)

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
