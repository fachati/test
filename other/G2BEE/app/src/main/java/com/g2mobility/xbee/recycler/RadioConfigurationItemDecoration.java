package com.g2mobility.xbee.recycler;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.g2mobility.xbee.R;

/**
 * Item decoration for the recycler view of radio configuration.
 *
 * @author Hanyu Li
 */
public class RadioConfigurationItemDecoration extends RecyclerView.ItemDecoration {

    private int mCount;

    public RadioConfigurationItemDecoration(int count) {
        mCount = count;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
            RecyclerView.State state) {
        float padding = parent.getResources().getDimension(R.dimen.card_edge_padding_vertical);
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top += padding;
        }
        if (parent.getChildLayoutPosition(view) == mCount - 1) {
            outRect.bottom += padding;
        }
    }

}
