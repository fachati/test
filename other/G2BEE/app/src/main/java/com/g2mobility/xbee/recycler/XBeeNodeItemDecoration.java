package com.g2mobility.xbee.recycler;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.g2mobility.xbee.R;

/**
 * Item decoration for XBee node recycler view.
 *
 * @author Hanyu Li
 */
public class XBeeNodeItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
            RecyclerView.State state) {
        float padding = parent.getResources().getDimension(R.dimen
                .card_edge_padding_vertical);
        outRect.bottom += padding;
    }

}
