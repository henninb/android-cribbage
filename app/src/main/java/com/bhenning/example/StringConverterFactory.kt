//package com.bhenning.example
//
//import okhttp3.ResponseBody
//import retrofit2.Converter
//import retrofit2.Retrofit
//import java.lang.reflect.Type
//
//class StringConverterFactory : Converter.Factory() {
//    override fun responseBodyConverter(
//        type: Type,
//        annotations: Array<Annotation>,
//        retrofit: Retrofit
//    ): Converter<ResponseBody, *>? {
//        if (type == String::class.java) {
//            return Converter { value -> value.string() }
//        }
//        return super.responseBodyConverter(type, annotations, retrofit)
//    }
//
//    companion object {
//        fun create(): StringConverterFactory {
//            return StringConverterFactory()
//        }
//    }
//}