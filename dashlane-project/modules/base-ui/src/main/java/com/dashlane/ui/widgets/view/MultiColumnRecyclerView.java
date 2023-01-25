package com.dashlane.ui.widgets.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.dashlane.ui.R;
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.dashlane.ui.adapter.HeaderItem;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



public class MultiColumnRecyclerView extends RecyclerView {
    public static final MultiColumnSpec DATA_IDENTIFIER_SPEC = new MultiColumnSpec() {
        @Override
        public int getMinMultiColumnCount() {
            return 2;
        }

        @Override
        public int getMaxColumnCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public float getItemWidth(Resources res) {
            return res.getDimension(R.dimen.item_width);
        }

        @Override
        public int getItemWidthSizeType() {
            return SizeType.EXACT;
        }

        @Override
        public float getMarginBetween(Resources res) {
            return res.getDimension(R.dimen.items_margin_between);
        }

        @Override
        public float getMarginExt(Resources res) {
            return res.getDimension(R.dimen.items_margin_ext);
        }
    };

    private MultiColumnSpec mSpec = DATA_IDENTIFIER_SPEC;

    public MultiColumnRecyclerView(Context context) {
        this(context, null);
    }

    public MultiColumnRecyclerView(Context context,
                                   @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiColumnRecyclerView(Context context,
                                   @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutManager(new GridLayoutManager(context, 1));
        setItemAnimator(null);

        getGridLayoutManager().setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object object = getAdapter().get(position);
                if (object instanceof DashlaneRecyclerAdapter.MultiColumnViewTypeProvider) {
                    int spanCount = getGridLayoutManager().getSpanCount();
                    return ((DashlaneRecyclerAdapter.MultiColumnViewTypeProvider) object)
                            .getSpanSize(spanCount);
                } else {
                    return 1;
                }
            }
        });
        addItemDecoration(new CenteredItemsList());
        setAdapter(new DashlaneRecyclerAdapter<>());
    }

    @Override
    public DashlaneRecyclerAdapter getAdapter() {
        return (DashlaneRecyclerAdapter) super.getAdapter();
    }

    public static int getColumnCount(int measuredWidth, float itemWidth, float marginBetween, float marginLine) {
        float spaceLeftAfterMargin = measuredWidth - marginLine * 2 + marginBetween;
        return (int) (spaceLeftAfterMargin / (marginBetween + itemWidth));
    }

    static float getMinWidth(int noCol, float marginBetween, float marginLine, float itemWidth) {
        return noCol * itemWidth
               + (noCol - 1) * marginBetween
               + (marginLine * 2);
    }

    @Override
    protected void onMeasure(int widthSpace, int heightSpec) {
        super.onMeasure(widthSpace, heightSpec);

        int measuredWidth = getMeasuredWidth();

        Resources res = getResources();

        int noCol = getColumnCount(measuredWidth, getItemWidth(res), getMarginBetween(res), getMarginExt(res));

        if (noCol < getMinMultiColumnCount()) {
            noCol = 1;
        }
        int maxColumnCount = getMaxColumnCount();
        if (noCol > maxColumnCount) {
            noCol = maxColumnCount;
        }

        getGridLayoutManager().setSpanCount(noCol);
    }

    public void setSpec(MultiColumnSpec spec) {
        mSpec = spec;
    }

    private int getMinMultiColumnCount() {
        return getSpec().getMinMultiColumnCount();
    }

    private int getMaxColumnCount() {
        return getSpec().getMaxColumnCount();
    }

    @VisibleForTesting
    public float getMarginExt(Resources res) {
        return getSpec().getMarginExt(res);
    }

    @VisibleForTesting
    public float getMarginBetween(Resources res) {
        return getSpec().getMarginBetween(res);
    }

    @VisibleForTesting
    public float getItemWidth(Resources res) {
        return getSpec().getItemWidth(res);
    }

    @VisibleForTesting
    @MultiColumnSpec.SizeType
    public int getSizeType() {
        return getSpec().getItemWidthSizeType();
    }

    @NonNull
    public MultiColumnSpec getSpec() {
        if (mSpec == null) {
            mSpec = DATA_IDENTIFIER_SPEC;
        }
        return mSpec;
    }

    public final GridLayoutManager getGridLayoutManager() {
        return (GridLayoutManager) getLayoutManager();
    }

    public interface MultiColumnSpec {

        

        int getMinMultiColumnCount();

        int getMaxColumnCount();

        float getItemWidth(Resources res);

        @SizeType
        int getItemWidthSizeType();

        float getMarginBetween(Resources res);

        float getMarginExt(Resources res);

        @IntDef({SizeType.EXACT, SizeType.MIN})
        @Retention(RetentionPolicy.CLASS)
        @interface SizeType {
            int EXACT = 0;
            int MIN = 1;
        }
    }

    @VisibleForTesting
    static class CenteredItemsList extends ItemDecoration {

        private int mLastParentWidth;
        private int[][] mPadding;

        static int[][] computePaddingColumn(MultiColumnRecyclerView parent, Resources resources,
                                            int parentWidth, int spanCount) {
            int[][] padding = new int[spanCount][];

            float withPerItem = parentWidth / (float) spanCount;
            float itemWidth = parent.getItemWidth(resources);
            float marginLine = parent.getMarginExt(resources);
            float marginBetween = parent.getMarginBetween(resources);

            if (parent.getSizeType() == MultiColumnSpec.SizeType.EXACT) {
                computePaddingColumnExactWidth(padding, spanCount,
                                               parentWidth,
                                               withPerItem, itemWidth, marginLine, marginBetween);
            }
            return padding;
        }

        private static void computePaddingColumnExactWidth(int[][] padding, int spanCount, int parentWidth,
                                                           float withPerItem, float itemWidth, float marginLine,
                                                           float marginBetween) {
            float minWidth = getMinWidth(spanCount, marginBetween, marginLine, itemWidth);

            float leftRightExtremeMargin = marginLine + Math.max(0, (parentWidth - minWidth) / 2);
            float leftRightBetweenMargin = marginBetween / 2;

            float left = 0;
            for (int i = 0; i < spanCount; i++) {
                if (i == 0) {
                    left = leftRightExtremeMargin;
                } else {
                    left += leftRightBetweenMargin;
                }
                float right = withPerItem - left - itemWidth;
                padding[i] = new int[]{Math.round(left), Math.round(right)};
                left = -right + leftRightBetweenMargin;
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent instanceof MultiColumnRecyclerView) {
                int adapterPosition = parent.getChildAdapterPosition(view);
                getItemOffsets(outRect, (MultiColumnRecyclerView) parent, adapterPosition);
            }
        }

        void getItemOffsets(Rect outRect, MultiColumnRecyclerView parent, int adapterPosition) {
            int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            if (!isMultiColumn(parent, spanCount)) {
                return;
            }
            DashlaneRecyclerAdapter adapter = parent.getAdapter();
            if (isFullWidthItem(adapter, adapterPosition, spanCount)
                && !isHeader(adapter, adapterPosition)) {
                return;
            }

            int indexInColumn = getIndexInColumn(adapter, adapterPosition, spanCount);

            int parentWidth = parent.getMeasuredWidth();

            if (isPaddingCachedChanged(spanCount, parentWidth)) {
                mLastParentWidth = parentWidth;
                mPadding = computePaddingColumn(parent, parent.getResources(), parentWidth, spanCount);
            }
            int[] padding = mPadding[indexInColumn];
            if (padding != null) {
                outRect.left += padding[0];
                outRect.right += padding[1];
            }
        }

        private int getIndexInColumn(DashlaneRecyclerAdapter adapter, int adapterPosition, int spanCount) {
            if (isFullWidthItem(adapter, adapterPosition, spanCount)) {
                return 0;
            }
            int positionFromLatestFullWidthItem = 0;
            for (int i = adapterPosition - 1; i >= 0; i--) {
                if (isFullWidthItem(adapter, i, spanCount)) {
                    break;
                } else {
                    positionFromLatestFullWidthItem++;
                }
            }
            return positionFromLatestFullWidthItem % spanCount;
        }

        private boolean isMultiColumn(MultiColumnRecyclerView parent, int spanCount) {
            return spanCount >= parent.getMinMultiColumnCount();
        }

        private boolean isHeader(DashlaneRecyclerAdapter adapter, int adapterPosition) {
            return adapterPosition >= 0 && adapterPosition < adapter.size() &&
                   adapter.get(adapterPosition) instanceof HeaderItem;
        }

        private boolean isFullWidthItem(DashlaneRecyclerAdapter adapter, int adapterPosition, int spanCount) {
            if (adapterPosition < 0 || adapterPosition >= adapter.size()) {
                return true;
            }
            Object item = adapter.get(adapterPosition);
            return !(item instanceof DashlaneRecyclerAdapter.MultiColumnViewTypeProvider) ||
                   ((DashlaneRecyclerAdapter.MultiColumnViewTypeProvider) item).getSpanSize(spanCount) == spanCount;
        }

        private boolean isPaddingCachedChanged(int spanCount, int parentWidth) {
            return mPadding == null || parentWidth != mLastParentWidth || spanCount * 2 != mPadding.length;
        }

    }
}
