package ru.bartwell.exfilepicker.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.R;
import ru.bartwell.exfilepicker.R2;
import ru.bartwell.exfilepicker.ui.adapter.FilesListAdapter;
import ru.bartwell.exfilepicker.ui.callback.OnListItemClickListener;
import ru.bartwell.exfilepicker.utils.ListUtils;
import ru.bartwell.exfilepicker.utils.Utils;

public class ExFilePickerFragment extends Fragment implements OnListItemClickListener {
    private static final String DIRECTORY_PARAM = "directory";
    @BindView(R2.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R2.id.empty_view)
    FrameLayout mEmptyView;
    Unbinder unbinder;
    private FilesListAdapter mAdapter;
    private static String currentDirPath;
    private boolean mIsMultiChoiceModeEnabled;

    private OnFragmentInteractionListener mListener;
    private ExFilePicker.ChoiceType mChoiceType;
    private boolean mUseFirstItemAsUpEnabled;

    public ExFilePickerFragment() {
    }

    public static ExFilePickerFragment newInstance(String directoryPath) {
        ExFilePickerFragment fragment = new ExFilePickerFragment();
        Bundle args = new Bundle();
        args.putString(DIRECTORY_PARAM, directoryPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentDirPath = getArguments().getString(DIRECTORY_PARAM);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ex_file_picker, container, false);
        unbinder = ButterKnife.bind(this, view);
        setupViews();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readDirectory(currentDirPath);
    }


    private void readDirectory(String currentDirPath) {

        setTitle(currentDirPath);
        File directory = new File(currentDirPath);
        mAdapter.setUseFirstItemAsUpEnabled(!isTopDirectory(directory) && mUseFirstItemAsUpEnabled);
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            if (mUseFirstItemAsUpEnabled) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                mAdapter.setItems(new ArrayList<File>(), mSortingType);
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            List<File> list = new ArrayList<>();
            ListUtils.ConditionChecker<File> checker;
            if (mShowOnlyExtensions != null && mShowOnlyExtensions.length > 0 && mChoiceType != ExFilePicker.ChoiceType.DIRECTORIES) {
                final List<String> showOnlyExtensions = Arrays.asList(mShowOnlyExtensions);
                checker = new ListUtils.ConditionChecker<File>() {
                    @Override
                    public boolean check(@NonNull File file) {
                        return file.isDirectory() || showOnlyExtensions.contains(Utils.getFileExtension(file.getName()));
                    }
                };
            } else {
                if (mChoiceType == ExFilePicker.ChoiceType.DIRECTORIES) {
                    checker = new ListUtils.ConditionChecker<File>() {
                        @Override
                        public boolean check(@NonNull File file) {
                            return file.isDirectory();
                        }
                    };
                } else {
                    checker = null;
                }
            }
            ListUtils.copyListWithCondition(files, list, checker);
            if (mExceptExtensions != null && mExceptExtensions.length > 0 && mChoiceType != ExFilePicker.ChoiceType.DIRECTORIES) {
                final List<String> exceptExtensions = Arrays.asList(mExceptExtensions);
                ListUtils.filterList(list, new ListUtils.ConditionChecker<File>() {
                    @Override
                    public boolean check(@NonNull File file) {
                        return !file.isDirectory() && exceptExtensions.contains(Utils.getFileExtension(file.getName()));
                    }
                });
            }
            if (mHideHiddenFiles) {
                ListUtils.filterList(list, new ListUtils.ConditionChecker<File>() {
                    @Override
                    public boolean check(@NonNull File file) {
                        return file.isHidden();
                    }
                });
            }
            mAdapter.setItems(list, mSortingType);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setupViews() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FilesListAdapter();
        mAdapter.setOnListItemClickListener(this);
        mAdapter.setCanChooseOnlyFiles(mChoiceType == ExFilePicker.ChoiceType.FILES);
        mAdapter.setUseFirstItemAsUpEnabled(mUseFirstItemAsUpEnabled);
        mRecyclerView.setAdapter(mAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onListItemClick(int position) {

    }

    @Override
    public void onListItemLongClick(int position) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
