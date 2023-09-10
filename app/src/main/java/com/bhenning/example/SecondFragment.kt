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

class SecondFragment : Fragment() {
    private var _binding: SecondFragmentBinding? = null
    private val binding get() = _binding!!

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(PXInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://cflare.bhenning.com/")
        .client(okHttpClient)
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
            val loginRequest = LoginRequest("henninb@gmail.com", "monday1")
            val loginService = retrofit.create(LoginService::class.java)
            val call = loginService.login(loginRequest)

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        Log.d("SecondFragment", "API response: $responseData")
                    } else {
                        Log.d("SecondFragment", "API response: failure")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("SecondFragment", "API call failed", t)
                    // Handle failure here (e.g., show an error message to the user)
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


