package ru.bartwell.exfilepicker.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.bartwell.exfilepicker.R;
import ru.bartwell.exfilepicker.R2;
import ru.bartwell.exfilepicker.ui.adapter.SelectedFilesAdapter;
import ru.bartwell.exfilepicker.ui.eventbus.SelectedFileDeletedEvent;
import ru.bartwell.exfilepicker.ui.eventbus.SelectedFilesUpdatedEvent;

public class SelectecFilesFragment extends Fragment implements SelectedFilesAdapter.OnFileItemDeleteListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R2.id.selected_rv)
    RecyclerView mSelectedRv;
    Unbinder unbinder;

    private SelectedFilesAdapter mAdapter;
    private List<String> selectedFiles;

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SelectecFilesFragment() {
    }


    public static SelectecFilesFragment newInstance(String param1, String param2) {
        SelectecFilesFragment fragment = new SelectecFilesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_selectec_files, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecycler();
    }

    private void initRecycler() {
        mAdapter = new SelectedFilesAdapter(selectedFiles);
        mSelectedRv.setHasFixedSize(true);
        mSelectedRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSelectedRv.setAdapter(mAdapter);
        mAdapter.setOnFileItemDelete(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SelectedFilesUpdatedEvent event) {
        selectedFiles = event.getSelectedFiles();
        mAdapter.setSelectedFileItems(selectedFiles);
        mAdapter.notifyDataSetChanged();
    }

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
    public void onItemDelete(int position) {
        mSelectedRv.getAdapter().notifyItemRemoved(position);
//        mAdapter.notifyItemRemoved(position);
        selectedFiles.remove(position);
        EventBus.getDefault().post(new SelectedFileDeletedEvent(position));
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
