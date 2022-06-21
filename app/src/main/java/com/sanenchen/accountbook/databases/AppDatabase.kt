package com.sanenchen.accountbook.databases

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * JetpackRoom 数据库
 * @author sanenchen
 */

/**
 * 系统信息 Table
 */
@Entity
data class SettingInformation(
    @PrimaryKey(autoGenerate = true) val id : Int,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "isUsingBiometric") var isUsingBiometric: Boolean,
    @ColumnInfo(name = "allowScreenShot") var allowScreenShot: Boolean,
    @ColumnInfo(name = "directShowPassword") var directShowPassword: Boolean,
)

@Dao
interface SettingInformationDao {
    @Insert
    fun insertSettingInfo(SettingInfo: SettingInformation)

    @Query("select * from SettingInformation")
    fun queryAll(): List<SettingInformation>

    @Query("select * from SettingInformation")
    fun queryAllFlow(): Flow<List<SettingInformation>>

    @Update
    fun update(SettingInfo: SettingInformation)
}

/**
 * 密码组 Table
 */
@Entity
data class PasswordGroup(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo var groupName: String
)

@Dao
interface PasswordGroupDao {
    @Insert
    fun insertPasswordGroup(passwordGroup: PasswordGroup): Long

    @Query("select * from PasswordGroup")
    fun queryAll(): Flow<List<PasswordGroup>>

    @Query("delete from PasswordGroup where id=:id")
    fun dropGroup(id: Int)

    @Update()
    fun update(passwordData: PasswordGroup)
}

/**
 * 密码数据 Table
 */
@Entity
data class PasswordData(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo var title: String,
    @ColumnInfo var user: String,
    @ColumnInfo var password: String,
    @ColumnInfo var remark: String,
    @ColumnInfo var viewTimes: Int,
    @ColumnInfo var groupID: Int,
)

@Dao
interface PasswordDataDao{
    @Insert
    fun insertPasswordData(passwordData: PasswordData)

    @Query("select * from PasswordData")
    fun queryAll(): Flow<List<PasswordData>>

    @Query("select * from PasswordData where groupID=:id")
    fun queryWithGroup(id: Int): Flow<List<PasswordData>>

    @Query("select * from PasswordData where groupID=:id")
    fun queryWithGroupCommon(id: Int): List<PasswordData>

    @Update()
    fun update(passwordData: PasswordData)

    @Update()
    fun updateList(passwordData: List<PasswordData>)

    @Query("delete from PasswordData where id=:id")
    fun dropData(id: Long)
}

@Database(version = 2, entities = [SettingInformation::class, PasswordGroup::class, PasswordData::class])
abstract class AppDatabase : RoomDatabase(){
    abstract fun SettingInformationDao(): SettingInformationDao
    abstract fun PasswordGroupDao(): PasswordGroupDao
    abstract fun PasswordDataDao(): PasswordDataDao
    companion object {
        lateinit var database: AppDatabase
    }
}