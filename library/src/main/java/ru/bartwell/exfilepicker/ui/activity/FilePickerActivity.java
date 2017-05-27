package ru.bartwell.exfilepicker.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.R;
import ru.bartwell.exfilepicker.R2;
import ru.bartwell.exfilepicker.ui.dialog.SortingDialog;
import ru.bartwell.exfilepicker.ui.fragment.ExFilePickerFragment;
import ru.bartwell.exfilepicker.ui.view.FilesListToolbar;

public class FilePickerActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {
    @BindView(R2.id.toolbar)
    FilesListToolbar mToolbar;
    @BindView(R2.id.file_content_frame)
    FrameLayout mFileContentFrame;
    @BindView(R2.id.sel_files_frame)
    FrameLayout mSelFilesFrame;
    @BindView(R2.id.drawer)
    DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;


    public static final String EXTRA_CAN_CHOOSE_ONLY_ONE_ITEM = "CAN_CHOOSE_ONLY_ONE_ITEM";
    public static final String EXTRA_SHOW_ONLY_EXTENSIONS = "SHOW_ONLY_EXTENSIONS";
    public static final String EXTRA_EXCEPT_EXTENSIONS = "EXCEPT_EXTENSIONS";
    public static final String EXTRA_IS_NEW_FOLDER_BUTTON_DISABLED = "IS_NEW_FOLDER_BUTTON_DISABLED";
    public static final String EXTRA_IS_SORT_BUTTON_DISABLED = "IS_SORT_BUTTON_DISABLED";
    public static final String EXTRA_IS_QUIT_BUTTON_ENABLED = "IS_QUIT_BUTTON_ENABLED";
    public static final String EXTRA_CHOICE_TYPE = "CHOICE_TYPE";
    public static final String EXTRA_SORTING_TYPE = "SORTING_TYPE";
    public static final String EXTRA_START_DIRECTORY = "START_DIRECTORY";
    public static final String EXTRA_USE_FIRST_ITEM_AS_UP_ENABLED = "USE_FIRST_ITEM_AS_UP_ENABLED";
    public static final String EXTRA_HIDE_HIDDEN_FILES = "HIDE_HIDDEN_FILES";
    public static final String PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 2;

    private boolean mCanChooseOnlyOneItem;
    private String[] mShowOnlyExtensions;
    private String[] mExceptExtensions;
    private boolean mIsNewFolderButtonDisabled;
    private boolean mIsSortButtonDisabled;
    private boolean mIsQuitButtonEnabled;

    private File mCurrentDirectory;
    private boolean mUseFirstItemAsUpEnabled;
    private boolean mHideHiddenFiles;

    private static String TOP_DIRECTORY = "/"; //default top directory

    @NonNull
    private ExFilePicker.ChoiceType mChoiceType = ExFilePicker.ChoiceType.ALL;
    @NonNull
    private ExFilePicker.SortingType mSortingType = ExFilePicker.SortingType.NAME_ASC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        ButterKnife.bind(this);
        initActionBar();
        initDrawer();
        handleIntent();
        setupViews();
        if (ContextCompat.checkSelfPermission(this, PERMISSION_READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            readDirectory(mCurrentDirectory);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * read directory
     *
     * @param currentDirectory
     */
    private void readDirectory(File currentDirectory) {
        ExFilePickerFragment fragment = ExFilePickerFragment.newInstance(currentDirectory.getAbsolutePath());
        replaceFragment(R.id.file_content_frame,fragment);
    }

    private void replaceFragment(int layoutId, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.file_content_frame, fragment);
        transaction.commit();
    }

    private void setupViews() {
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setQuitButtonEnabled(mIsQuitButtonEnabled);
        mToolbar.setMultiChoiceModeEnabled(false);

        Menu menu = mToolbar.getMenu();
        menu.findItem(R.id.ok).setVisible(mChoiceType == ExFilePicker.ChoiceType.DIRECTORIES);
        menu.findItem(R.id.new_folder).setVisible(!mIsNewFolderButtonDisabled);
        menu.findItem(R.id.sort).setVisible(!mIsSortButtonDisabled);
    }

    private void initActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initDrawer() {
        mToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                //获取屏幕的宽高
                WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                //设置右面的布局位置  根据左面菜单的right作为右面布局的left   左面的right+屏幕的宽度（或者right的宽度这里是相等的）为右面布局的right
                mFileContentFrame.layout(mSelFilesFrame.getRight(), 0, mSelFilesFrame.getRight() + point.x, point.y);
            }
        };
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mCanChooseOnlyOneItem = intent.getBooleanExtra(EXTRA_CAN_CHOOSE_ONLY_ONE_ITEM, false);
        mShowOnlyExtensions = intent.getStringArrayExtra(EXTRA_SHOW_ONLY_EXTENSIONS);
        mExceptExtensions = intent.getStringArrayExtra(EXTRA_EXCEPT_EXTENSIONS);
        mIsNewFolderButtonDisabled = intent.getBooleanExtra(EXTRA_IS_NEW_FOLDER_BUTTON_DISABLED, false);
        mIsSortButtonDisabled = intent.getBooleanExtra(EXTRA_IS_SORT_BUTTON_DISABLED, false);
        mIsQuitButtonEnabled = intent.getBooleanExtra(EXTRA_IS_QUIT_BUTTON_ENABLED, false);
        mChoiceType = (ExFilePicker.ChoiceType) intent.getSerializableExtra(EXTRA_CHOICE_TYPE);
        mSortingType = (ExFilePicker.SortingType) intent.getSerializableExtra(EXTRA_SORTING_TYPE);
        mCurrentDirectory = getStartDirectory(intent);
        mUseFirstItemAsUpEnabled = intent.getBooleanExtra(EXTRA_USE_FIRST_ITEM_AS_UP_ENABLED, false);
        mHideHiddenFiles = intent.getBooleanExtra(EXTRA_HIDE_HIDDEN_FILES, false);
    }

    @NonNull
    private File getStartDirectory(@NonNull Intent intent) {
        File path = null;
        String startPath = intent.getStringExtra(EXTRA_START_DIRECTORY);
        if (startPath != null && startPath.length() > 0) {
            File tmp = new File(startPath);
            if (tmp.exists() && tmp.isDirectory()) {
                path = tmp;
                TOP_DIRECTORY = path.getAbsolutePath();
            }
        }
        if (path == null) {
            path = new File("/");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                path = Environment.getExternalStorageDirectory();
            }
        }
        return path;
    }

    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.ok) {
            if (mIsMultiChoiceModeEnabled) {
                finishWithResult(mCurrentDirectory, mAdapter.getSelectedItems());
            } else if (mChoiceType == ExFilePicker.ChoiceType.DIRECTORIES) {
                if (isTopDirectory(mCurrentDirectory)) {
                    finishWithResult(mCurrentDirectory, "/");
                } else {
                    finishWithResult(mCurrentDirectory.getParentFile(), mCurrentDirectory.getName());
                }
            }
        } else if (itemId == R.id.sort) {
            SortingDialog dialog = new SortingDialog(this);
            dialog.setOnSortingSelectedListener(this);
            dialog.show();
        } else if (itemId == R.id.new_folder) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showNewFolderDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            }
        } else if (itemId == R.id.select_all) {
            mAdapter.selectAll();
        } else if (itemId == R.id.deselect) {
            mAdapter.deselect();
        } else if (itemId == R.id.invert_selection) {
            mAdapter.invertSelection();
        } else if (itemId == R.id.change_view) {
            toggleViewMode();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {

    }
}
