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
            Log.d("paging_user", "Refreshing the source " + state);
            // Try to find the page key of the closest page to anchorPosition, from
            // either the prevKey or the nextKey, but you need to handle nullability
            // here:
            //  * prevKey == null -> anchorPage is the first page.
            //  * nextKey == null -> anchorPage is the last page.
            //  * both prevKey and nextKey null -> anchorPage is the initial page, so
            //    just return null.
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

//    public static class UserPagingSource extends ListenableFuturePagingSource<Integer, User> {
//        @NonNull
//        private AppDatabaseService.UserDao userDao;
//        @NonNull
//        private Executor bgExecutor;
//
//        UserPagingSource(
//                @NonNull AppDatabaseService.UserDao userDao,
//                @NonNull Executor bgExecutor) {
//            this.userDao = userDao;
//            this.bgExecutor = bgExecutor;
//        }
//
//        @NotNull
//        @Override
//        public ListenableFuture<LoadResult<Integer, User>> loadFuture(@NotNull LoadParams<Integer> params) {
//            // Start refresh at page 1 if undefined.
//            Integer nextPageNumber = params.getKey();
//            if (nextPageNumber == null) {
//                nextPageNumber = 0;
//            }
//
//            ListenableFuture<LoadResult<Integer, User>> pageFuture =
//                    Futures.transform(userDao.getUsers(nextPageNumber, params.getLoadSize()),
//                            (response) -> toLoadResult(params, response), bgExecutor);
//
////            ListenableFuture<LoadResult<Integer, User>> partialLoadResultFuture =
////                    Futures.catching(pageFuture, HttpException.class,
////                            LoadResult.Error::new, mBgExecutor);
//
//            return Futures.catching(pageFuture,
//                    IOException.class, LoadResult.Error::new, bgExecutor);
//        }
//
//        private LoadResult<Integer, User> toLoadResult(LoadParams<Integer> params, @NonNull List<User> response) {
//            int key = params.getKey() != null && params.getKey() >= 0 ? params.getKey() : 0;
//            return new LoadResult.Page<>(response,
//                    key > 0 ? key - 1 : key, // Only paging forward.
//                    key + 1,
//                    LoadResult.Page.COUNT_UNDEFINED,
//                    LoadResult.Page.COUNT_UNDEFINED);
//        }
//
//        @Nullable
//        @Override
//        public Integer getRefreshKey(@NotNull PagingState<Integer, User> state) {
//            // Try to find the page key of the closest page to anchorPosition, from
//            // either the prevKey or the nextKey, but you need to handle nullability
//            // here:
//            //  * prevKey == null -> anchorPage is the first page.
//            //  * nextKey == null -> anchorPage is the last page.
//            //  * both prevKey and nextKey null -> anchorPage is the initial page, so
//            //    just return null.
//            Integer anchorPosition = state.getAnchorPosition();
//            if (anchorPosition == null) {
//                return null;
//            }
//
//            LoadResult.Page<Integer, User> anchorPage = state.closestPageToPosition(anchorPosition);
//            if (anchorPage == null) {
//                return null;
//            }
//
//            Integer prevKey = anchorPage.getPrevKey();
//            if (prevKey != null) {
//                return prevKey + 1;
//            }
//
//            Integer nextKey = anchorPage.getNextKey();
//            if (nextKey != null) {
//                return nextKey - 1;
//            }
//
//            return null;
//        }
//    }

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
                    Log.d("paging_user", "Remote Mediator Refresh " + pagingState);
                    break;
                case PREPEND:
                    // In this example, you never need to prepend, since REFRESH will always
                    // load the first page in the list. Immediately return, reporting end of
                    // pagination.
                    Log.d("paging_user", "Remote Mediator Prepend " + pagingState);
                    return Futures.immediateFuture(new MediatorResult.Success(true));
                case APPEND:
                    lastItem = pagingState.lastItemOrNull();

                    // You must explicitly check if the last item is null when appending,
                    // since passing null to networkService is only valid for initial load.
                    // If lastItem is null it means no items were loaded after the initial
                    // REFRESH and there are no more items to load.
                    Log.d("paging_user", "Remote Mediator Append " + pagingState);

                    if (lastItem == null) {
                        return Futures.immediateFuture(new MediatorResult.Success(true));
                    }

//                List<PagingSource.LoadResult.Page<Integer, User>> pages = pagingState.getPages();
//                next = pages.size() + 1;

                    break;
            }

            Integer page = 0;
            if ( pagingState.getPages().size() != 0 )
                page = pagingState.getPages().get(pagingState.getPages().size() - 1).getNextKey();

            if (page == null)
                return Futures.immediateFuture(new MediatorResult.Success(true));

//            int page = lastPage.getNextKey() == null ? 0 : lastPage.getNextKey();
//            int page = lastItem == null ? 0 : userDao.getPage(lastItem, pagingState.getConfig().pageSize);

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

//        @NotNull
//        @Override
//        public ListenableFuture<InitializeAction> initializeFuture() {
//            long cacheTimeout = TimeUnit.HOURS.convert(1, TimeUnit.MILLISECONDS);
//            return Futures.transform(
//                    userDao.lastUpdated(),
//                    lastUpdatedMillis -> {
//                        if (System.currentTimeMillis() - lastUpdatedMillis >= cacheTimeout) {
//                            // Cached data is up-to-date, so there is no need to re-fetch
//                            // from the network.
//                            return InitializeAction.SKIP_INITIAL_REFRESH;
//                        } else {
//                            // Need to refresh cached data from network; returning
//                            // LAUNCH_INITIAL_REFRESH here will also block RemoteMediator's
//                            // APPEND and PREPEND from running until REFRESH succeeds.
//                            return InitializeAction.LAUNCH_INITIAL_REFRESH;
//                        }
//                    },
//                    mBgExecutor);
//        }

    }
}

