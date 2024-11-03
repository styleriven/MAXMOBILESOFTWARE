package com.example.maxmobilesoftware

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream

class ImageEditDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_URI = "image_uri"
        private const val CROP_IMAGE_REQUEST_CODE = 1001

        fun newInstance(uri: Uri): ImageEditDialogFragment {
            val fragment = ImageEditDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE_URI, uri)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var paint: Paint
    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap
    private var imageUri: Uri? = null
    private lateinit var imageView: ImageView
    var onImageEditedListener: OnImageEditedListener? = null
    private lateinit var originalBitmap: Bitmap // Bitmap for the original image
    private lateinit var combinedBitmap: Bitmap // Bitmap for the combined image (original + drawings)
    private lateinit var drawCanvas: Canvas // Canvas for drawing
    private lateinit var drawPaint: Paint // Paint for drawing
    private var isDrawing = false // Flag to check if drawing is enabled
    private var text: String = ""
    private var textX: Float = 100f // Initial X position
    private var textY: Float = 100f // Initial Y position
    private var isDragging: Boolean = false // Flag to indicate if text is being dragged
    private var lastTouchX: Float = 0f // Last touch X coordinate
    private var lastTouchY: Float = 0f // Last touch Y coordinate


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_image_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        imageView = view.findViewById(R.id.fullSizeImageView)
        imageUri = arguments?.getParcelable(ARG_IMAGE_URI)

        // Set ảnh phóng to
        imageUri?.let { imageView.setImageURI(it) }
//        imageUri?.let { uri ->
//            imageView.setImageURI(uri)
//            setupCanvas()
//        }

        // Chức năng "Chỉnh sửa"
        view.findViewById<Button>(R.id.cropButton).setOnClickListener {
            cropImage()
        }

        // Chức năng "Xoay"
//        view.findViewById<Button>(R.id.rotateButton).setOnClickListener {
//            imageView.rotation = imageView.rotation + 90f
//        }

        // Chức năng "Bộ lọc"
        view.findViewById<Button>(R.id.filterButton).setOnClickListener {
            // Open the color picker dialog
            ColorPickerDialogBuilder
                .with(requireContext())
                .setTitle("Chọn màu") // Set title for the dialog
                .initialColor(Color.RED) // Starting color for the tint effect
                .wheelType(com.flask.colorpicker.ColorPickerView.WHEEL_TYPE.FLOWER) // Set wheel type for color selection
                .density(12) // Adjust density as desired
                .setOnColorSelectedListener { selectedColor ->
                    // Optional: Can handle real-time selection here if needed
                }
                .setPositiveButton("OK") { dialog, selectedColor, _ ->
                    // Apply the selected color as a filter on the imageView
                    imageView.setColorFilter(selectedColor, android.graphics.PorterDuff.Mode.MULTIPLY)
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    // Close the dialog without applying any changes
                    dialog.dismiss()
                }
                .build()
                .show()
        }



        // Chức năng "Ghi chữ"
        view.findViewById<Button>(R.id.textButton).setOnClickListener {
//            addTextToImage()
        }

        // Chức năng "Vẽ"
        view.findViewById<Button>(R.id.drawButton).setOnClickListener {
            if (isDrawing) {
                // If already drawing, disable it
                isDrawing = false
                imageView.setOnTouchListener(null) // Remove touch listener
            } else {
                // Enable drawing
                enableDrawing()
            }
        }

        // Chức năng "Lưu"
        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveEditedImage()
        }

        // Chức năng "Xoá"
        view.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            deleteImage()
        }

    }



    private fun cropImage() {
        imageUri?.let { uri ->
            val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
            val cropIntent = UCrop.of(uri, destinationUri)
                .withAspectRatio(1f, 1f)
                .getIntent(requireContext())
            startActivityForResult(cropIntent, CROP_IMAGE_REQUEST_CODE)
        }
    }

