package com.example.oneidentity.app.views.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataAdapter;
import androidx.paging.PagingLiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.oneidentity.MainActivity;
import com.example.oneidentity.R;
import com.example.oneidentity.app.models.User;
import com.example.oneidentity.app.services.APIService;
import com.example.oneidentity.app.services.AppDatabaseService;
import com.example.oneidentity.databinding.UserCardBinding;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

import kotlinx.coroutines.CoroutineScope;

/**
 * A fragment representing a list of Items.
 */
public class UserList extends Fragment {

    private final int pageSize = 100;
//    private final int initialCount = 20;
    private UserViewModel viewModel;
    private AppDatabaseService database;
    private Executor bgExecutor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bgExecutor = new ViewModelProvider(requireActivity()).get(MainActivity.ApplicationModel.class).getBgExecutor();
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        database = AppDatabaseService.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_list, container, false);
        initView(view);

        return view;
    }

    private void initView(View view)
    {
        LiveData<PagingData<User>> pagingData = initPagingData();
        UserAdapter pagingAdapter = new UserAdapter(new UserComparator());
        RecyclerView recyclerView = view.findViewById(R.id.user_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(pagingAdapter);

        Log.d("paging_user", "observe submit the page");
        pagingData.observe(getViewLifecycleOwner(), data -> {
            Log.d("paging_user", "A change as occur " + data);
            pagingAdapter.submitData(getViewLifecycleOwner().getLifecycle(), data);
        });

    }

    private LiveData<PagingData<User>> initPagingData()
    {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Pager<Integer, User> pager = new Pager(
                new PagingConfig(pageSize),
                null, // initialKey
                UserInterface.getUserMediator(APIService.getInstance(requireContext()), database, bgExecutor),
                () -> database.getUserDao().getPagedUsers(0, pageSize)
//                () -> UserInterface.getUserSource(database, bgExecutor)
        );

        LiveData<PagingData<User>> liveData = PagingLiveData.getLiveData(pager);

        LiveData<PagingData<User>> pagingData = PagingLiveData.cachedIn(liveData, viewModelScope);
        return pagingData;
    }

    private class UserAdapter extends PagingDataAdapter<User, UserViewHolder> {
        UserAdapter(@NotNull DiffUtil.ItemCallback<User> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            UserCardBinding bindings = UserCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new UserViewHolder(bindings);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = getItem(position);
            // Note that item may be null. ViewHolder must support binding a
            // null item as a placeholder.

            holder.bind(user);
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final UserCardBinding bindings;

        public UserViewHolder(@NonNull UserCardBinding bindings) {
            super(bindings.getRoot());
            this.bindings = bindings;
        }

        public void bind(User user)
        {
            bindings.setUser(user);
            bindings.cardViewRoot.setOnClickListener(view -> {
                UserListDirections.ActionUserListToUserDetails action = UserListDirections.actionUserListToUserDetails(user);
                Navigation.findNavController(bindings.getRoot()).navigate(action);
            });
        }

        @BindingAdapter({"imageUrl"})
        public static void loadImage(ImageView view, String url) {
            Glide.with(view.getContext()).load(url)
                    .fitCenter().into(view);
        }
    }

    class UserComparator extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem,
                                       @NonNull User newItem) {
            // Id is unique.
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem,
                                          @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class UserViewModel extends ViewModel {
    }
}