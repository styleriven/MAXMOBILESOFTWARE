package com.example.maxmobilesoftware

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity(), OnImageEditedListener {

    private lateinit var captureImageView: ImageView
    private lateinit var imageUri: Uri
    private val imageUriList = mutableListOf<Uri>() // List to hold captured image URIs
    private lateinit var imagesAdapter: ImagesAdapter // RecyclerView adapter

    private val takePictureContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            // Display the captured image in the main ImageView
            captureImageView.setImageURI(imageUri)
            // Add URI to the list and update the RecyclerView
            imageUriList.add(imageUri)
            imagesAdapter.notifyItemInserted(imageUriList.size - 1)
            // Prepare a new URI for the next image
            imageUri = createImageUri()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize URI, UI components, and adapter
        imageUri = createImageUri()
        captureImageView = findViewById(R.id.captureImageView)
        val captureImageButton = findViewById<Button>(R.id.captureImgBtn)
        val recyclerView = findViewById<RecyclerView>(R.id.capturedImagesRecyclerView)

        // Define the onImageClick lambda
        val onImageClick: (Uri) -> Unit = { uri ->
            val dialogFragment = ImageEditDialogFragment.newInstance(uri)
            dialogFragment.onImageEditedListener = this
            dialogFragment.show(supportFragmentManager, "ImageEditDialog")
        }

        // Setup RecyclerView with GridLayoutManager for a horizontal grid layout
        imagesAdapter = ImagesAdapter(imageUriList, onImageClick) // Pass the onImageClick lambda
        recyclerView.layoutManager = GridLayoutManager(this, 3) // Adjust columns as needed
        recyclerView.adapter = imagesAdapter

        // Capture image button click
        captureImageButton.setOnClickListener {
            takePictureContract.launch(imageUri)
        }
    }

    // Function to create a new image URI
    private fun createImageUri(): Uri {
        val imageFile = File(filesDir, "camera_photo_${System.currentTimeMillis()}.png")
        return FileProvider.getUriForFile(
            this,
            "com.example.maxmobilesoftware.FileProvider",
            imageFile
        )
    }


    override fun onImageAdded(editedImageUri: Uri) {
        imageUriList.add(editedImageUri)
        imagesAdapter.notifyItemInserted(imageUriList.size - 1)
    }

    override fun onImageDeleted(deletedImageUri: Uri) {
        // Remove the image from the list and update the adapter
        val position = imageUriList.indexOf(deletedImageUri)
        if (position != -1) {
            imageUriList.removeAt(position)
            imagesAdapter.notifyItemRemoved(position)
        }
    }


}

