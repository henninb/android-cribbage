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

        // Find the Button by its ID
        val nextButton = binding.ApiCall

        nextButton.setOnClickListener {
            val loginRequest = LoginRequest("henninb@gmail.com", "monday1")
            val loginService = retrofit.create(LoginService::class.java)
            val call = loginService.login(loginRequest)

            Log.d("SecondFragment", "button clicked")

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d("SecondFragment", "onResponse")
                    if (response.isSuccessful) {
                        // Handle a successful response here
                        val responseData = response.body()
                        println(responseData)
                        Log.d("SecondFragment", "API response: $responseData") // Log the response
                    } else {
                        println("failure")
                        Log.d("SecondFragment", "API response: failure") // Log the response
                        // Handle an unsuccessful response here
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    // Handle failure here
                }
            })
            // Navigate to the next fragment when the button is clicked
            //findNavController().navigate(R.id.action_secondFragment_to_thirdFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
