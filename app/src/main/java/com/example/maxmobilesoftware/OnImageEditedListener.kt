package com.example.maxmobilesoftware

import android.net.Uri

interface OnImageEditedListener {
    fun onImageAdded(editedImageUri: Uri)
    fun onImageDeleted(deletedImageUri: Uri)

}