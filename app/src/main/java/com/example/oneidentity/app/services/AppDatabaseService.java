package com.example.oneidentity.app.services;

import android.content.Context;
import android.util.Log;

import androidx.paging.LoadType;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Transaction;

import com.example.oneidentity.app.models.User;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public class AppDatabaseService {

    private final AppDatabase db;
    private static AppDatabaseService database;

    public AppDatabaseService(Context context)
    {
        db = Room.databaseBuilder(context,
                AppDatabase.class, "database-name").allowMainThreadQueries().build();
    }

    public static AppDatabaseService getInstance(Context context)
    {
        if ( database == null )
            database = new AppDatabaseService(context);
        return database;
    }

    public UserDao getUserDao()
    {
        return db.userDao();
    }

    @androidx.room.Database(entities = {User.class}, version = 1)
    public abstract static class AppDatabase extends RoomDatabase {
        public abstract UserDao userDao();
    }

    @Dao
    public static abstract class UserDao
    {
        @Query("SELECT * FROM user")
        public abstract List<User> getAll();

        @Query("SELECT * FROM user LIMIT :size OFFSET :offset")
        public abstract ListenableFuture<List<User>> getSomeUsers(int offset, int size );

        @Query("SELECT * FROM user LIMIT :size OFFSET :offset")
        public abstract List<User> getSomeUsersNow(int offset, int size );

        @Query("SELECT * FROM user LIMIT :size OFFSET :page * :size")
        public abstract PagingSource<Integer, User> getPagedUsers(int page, int size);

        @Query("SELECT * FROM user WHERE id == :id")
        public abstract User getUser( String id );

        @Transaction
        public PagingSource<Integer, User> getPagedUsers(User user, int size)
        {
            User dUser = getUser(user.getId());
            return getPagedUsers(dUser.getUid(), size);
        }

        @Transaction
        public List<User> getUsers(int page, int size)
        {
            return getSomeUsersNow( size * page, size );
        }

        @Transaction
        public int getCount(User user)
        {
            User dUser = getUser(user.getId());
            return getCount(dUser.getUid());
        }

        @Transaction
        public int getPage(User user, int size)
        {
            int count = getCount(user.getUid());
            return count % size != 0 ? (count / size) + 1 : count / size;
        }

        @Transaction
        public int addUsers(List<User> user, LoadType type)
        {
            Log.d("paging_user", "Adding Users " + type );
            int count = 0;
            if (type == LoadType.REFRESH)
                count = clearAll();
            insertAll(user);
            Log.d("paging_user", "Added Users " + getAll() );
            return count;
        }

        @Query("SELECT COUNT(uid) FROM user WHERE uid <= :uid")
        public abstract int getCount(int uid);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        public abstract void insertAll(List<User> users);

        @Query("DELETE FROM user")
        public abstract int clearAll();
    }
}
