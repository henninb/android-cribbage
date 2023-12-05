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

    val baseUrl = "https://api.bhenning.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
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

            Log.i("SecondFragment", "Login API clicked.")

            call.enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseData: LoginResponse? = response.body()

                        if (responseData != null) {
                            Log.i("FirstFragment", "Login API response: $responseData")
                        } else {
                            Log.i("FirstFragment", "Login API response is empty or null")
                        }
                    } else {
                        Log.i("FirstFragment", "Login API response: failure")
                    }

                    // Enable the button on the main thread
                    requireActivity().runOnUiThread {
                        nextButton.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
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
