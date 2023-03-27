package org.hyperskill.photoeditor

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.google.android.material.slider.Slider
import kotlinx.coroutines.*
import java.security.Permission
import kotlin.math.nextTowards
import kotlin.math.pow
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var currentImage: ImageView
    private lateinit var button: Button
    private lateinit var saveButton: Button
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val MEDIA_REQUEST_CODE = 0
    private lateinit var sliderBright: Slider
    private lateinit var sliderContrast: Slider
    private lateinit var myImage: Bitmap
    private lateinit var sliderSaturation: Slider
    private lateinit var sliderGamma: Slider
    private var lastJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        currentImage.setImageBitmap(createBitmap())
        // slider.value = 0.0F
        try {

            activityResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val photoUri = result.data?.data ?: return@registerForActivityResult
                    currentImage.setImageURI(photoUri)
                    myImage = currentImage.drawable.toBitmap()
                } else {
                    println("no image")
                }
            }
            myImage = currentImage.drawable.toBitmap()
            //brightImage = myImage
            //contrastImage = myImage

        } catch (e: Exception) {
            Log.d("Error", e.message)
        }


        button.setOnClickListener() {
            try {
                val gallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(Intent(gallery))
            } catch (e: Exception) {
                println(e.message)
            }
        }
        saveButton.setOnClickListener() {
                try {
                    when {
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED  -> {
                            saveImage()
                            Toast.makeText(this, "Image is Saved in Gallery", Toast.LENGTH_SHORT).show()
                                }
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                            // The permission was denied some time before. Show the rationale!
                            AlertDialog.Builder(this)
                                .setTitle("Permission Required")
                                .setMessage("This app needs permission to access Storage to save Image in gallery.")
                                .setPositiveButton("Grant") { _, _ ->
                                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                        else -> {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0)

                        }
                    }
                }catch(e:Exception){
                    println(e.message)}}

        sliderBright.addOnChangeListener { slider, value, fromUser ->

           // lastJob = GlobalScope.launch(Dispatchers.Default) {
                //GlobalScope.launch(Dispatchers.Main) {
                try {
                    applyFilters(
                        value.toInt(),
                        sliderContrast.value.toInt(),
                        sliderSaturation.value.toInt(),
                        sliderGamma.value.toDouble()
                    )
                    //  println("applyFilters( ${value.toInt()}, ${sliderContrast.value.toInt()}")
                } catch (e: Exception) {
                    println(e.message)
                }
                //}
            }


        sliderContrast.addOnChangeListener { _, value, _ ->
           // GlobalScope.launch(Dispatchers.Main) {

                try {
                    applyFilters(
                        sliderBright.value.toInt(),
                        value.toInt(),
                        sliderSaturation.value.toInt(),
                        sliderGamma.value.toDouble()
                    )
                    //   println("applyFilters( ${slider.value.toInt()}, ${value.toInt()}")
                } catch (e: Exception) {
                    println(e.message)
                }
            //}
        }



        sliderSaturation.addOnChangeListener { _, value, _ ->
            //GlobalScope.launch(Dispatchers.Main) {
                applyFilters(
                    sliderBright.value.toInt(),
                    sliderContrast.value.toInt(),
                    value.toInt(),
                    sliderGamma.value.toDouble()
                )


        }
        sliderGamma.addOnChangeListener { slider, value, fromUser ->
                applyFilters(
                    sliderBright.value.toInt(),
                    sliderContrast.value.toInt(),
                    sliderSaturation.value.toInt(),
                    value.toDouble()
                )
                // println("applyFiltersGamma( ${value.toDouble()}, ${sliderSaturation.value.toInt()}")

            }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MEDIA_REQUEST_CODE -> { println("GRANT RESULTS = ${grantResults[0]}")
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage()
                    Toast.makeText(
                                this,
                                "Image Saved",
                                Toast.LENGTH_SHORT
                            ).show()

                } else {
                    // no permission, block access to this feature
                    Toast.makeText(
                                this,
                                "Image Can't be Saved in Gallery without granting permission",
                                Toast.LENGTH_SHORT
                            ).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

     fun applyFilters(brightnessValue: Int, contrastValue: Int, saturationValue: Int, gammavalue: Double) {
         lastJob?.cancel()
         lastJob = GlobalScope.launch(Dispatchers.Default) {
             //  the execution inside this block is already asynchronous as you can see by the print below

             //  I/System.out: onSliderChanges job making calculations running on thread DefaultDispatcher-worker-1
             println("onSliderChanges " + "job making calculations running on thread ${Thread.currentThread().name}")
             val bitmap = myImage.copy(myImage.config, true) ?: return@launch
             println("$brightnessValue, $contrastValue, $saturationValue, $gammavalue")

             // if the current image is null, we have nothing to do with it
             //----val bitmap = currentImage.drawable.toBitmap()?: return@launch

             // if you need to make some computations and wait for the result, you can use the async block
             // it will schedule a new coroutine task and return a Deferred object that will have the
             // returned value
             val brightenCopyDeferred: Deferred<Bitmap> = this.async {
                 /* invoke your computation that returns a value */
                 /* making some brightness calculations */
                 val bitmapTemp: Bitmap
                 if (brightnessValue != 0) {
                     println("Brightness Filer Applied")
                     bitmapTemp = applyBrightness(bitmap, brightnessValue)

                 } else {
                     bitmapTemp = bitmap
                     println("Brightness Filer not Applied")
                 }
                 return@async bitmapTemp
             }
             // here we wait for the result
             val brightenCopy: Bitmap = brightenCopyDeferred.await()

             val contrastedCopyDeferred: Deferred<Bitmap> = this.async {
                 /* invoke your computation that returns a value */
                 /* making some brightness calculations */
                 /* do more calculations */
                 //calculate average brightness
                 val avgBrightness = calcAvgBrightness(brightenCopy)
                    val contrastedCopyTemp :Bitmap
                 //calculate contrast
                 if (contrastValue != 0) {
                     println("Contrast Filer Applied")
                     contrastedCopyTemp = applyContrast(brightenCopy, avgBrightness, contrastValue)
                 } else {
                     contrastedCopyTemp = brightenCopy
                     println("Contrast Filer not Applied")
                 }
                 /* check if job was canceled */

                 ensureActive()
                 return@async contrastedCopyTemp


             }
             // here we wait for the result
             val contrastedCopy: Bitmap = contrastedCopyDeferred.await()

             val saturatedCopyDeferred: Deferred<Bitmap> = this.async {
                 /* invoke your computation that returns a value */
                 /* making some brightness calculations */
                 /* do more calculations */
                 //calculate average brightness
                // val avgBrightness = calcAvgBrightness(brightenCopy)
                 val saturatedCopyTemp :Bitmap
                 //calculate contrast
                 if (saturationValue != 0) {
                     println("Saturation Filer Applied")
                     saturatedCopyTemp = applySaturation(contrastedCopy, saturationValue)
                 } else {
                     saturatedCopyTemp = contrastedCopy
                     println("Saturation Filer not Applied")
                 }
                 /* check if job was canceled */
                 ensureActive()
                 return@async saturatedCopyTemp
             }
             // here we wait for the result
             val saturatedCopy: Bitmap = saturatedCopyDeferred.await()


             val gammaCopyDeferred: Deferred<Bitmap> = this.async {
                 /* invoke your computation that returns a value */
                 /* making some brightness calculations */
                 /* do more calculations */
                 //calculate average brightness
                 // val avgBrightness = calcAvgBrightness(brightenCopy)
                 val gammaCopyTemp :Bitmap
                 //calculate contrast

                 if (gammavalue == 1.0) {
                     gammaCopyTemp = saturatedCopy
                     println("Gamma Filer not Applied")
                 } else {
                     gammaCopyTemp = applyGamma(saturatedCopy, gammavalue)
                     println("Gamma Filer Applied")
                 }

                 /* check if job was canceled */
                 ensureActive()
                 return@async gammaCopyTemp
             }
             // here we wait for the result
             val gammaCopy: Bitmap = gammaCopyDeferred.await()
             // since you are already on a worker thread, you don't have to use the async block
             //val contrastedCopy:Bitmap/* invoke computations for contrast filter */


             // to update view, we need to change the thread context to the main thread
             runOnUiThread {
                 // here we are already on the main thread, as you can see on the println below
                 //  I/System.out: onSliderChanges job updating view running on thread main
                 println("onSliderChanges " + "job updating view running on thread ${Thread.currentThread().name}")
                 currentImage.setImageBitmap(gammaCopy)
             }
         }


             // var bitmap = myImage

            // withContext(Dispatchers.Default) {
                 //apply brightness
            // val deferredOne: Deferred<Bitmap> = async {


             //}

            // val deferredTwo = async {




             //currentImage.setImageBitmap(bitmap)
         }



    private suspend fun applyContrast(bitmap: Bitmap, avgBright: Int, contrastValue: Int): Bitmap {
        return withContext(Dispatchers.Default) {

        var bitmap = bitmap
        val contrastDouble: Double = contrastValue.toDouble()
        val alpha: Double = (255 + contrastDouble) / (255 - contrastDouble)
        Log.d("Contrast", "contrastDouble: $contrastDouble; alpha: $alpha")

        for (x in 0 until bitmap.width){
            for (y in 0 until bitmap.height){

                val color = bitmap.getPixel(x,y)

                var R = (alpha * (color.red - avgBright) + avgBright).toInt()
                if (R > 255) { R = 255 }
                else if (R < 0) { R = 0}

                var G = (alpha * (color.green - avgBright) + avgBright).toInt()
                if (G > 255) { G = 255 }
                else if (G < 0) { G = 0}

                var B = (alpha * (color.blue - avgBright) + avgBright).toInt()
                if (B > 255) { B = 255 }
                else if (B < 0) { B = 0}

                bitmap.setPixel(x, y, Color.rgb(R, G, B))
            }
        }

        return@withContext bitmap
        }
    }
    private  fun calcAvgBrightness(bitmap: Bitmap): Int {
       // return withContext(Dispatchers.Default) {

            var accumulator: Long = 0

            for (i in 0 until bitmap.width) {
                for (j in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(i, j)
                    accumulator += pixel.red + pixel.green + pixel.blue
                }
            }

            //calculate average and return
            return (accumulator / (bitmap.width * bitmap.height * 3)).toInt()
        }


    private suspend fun applyBrightness(bitmap: Bitmap, value: Int): Bitmap {
       return withContext(Dispatchers.Default) {

           var newBitmap = bitmap

        for (x in 0 until newBitmap.width){
            for (y in 0 until newBitmap.height){

                val color = newBitmap.getPixel(x,y)

                var R = color.red + value
                if (R > 255) { R = 255 }
                else if (R < 0) { R = 0}

                var G = color.green + value
                if (G > 255) { G = 255 }
                else if (G < 0) { G = 0}

                var B = color.blue + value
                if (B > 255) { B = 255 }
                else if (B < 0) { B = 0}

                newBitmap.setPixel(x, y, Color.rgb(R, G, B))
            }
        }

        return@withContext newBitmap
       }

    }
   private suspend fun applySaturation(BBitmap: Bitmap, saturationValue: Int): Bitmap {
       return withContext(Dispatchers.Default) {

       var bitmap = BBitmap

       for(x in 0 until bitmap.width) {
           for(y in 0 until bitmap.height) {
               val pixelColor = bitmap.getPixel(x,y)
               var red = pixelColor.red
               var green = pixelColor.green
               var blue = pixelColor.blue
            //   println(" Saturation RGB= $red , $green, $blue")

               val rgbAvg = (red + green + blue) / 3
               val alpha = (255.0 + saturationValue) / (255.0 - saturationValue)
               red = (alpha * (red - rgbAvg) + rgbAvg).toInt()
               if (red > 255) { red = 255 }
               else if (red < 0) { red = 0}

               // red = (alpha * (red - rgbAvg) + rgbAvg).toInt().coerceIn(0..255)
               green = (alpha * (green - rgbAvg) + rgbAvg).toInt()
               if (green > 255) { green = 255 }
               else if (green < 0) { green = 0}


               blue = (alpha * (blue - rgbAvg) + rgbAvg).toInt()
               if (blue > 255) { blue = 255 }
               else if (blue < 0) { blue = 0}

               //   println("new RGB= $red , $green, $blue")
               bitmap.setPixel(x, y, Color.rgb(red, green, blue))

           }
       }
       return@withContext bitmap
       }
   }
    private suspend fun applyGamma(bitmap: Bitmap, gamma: Double): Bitmap {
        return withContext(Dispatchers.Default) {

            var newBitmap = bitmap
            val gammavalue: Double = String.format("%1.1f", gamma).toDouble()

            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    val pixelColor = bitmap.getPixel(x, y)
                    var red = pixelColor.red
                    var green = pixelColor.green
                    var blue = pixelColor.blue
                    // println(gammavalue)
                    //  println(" GAMMA RGB= $red , $green, $blue")
                    red = (255 * (red.toDouble() / 255).pow(gammavalue)).toInt().coerceIn(0..255)
                    //  (255 * (color.toDouble() / 255).pow(gamma)).toInt()
                    green =
                        (255 * (green.toDouble() / 255).pow(gammavalue)).toInt().coerceIn(0..255)
                    blue = (255 * (blue.toDouble() / 255).pow(gammavalue)).toInt().coerceIn(0..255)

                    bitmap.setPixel(x, y, Color.rgb(red, green, blue))

                }
            }
            return@withContext newBitmap
        }
    }

    private fun bindViews() {
        currentImage = findViewById(R.id.ivPhoto)
        button = findViewById(R.id.btnGallery)
        sliderBright = findViewById(R.id.slBrightness)
        sliderContrast = findViewById(R.id.slContrast)
        saveButton = findViewById(R.id.btnSave)
        sliderSaturation = findViewById(R.id.slSaturation)
        sliderGamma = findViewById(R.id.slGamma)
    }


    // do not change this function
    private fun createBitmap(): Bitmap {
        val width = 200
        val height = 100
        val pixels = IntArray(width * height)
        // get pixel array from source

        var R: Int
        var G: Int
        var B: Int
        var index: Int

        for (y in 0 until height) {
            for (x in 0 until width) {
                // get current index in 2D-matrix
                index = y * width + x
                // get color
                R = x % 100 + 40
                G = y % 100 + 80
                B = (x + y) % 100 + 120

                pixels[index] = Color.rgb(R, G, B)

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }



    private fun saveImage() {
        val bitmap: Bitmap = currentImage.drawable.toBitmap() /* image you want to save */
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.ImageColumns.WIDTH, bitmap.width)
        values.put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.height)

        val uri = this@MainActivity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        ) ?: return

        contentResolver.openOutputStream(uri).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }
}

