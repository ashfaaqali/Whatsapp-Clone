package com.ali.whatsappplus.common.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

class Utils {
    companion object {
        fun getRealPath(context: Context?, fileUri: Uri?): File {
            Log.d("", "getRealPath: " + fileUri?.path)
            val realPath: String = if (fileUri?.let { isGoogleDrive(it) } == true) {
                return saveDriveFile(context!!, fileUri)
            } else if (Build.VERSION.SDK_INT < 30) {
                fileUri?.let { getRealPathFromURI(context!!, it) }!!
            } else {
                //(Build.VERSION.SDK_INT == 30)
                fileUri?.let { getRealPathFromN(context, it) }!!
            }

            return File(realPath)
        }

        private fun getRealPathFromURI(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id != null) {
                        if (id.startsWith("raw:")) {
                            return id.substring(4)
                        }
                        if (id.startsWith("msf:")) {
                            id = id.substring(4)
                        }
                    }
                    val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix),
                            java.lang.Long.valueOf(id!!)
                        )
                        try {
                            val path: String = getDataColumn(context, contentUri, null, null)!!
                            if (path != null) {
                                return path
                            }
                        } catch (_: Exception) {
                        }
                    }

                    // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                    val fileName = getFileName(context, uri)
                    val cacheDir = getDocumentCacheDir(context)
                    val file = generateFileName(fileName, cacheDir)
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        saveFileFromUri(context, uri, destinationPath)
                    }
                    return destinationPath
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }

                        "video" -> {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }

                        "audio" -> {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        @RequiresApi(Build.VERSION_CODES.R)
        private fun getRealPathFromN(context: Context?, uri: Uri): String? {
            val returnUri = uri
            val returnCursor = context?.contentResolver?.query(
                returnUri,
                null, null, null, null
            )
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
            returnCursor?.moveToFirst()
            val name = returnCursor?.getString(nameIndex!!)
            returnCursor?.getLong(sizeIndex!!)
            val file = File(context?.filesDir, name)
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream!!.available()

                //int bufferSize = 1024;
                val bufferSize = min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                Log.e("File Size", "Size " + file.length())
                inputStream.close()
                outputStream.close()
                returnCursor?.close()
                Log.e("File Path", "Path " + file.path)
                Log.e("File Size", "Size " + file.length())
            } catch (e: Exception) {
                Log.e("Exception", e.message!!)
            }
            return file.path
        }

        fun isGoogleDrive(uri: Uri): Boolean {
            return uri.authority!!.contains("com.google.android.apps.docs.storage")
        }

        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        fun saveDriveFile(context: Context?, uri: Uri?): File {
            var file: File? = null
            try {
                if (uri != null) {
                    file = File(context?.cacheDir, getFileName(context, uri))
                    val inputStream = context?.contentResolver?.openInputStream(uri)
                    try {
                        val output: OutputStream = FileOutputStream(file)
                        try {
                            val buffer = ByteArray(4 * 1024) // or other buffer size
                            var read: Int
                            while (inputStream!!.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        } finally {
                            output.close()
                        }
                    } finally {
                        inputStream!!.close()
                        //Upload Bytes.
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "File Uri is null", Toast.LENGTH_LONG).show()
            }
            return file!!
        }

        fun getFileName(context: Context?, uri: Uri): String? {
            val mimeType = context?.contentResolver?.getType(uri)
            var filename: String? = null
            if (mimeType == null && context != null) {
                val path = getPath(context, uri)
                filename = if (path == null) {
                    getName(uri.toString())
                } else {
                    val file = File(path)
                    file.name
                }
            } else {
                val returnCursor = context?.contentResolver?.query(
                    uri, null,
                    null, null, null
                )
                if (returnCursor != null) {
                    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    returnCursor.moveToFirst()
                    filename = returnCursor.getString(nameIndex)
                    returnCursor.close()
                }
            }
            return filename
        }

        fun getPath(context: Context?, uri: Uri): String? {
            val absolutePath = getImagePathFromUri(context, uri)
            return absolutePath ?: uri.toString()
        }

        fun getName(filename: String?): String? {
            if (filename == null) {
                return null
            }
            val index = filename.lastIndexOf('/')
            return filename.substring(index + 1)
        }

        fun getImagePathFromUri(context: Context?, aUri: Uri?): String? {
            var imagePath: String? = null
            if (aUri == null) {
                return imagePath
            }
            if (DocumentsContract.isDocumentUri(context, aUri)) {
                val documentId = DocumentsContract.getDocumentId(aUri)
                if ("com.android.providers.media.documents" == aUri.authority) {
                    val id = DocumentsContract.getDocumentId(aUri)
                    if (id != null && id.startsWith("raw:")) {
                        return id.substring(4)
                    }
                    val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix),
                            java.lang.Long.valueOf(id!!)
                        )
                        try {
                            val path = getDataColumn(context!!, contentUri, null, null)
                            if (path != null) {
                                return path
                            }
                        } catch (e: java.lang.Exception) {
                        }
                    }

                    // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                    val fileName: String = getFileName(context!!, aUri)!!
                    val cacheDir: File = getDocumentCacheDir(context)
                    val file: File = generateFileName(fileName, cacheDir)!!
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        saveFileFromUri(context, aUri, destinationPath)
                    }
                    imagePath = destinationPath
                } else if ("com.android.providers.downloads.documents" == aUri.authority) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(documentId)
                    )
                    imagePath = getImagePath(contentUri, null!!, context!!)
                }
            } else if ("content".equals(aUri.scheme, ignoreCase = true)) {
                imagePath = getImagePath(aUri, null!!, context!!)
            } else if ("file".equals(aUri.scheme, ignoreCase = true)) {
                imagePath = aUri.path
            }
            return imagePath
        }

        @SuppressLint("Range")
        private fun getImagePath(aUri: Uri, aSelection: String, context: Context): String? {
            try {
                var path: String? = null
                val cursor = context.contentResolver.query(aUri, null, aSelection, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    }
                    cursor.close()
                }
                return path
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun getDataColumn(
            context: Context,
            uri: Uri?,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        fun generateFileName(name: String?, directory: File?): File? {
            var name = name ?: return null
            var file = File(directory, name)
            if (file.exists()) {
                var fileName = name
                var extension = ""
                val dotIndex = name.lastIndexOf('.')
                if (dotIndex > 0) {
                    fileName = name.substring(0, dotIndex)
                    extension = name.substring(dotIndex)
                }
                var index = 0
                while (file.exists()) {
                    index++
                    name = "$fileName($index)$extension"
                    file = File(directory, name)
                }
            }
            try {
                if (!file.createNewFile()) {
                    return null
                }
            } catch (e: IOException) {
//            Log.w(TAG, e)
                return null
            }
            return file
        }

        fun getDocumentCacheDir(context: Context): File {
            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

        private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
            var inputStream: InputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
                val buf = ByteArray(1024)
                inputStream!!.read(buf)
                do {
                    bos.write(buf)
                } while (inputStream.read(buf) != -1)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                    bos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}