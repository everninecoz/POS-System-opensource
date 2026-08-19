package com.posopensrc.core.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.posopensrc.core.utils.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    fun backupDatabase(): Uri? {
        return try {
            val dbFile = context.getDatabasePath(AppConstants.DATABASE_NAME)
            val dbWalFile = File(dbFile.path + "-wal")
            val dbShmFile = File(dbFile.path + "-shm")

            val backupDir = File(context.cacheDir, "backups")
            backupDir.mkdirs()

            val timestamp = dateFormat.format(Date())
            val backupFile = File(backupDir, "pos_backup_$timestamp.zip")

            ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
                // Add main database file
                FileInputStream(dbFile).use { fis ->
                    val entry = ZipEntry("database/${dbFile.name}")
                    zos.putNextEntry(entry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }

                // Add WAL file if exists
                if (dbWalFile.exists()) {
                    FileInputStream(dbWalFile).use { fis ->
                        val entry = ZipEntry("database/${dbWalFile.name}")
                        zos.putNextEntry(entry)
                        fis.copyTo(zos)
                        zos.closeEntry()
                    }
                }

                // Add SHM file if exists
                if (dbShmFile.exists()) {
                    FileInputStream(dbShmFile).use { fis ->
                        val entry = ZipEntry("database/${dbShmFile.name}")
                        zos.putNextEntry(entry)
                        fis.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }

            shareBackupFile(backupFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun restoreDatabase(uri: Uri): Result<Unit> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("File tidak dapat dibuka"))

            val dbFile = context.getDatabasePath(AppConstants.DATABASE_NAME)

            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val fileName = File(entry.name).name

                    when {
                        fileName == dbFile.name -> {
                            FileOutputStream(dbFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        fileName.endsWith("-wal") -> {
                            val walFile = File(dbFile.path + "-wal")
                            FileOutputStream(walFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        fileName.endsWith("-shm") -> {
                            val shmFile = File(dbFile.path + "-shm")
                            FileOutputStream(shmFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                    }

                    entry = zis.nextEntry
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getBackupFiles(): List<File> {
        val backupDir = File(context.cacheDir, "backups")
        return if (backupDir.exists()) {
            backupDir.listFiles()?.filter { it.extension == "zip" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteBackup(file: File): Boolean {
        return file.delete()
    }

    private fun shareBackupFile(file: File): Uri {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Backup POS Open Source")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Backup Database"))
        return uri
    }
}
