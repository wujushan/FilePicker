package ru.bartwell.exfilepicker.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import ru.bartwell.exfilepicker.ui.eventbus.DirPathEvent;
import ru.bartwell.exfilepicker.ui.eventbus.MultiChoiceEvent;
import ru.bartwell.exfilepicker.ui.eventbus.ToolbarMenuEvent;
import ru.bartwell.exfilepicker.utils.ListUtils;
import ru.bartwell.exfilepicker.utils.Utils;

public class ExFilePickerFragment extends Fragment implements OnListItemClickListener {
    private static final String DIRECTORY_PARAM = "directory";
    private static final String CHOOSE_ONLY_ONE_ITEM_PARAM = "choose_only_one_item";
    private static final String SHOW_ONLY_EXTENSIONS_PARAM = "show_only_extensions";
    private static final String EXCEPT_EXTENSIONS_PARAM = "except_extensions";
    private static final String IS_NEW_FOLDER_BT_DISABLE_PARAM = "is_new_folder_bt_disable";
    private static final String IS_SORT_BT_DISABLE_PARAM = "is_sort_bt_disable";
    private static final String IS_QUIT_BT_ENABLE_PARAM = "is_quit_bt_enable";
    private static final String CHOICE_TYPE_PARAM = "choice_type";
    private static final String SORTING_TYPE_PARAM = "sorting_type";
    private static final String FIRST_ITEM_AS_UP_PARAM = "use_first_item_as_up_enable";
    private static final String HIDE_HIDDEN_FILES_PARAM = "hide_hidden_files";

    @BindView(R2.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R2.id.empty_view)
    FrameLayout mEmptyView;
    Unbinder unbinder;
    private FilesListAdapter mAdapter;
    /**
     * true if enable multi choice mode and false if disable multi choice mode
     */
    private boolean mIsMultiChoiceModeEnabled;

    private static String currentDirPath;
    private OnFragmentInteractionListener mListener;