//    private fun addTextToImage() {
//        val editText = EditText(requireContext())
//        editText.hint = "Nhập văn bản"
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Ghi chữ lên hình")
//            .setView(editText)
//            .setPositiveButton("OK") { _, _ ->
//                text = editText.text.toString() // Store the text
//                if (text.isNotEmpty()) {
//                    // Reset the initial position
//                    textX = 100f
//                    textY = 100f
//
//                    // Draw the initial text on the canvas
//                    updateCanvasWithText()
//
//                    // Enable movement of the text
//                    enableTextMovement()
//                }
//            }
//            .setNegativeButton("Hủy", null)
//            .show()
//    }
//
//
//    private fun enableTextMovement() {
//        imageView.setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    // Check if the touch is within the bounds of the text
//                    if (event.x >= textX && event.x <= textX + drawPaint.measureText(text) &&
//                        event.y >= textY - drawPaint.textSize && event.y <= textY) {
//                        isDragging = true
//                        lastTouchX = event.x
//                        lastTouchY = event.y
//                    }
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    if (isDragging) {
//                        val dx = event.x - lastTouchX
//                        val dy = event.y - lastTouchY
//                        textX += dx
//                        textY += dy
//                        lastTouchX = event.x
//                        lastTouchY = event.y
//                        updateCanvasWithText() // Redraw text at new position
//                    }
//                }
//                MotionEvent.ACTION_UP -> {
//                    isDragging = false
//                }
//            }
//            true
//        }
//    }
//
//    private fun updateCanvasWithText() {
//        // Clear the combinedBitmap and redraw the image and text
//        combinedBitmap.eraseColor(Color.TRANSPARENT) // Clear the bitmap
//        drawCanvas.drawBitmap(originalBitmap, 0f, 0f, null) // Draw the original image
//        drawCanvas.drawText(text, textX, textY, drawPaint) // Draw the text at new position
//        imageView.setImageBitmap(combinedBitmap) // Update the ImageView
//        imageView.invalidate() // Refresh the view
//    }



    private fun enableDrawing() {
        imageUri?.let { uri ->
            // Load the original bitmap from the image URI
            originalBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

            // Create a mutable bitmap to combine drawings with the original image
            combinedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

            // Create a canvas with the combined bitmap
            drawCanvas = Canvas(combinedBitmap)

            // Initialize the paint object for drawing
            drawPaint = Paint().apply {
                color = Color.RED // Set the initial drawing color
                strokeWidth = 10f // Set stroke width
                style = Paint.Style.STROKE // Choose stroke style
                isAntiAlias = true // Smooth the edges
            }

            // Set the combined bitmap to the imageView
            imageView.setImageBitmap(combinedBitmap)

            // Set touch listener to the imageView for drawing
            imageView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Start drawing
                        isDrawing = true
                        drawCanvas.drawCircle(event.x, event.y, drawPaint.strokeWidth / 2, drawPaint)
                        imageView.invalidate() // Refresh the view
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Draw while moving
                        if (isDrawing) {
                            drawCanvas.drawCircle(event.x, event.y, drawPaint.strokeWidth / 2, drawPaint)
                            imageView.invalidate() // Refresh the view
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        // Stop drawing
                        isDrawing = false
                    }
                }
                true // Indicate that the touch event was handled
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                imageView.setImageURI(it)
            }
        }
    }

    private fun saveEditedImage() {
        // Tạo một bitmap từ imageView với các sửa đổi
        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.draw(canvas) // Vẽ nội dung của ImageView lên canvas

        // Lưu bitmap vào một tệp trong thư mục cache
        val file = File(requireContext().cacheDir, "edited_image_${System.currentTimeMillis()}.png")
        try {
            val outputStream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // Nén bitmap thành PNG
            outputStream.flush()
            outputStream.close()

            // Thông báo cho Activity chính về URI mới và đóng dialog
            onImageEditedListener?.onImageAdded(Uri.fromFile(file))
            dismiss()

        } catch (e: Exception) {
            e.printStackTrace()
            AlertDialog.Builder(requireContext())
                .setTitle("Lỗi")
                .setMessage("Không thể lưu hình ảnh.")
                .setPositiveButton("OK", null)
                .show()
        }
    }




    private fun deleteImage() {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xoá")
            .setMessage("Bạn có chắc chắn muốn xoá hình ảnh này không?")
            .setPositiveButton("Có") { _, _ ->
                // Call the listener's onImageDeleted method with the current image URI
                imageUri?.let {
                    onImageEditedListener?.onImageDeleted(it)
                }
                dismiss() // Close the dialog after deletion
            }
            .setNegativeButton("Không", null)
            .show()
    }


}
