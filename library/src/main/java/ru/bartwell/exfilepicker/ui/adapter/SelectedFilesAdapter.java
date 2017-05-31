package ru.bartwell.exfilepicker.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.R;
import ru.bartwell.exfilepicker.R2;

/**
 * Created by wujs on 2017/5/31.
 */

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.Holder> implements View.OnClickListener {


    private List<String> selectedFileItems;

    public SelectedFilesAdapter() {
    }

    public SelectedFilesAdapter(List<String> selectedFileItems) {
        this.selectedFileItems = selectedFileItems;
    }

    public void setSelectedFileItems(List<String> selectedFileItems) {
        this.selectedFileItems = selectedFileItems;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.rv_selected_item_layout;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if (position < selectedFileItems.size()) {
            holder.itemView.setTag(position);
            holder.mFileNameTx.setText(selectedFileItems.get(position));
            holder.mDelBt.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return selectedFileItems != null && selectedFileItems.size() > 0 ? selectedFileItems.size() : 0;
    }

    @Override
    public void onClick(View v) {
        if (mOnFileItemDeleteListener == null) {
            return;
        }
        int position = (int) v.getTag();
        mOnFileItemDeleteListener.onItemDelete(position);

    }

    class Holder extends RecyclerView.ViewHolder {
        @BindView(R2.id.file_name_tx)
        TextView mFileNameTx;
        @BindView(R2.id.del_bt)
        Button mDelBt;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnFileItemDeleteListener {
        void onItemDelete(int position);
    }

    OnFileItemDeleteListener mOnFileItemDeleteListener;

    public void setOnFileItemDelete(OnFileItemDeleteListener listener) {
        this.mOnFileItemDeleteListener = listener;
    }
}