    private boolean mCanChooseOnlyOneItem;
    private String[] mShowOnlyExtensions;
    private String[] mExceptExtensions;
    private boolean mIsNewFolderButtonDisabled;
    private boolean mIsSortButtonDisabled;
    private boolean mIsQuitButtonEnabled;
    private File mCurrentDirectory;
    /**
     * Three choice type : ALL, FILES, DIRECTORIES
     */
    @NonNull
    private ExFilePicker.ChoiceType mChoiceType = ExFilePicker.ChoiceType.ALL;
    /**
     * Six sorting type : NAME_ASC, NAME_DESC, SIZE_ASC, SIZE_DESC, DATE_ASC, DATE_DESC
     */
    @NonNull
    private ExFilePicker.SortingType mSortingType = ExFilePicker.SortingType.NAME_ASC;
    /**
     * true if use first item as up and false if don't use first item as up
     */
    @NonNull
    private boolean mUseFirstItemAsUpEnabled;
    /**
     * true if hide hidden files and false if don't hide hidden files
     */
    private boolean mHideHiddenFiles;
    private static String TOP_DIRECTORY = "/";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentDirPath = getArguments().getString(DIRECTORY_PARAM);
            mCanChooseOnlyOneItem = bundle.getBoolean(CHOOSE_ONLY_ONE_ITEM_PARAM);
            mShowOnlyExtensions = bundle.getStringArray(SHOW_ONLY_EXTENSIONS_PARAM);
            mExceptExtensions = bundle.getStringArray(EXCEPT_EXTENSIONS_PARAM);
            mIsNewFolderButtonDisabled = bundle.getBoolean(IS_NEW_FOLDER_BT_DISABLE_PARAM);
            mIsSortButtonDisabled = bundle.getBoolean(IS_SORT_BT_DISABLE_PARAM);
            mIsQuitButtonEnabled = bundle.getBoolean(IS_QUIT_BT_ENABLE_PARAM);
            mChoiceType = ExFilePicker.ChoiceType.values()[bundle.getInt(CHOICE_TYPE_PARAM)];
            mSortingType = ExFilePicker.SortingType.values()[bundle.getInt(SORTING_TYPE_PARAM)];
            mUseFirstItemAsUpEnabled = bundle.getBoolean(FIRST_ITEM_AS_UP_PARAM);
            mHideHiddenFiles = bundle.getBoolean(HIDE_HIDDEN_FILES_PARAM);
            TOP_DIRECTORY = currentDirPath;
        }
        //register event bus
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ToolbarMenuEvent event) {
        switch (event.getEventType()) {
            case ToolbarMenuEvent.OK:

                break;
            case ToolbarMenuEvent.SORT:
                break;
            case ToolbarMenuEvent.NEW_FOLDER:
                break;
            case ToolbarMenuEvent.SELECT_ALL:
                break;
            case ToolbarMenuEvent.DESELECT:
                break;
            case ToolbarMenuEvent.INVERT_SELECTION:
                break;
            case ToolbarMenuEvent.CHANGE_VIEW:
                break;
            default:
                break;
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public ExFilePickerFragment() {
    }

    public static ExFilePickerFragment newInstance(String directoryPath) {
        ExFilePickerFragment fragment = new ExFilePickerFragment();
        Bundle args = new Bundle();
        args.putString(DIRECTORY_PARAM, directoryPath);
        fragment.setArguments(args);
        return fragment;
    }

    public static ExFilePickerFragment newInstance(String directoryPath, boolean canChooseOnlyOneItem, String[] showOnlyExtensions
            , String[] exceptExtensions, boolean isNewFolderButtonDisabled, boolean isSortButtonDisabled
            , boolean isQuitButtonEnabled, ExFilePicker.ChoiceType choiceType, ExFilePicker.SortingType sortingType
            , boolean useFirstItemAsUpEnabled, boolean hideHiddenFiles) {
        ExFilePickerFragment fragment = new ExFilePickerFragment();
        Bundle args = new Bundle();
        args.putString(DIRECTORY_PARAM, directoryPath);
        args.putBoolean(CHOOSE_ONLY_ONE_ITEM_PARAM, canChooseOnlyOneItem);
        args.putStringArray(SHOW_ONLY_EXTENSIONS_PARAM, showOnlyExtensions);
        args.putStringArray(EXCEPT_EXTENSIONS_PARAM, exceptExtensions);
        args.putBoolean(IS_NEW_FOLDER_BT_DISABLE_PARAM, isNewFolderButtonDisabled);
        args.putBoolean(IS_SORT_BT_DISABLE_PARAM, isSortButtonDisabled);
        args.putBoolean(IS_QUIT_BT_ENABLE_PARAM, isQuitButtonEnabled);
        args.putInt(CHOICE_TYPE_PARAM, choiceType.ordinal());
        args.putInt(SORTING_TYPE_PARAM, sortingType.ordinal());
        args.putBoolean(FIRST_ITEM_AS_UP_PARAM, useFirstItemAsUpEnabled);
        args.putBoolean(HIDE_HIDDEN_FILES_PARAM, hideHiddenFiles);
        fragment.setArguments(args);
        return fragment;
    }


    private void readDirectory(String currentDirPath) {
        //post event to change the title of toolbar
        EventBus.getDefault().post(new DirPathEvent(currentDirPath));
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

    private boolean isTopDirectory(@Nullable File directory) {
        return directory != null && TOP_DIRECTORY.equals(directory.getAbsolutePath());
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
    public void onListItemClick(int position) {

    }

    @Override
    public void onListItemLongClick(int position) {
        if (!mIsMultiChoiceModeEnabled && position != OnListItemClickListener.POSITION_UP) {
            mIsMultiChoiceModeEnabled = true;
            if (mChoiceType != ExFilePicker.ChoiceType.FILES || !mAdapter.getItem(position).isDirectory()) {
                mAdapter.setItemSelected(position, true);
            }
            setMultiChoiceModeEnabled(true);
        }
    }

    private void setMultiChoiceModeEnabled(boolean enabled) {
        mIsMultiChoiceModeEnabled = enabled;
        mAdapter.setUseFirstItemAsUpEnabled(!enabled && mUseFirstItemAsUpEnabled && !isTopDirectory(mCurrentDirectory));
        mAdapter.setMultiChoiceModeEnabled(enabled);
        EventBus.getDefault().post(new MultiChoiceEvent(enabled,mAdapter.isGridModeEnabled()));
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
