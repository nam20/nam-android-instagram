package com.example.naminsta.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.example.naminsta.R
import com.example.naminsta.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // initial storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // add image upload event
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                // this is path to the selected image
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            }else{
                // exit the addPhotoActivity if you leave the album without selecting it
                finish()
            }
        }
    }
    fun contentUpload(){
        // Makd filename

        var timestamp =  SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "Image_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)


        // Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            // insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

            // insert uid of image
            contentDTO.uid = auth?.currentUser?.uid

            // insert userid
            contentDTO.userId = auth?.currentUser?.email

            // insert explain of content
            contentDTO.explain = addphoto_edit_display.text.toString()

            // insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }


        // Callback method
//        storageRef!!.putFile(photoUri!!)?.addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                var contentDTO = ContentDTO()
//
//                // insert downloadUrl of image
//                contentDTO.imageUrl = uri.toString()
//
//                // insert uid of image
//                contentDTO.uid = auth?.currentUser?.uid
//
//                // insert userid
//                contentDTO.userId = auth?.currentUser?.email
//
//                // insert explain of content
//                contentDTO.explain = addphoto_edit_display.text.toString()
//
//                // insert timestamp
//                contentDTO.timestamp = System.currentTimeMillis()
//
//                firestore?.collection("images")?.document()?.set(contentDTO)
//
//                setResult(Activity.RESULT_OK)
//
//                finish()
//
//            }
//        }
    }
}