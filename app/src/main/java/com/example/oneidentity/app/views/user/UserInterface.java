package com.example.oneidentity.app.views.user;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.ListenableFutureRemoteMediator;
import androidx.paging.LoadType;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;
import androidx.paging.RemoteMediator;

import com.example.oneidentity.app.models.User;
import com.example.oneidentity.app.models.UserPage;
import com.example.oneidentity.app.services.APIService;
import com.example.oneidentity.app.services.AppDatabaseService;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import kotlin.coroutines.Continuation;

public class UserInterface {

    public static ListenableFutureRemoteMediator<Integer, User> getUserMediator(APIService instance, AppDatabaseService database, Executor bgExecutor) {
        return new UserRemoteMediator(instance, database, bgExecutor);
    }

    public static PagingSource<Integer, User> getUserSource(AppDatabaseService database, Executor bgExecutor) {
        return new UserPagingSource(database, bgExecutor);
    }

    public static class UserPagingSource extends PagingSource<Integer, User> {
        @NonNull
        private AppDatabaseService.UserDao userDao;
        @NonNull
        private Executor bgExecutor;

        UserPagingSource(
                @NonNull AppDatabaseService database,
                @NonNull Executor bgExecutor) {
            Log.d("paging_user", "Creating the source");
            this.userDao = database.getUserDao();
            this.bgExecutor = bgExecutor;
        }

        @Nullable
        @Override
        public LoadResult<Integer, User> load(@NonNull LoadParams<Integer> loadParams, @NonNull Continuation<? super LoadResult<Integer, User>> continuation) {
            Log.d("paging_user", "Loading Users from DB " + loadParams.getKey());

            Integer nextPageNumber = loadParams.getKey();
            if (nextPageNumber == null) {
                nextPageNumber = 0;
            }

            List<User> users = userDao.getUsers(nextPageNumber, loadParams.getLoadSize());
            LoadResult.Page<Integer, User> page = new LoadResult.Page<>(users,
                    null, // Only paging forward.
                    users.isEmpty() ? null : nextPageNumber + 1,
                    LoadResult.Page.COUNT_UNDEFINED,
                    LoadResult.Page.COUNT_UNDEFINED);
            Log.d("paging_user", "Loading Users from DB " + page);
            return page;
        }

        @Nullable
        @Override
        public Integer getRefreshKey(@NotNull PagingState<Integer, User> state) {
            Integer anchorPosition = state.getAnchorPosition();
            if (anchorPosition == null) {
                return null;
            }

            LoadResult.Page<Integer, User> anchorPage = state.closestPageToPosition(anchorPosition);
            if (anchorPage == null) {
                return null;
            }

            Integer prevKey = anchorPage.getPrevKey();
            if (prevKey != null) {
                return prevKey + 1;
            }

            Integer nextKey = anchorPage.getNextKey();
            if (nextKey != null) {
                return nextKey - 1;
            }

            return null;
        }
    }

    public static class UserRemoteMediator extends ListenableFutureRemoteMediator<Integer, User> {
        @NonNull
        private AppDatabaseService.UserDao userDao;
        @NonNull
        private APIService backend;
        @NonNull
        private Executor bgExecutor;

        public UserRemoteMediator(
                APIService backend,
                AppDatabaseService database,
                Executor executorService) {
            this.backend = backend;
            this.userDao = database.getUserDao();
            this.bgExecutor = executorService;
        }

        @NonNull
        @Override
        public ListenableFuture<MediatorResult> loadFuture(@NonNull LoadType loadType, @NonNull PagingState<Integer, User> pagingState) {
            User lastItem = null;
            switch (loadType) {
                case REFRESH:
                    break;
                case PREPEND:
                    return Futures.immediateFuture(new MediatorResult.Success(true));
                case APPEND:
                    lastItem = pagingState.lastItemOrNull();

                    if (lastItem == null) {
                        return Futures.immediateFuture(new MediatorResult.Success(true));
                    }

                    break;
            }

            Integer page = 0;
            if ( pagingState.getPages().size() != 0 )
                page = pagingState.getPages().get(pagingState.getPages().size() - 1).getNextKey();

            if (page == null)
                return Futures.immediateFuture(new MediatorResult.Success(true));

            ListenableFuture<MediatorResult.Success> networkResult = Futures.transform(backend.getService().listUsers(page, pagingState.getConfig().pageSize),
                    response -> {
                        userDao.addUsers(response.getUsers(), loadType);
                        Log.d("paging_user", "Remote Mediator Loaded database " + response.isEmpty());
                        return new MediatorResult.Success(response.isEmpty());
                    }, bgExecutor);

            ListenableFuture<MediatorResult> ioCatchingNetworkResult =
                    Futures.catching(networkResult, IOException.class, MediatorResult.Error::new, bgExecutor);

            return ioCatchingNetworkResult;
        }

    }
}

