package com.example.habbittrainer.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.habbittrainer.Habit
import com.example.habbittrainer.db.HabitEntry.DESCR_COL
import com.example.habbittrainer.db.HabitEntry.IMAGE_COL
import com.example.habbittrainer.db.HabitEntry.TABLE_NAME
import com.example.habbittrainer.db.HabitEntry.TITLE_COL
import com.example.habbittrainer.db.HabitEntry._ID
import java.io.ByteArrayOutputStream

class HabitDbTable(context: Context) {

    val dbHelper = HabitTrainerDb(context)
    private val TAG = HabitDbTable::class.java.simpleName

    fun store(habit: Habit): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        with(values) {
            put(TITLE_COL, habit.title)
            put(DESCR_COL, habit.desc)
            put(IMAGE_COL, toByteArray(habit.img))
        }
        val id = db.transaction {
            it.insert(TABLE_NAME, null, values)
        }
        Log.d(TAG, "Stored new habit to the DB $habit")
        return id
    }

    fun readAllHabits(): List<Habit> {
        val columns = arrayOf(_ID, TITLE_COL, DESCR_COL, IMAGE_COL)
        val order = "$_ID ASC"
        val db = dbHelper.readableDatabase
        val cursor = db.doQuery(TABLE_NAME, columns, orderBy = order)
        return parseHabitsFrom(cursor)
    }

    private fun parseHabitsFrom(cursor: Cursor): MutableList<Habit> {
        val habits = mutableListOf<Habit>()
        while (cursor.moveToNext()) {
            val title = cursor.getString(TITLE_COL)
            val desc = cursor.getString(DESCR_COL)
            val bitmap = cursor.getBitmap(IMAGE_COL)
            habits.add(Habit(title, desc, bitmap))
        }
        cursor.close()
        return habits
    }

    private fun toByteArray(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }
}

private fun Cursor.getString(columnName: String) = getString(getColumnIndex(columnName))

private fun Cursor.getBitmap(columnName: String): Bitmap {
    val byte = getBlob(getColumnIndex(columnName))
    return BitmapFactory.decodeByteArray(byte, 0, byte.size)
}

private fun SQLiteDatabase.doQuery(
    table: String, columns: Array<String>, selection: String? = null,
    selectionArgs: Array<String>? = null, groupBy: String? = null, having: String? = null,
    orderBy: String? = null,
): Cursor = query(table, columns, selection, selectionArgs, groupBy, having, orderBy)

private inline fun <T> SQLiteDatabase.transaction(f: (SQLiteDatabase) -> T): T {
    beginTransaction()
    val result = try {
        val returnValue = f(this)
        setTransactionSuccessful()
        returnValue
    } finally {
        endTransaction()
    }
    close()
    return result
}