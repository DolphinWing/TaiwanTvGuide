package dolphin.apps.TaiwanTVGuide.v7;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by dolphin on 2015/03/14.
 * ViewHolder for RecyclerView
 */
public class ProgramViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    public ProgramViewHolder(View itemView) {
        super(itemView);

        mTextView = (TextView) itemView.findViewById(android.R.id.text1);
    }

    public void bindItem(String text, Object tag) {
        mTextView.setText(text);
        mTextView.setTag(tag);
    }

    public interface OnItemClickListener {
        public void onItemClick(View view);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        if (mTextView == null) {
            return;
        }
        if (l == null) {
            mTextView.setOnClickListener(null);
        } else {
            final OnItemClickListener listener = l;
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view);
                }
            });
        }
    }
}
