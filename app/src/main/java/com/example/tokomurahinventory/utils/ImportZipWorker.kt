package com.example.tokomurahinventory.utils

import android.content.Context
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ImportZipWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uriString = inputData.getString("uri") ?: return Result.failure()
        val uri = Uri.parse(uriString)
        val context = applicationContext

        // Perform the file extraction process
        val tempFile = readFileFromUri(context, uri)
        return if (tempFile?.exists() == true) {
            try {
                extractZipFile(tempFile)
                Result.success()
            } catch (e: IOException) {
                Result.failure()
            }
        } else {
            Result.failure()
        }
    }

    private fun readFileFromUri(context: Context, uri: Uri): File? {
        // Implement file reading from URI
        return null
    }

    private fun extractZipFile(zipFile: File) {
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))
        var zipEntry: ZipEntry? = zipInputStream.nextEntry
        while (zipEntry != null) {
            val outputFile = File(applicationContext.getDatabasePath(zipEntry.name).parent, zipEntry.name)
            if (zipEntry.isDirectory) {
                outputFile.mkdirs()
            } else {
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (zipInputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()
    }
}
