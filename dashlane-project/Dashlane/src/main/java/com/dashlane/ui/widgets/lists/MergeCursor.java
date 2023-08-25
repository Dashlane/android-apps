package com.dashlane.ui.widgets.lists;

import android.database.AbstractCursor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;

public class MergeCursor extends AbstractCursor {
    private DataSetObserver mObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            
            
            mPos = -1;
        }

        @Override
        public void onInvalidated() {
            mPos = -1;
        }
    };
    private Cursor mCursor; 
    private Cursor[] mCursors;

    public MergeCursor(Cursor[] cursors) {
        mCursors = cursors;
        mCursor = cursors[0];

        for (int i = 0; i < mCursors.length; i++) {
            if (mCursors[i] == null) continue;

            mCursors[i].registerDataSetObserver(mObserver);
        }
    }

    @Override
    public int getCount() {
        int count = 0;
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] != null) {
                count += mCursors[i].getCount();
            }
        }
        return count;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        mCursor = null;
        int cursorStartPos = 0;
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] == null) {
                continue;
            }

            if (newPosition < (cursorStartPos + mCursors[i].getCount())) {
                mCursor = mCursors[i];
                break;
            }

            cursorStartPos += mCursors[i].getCount();
        }

        if (mCursor != null) {
            boolean ret = mCursor.moveToPosition(newPosition - cursorStartPos);
            return ret;
        }
        return false;
    }

    @Override
    public String getString(int column) {
        return mCursor.getString(column);
    }

    @Override
    public short getShort(int column) {
        return mCursor.getShort(column);
    }

    @Override
    public int getInt(int column) {
        return mCursor.getInt(column);
    }

    @Override
    public long getLong(int column) {
        return mCursor.getLong(column);
    }

    @Override
    public float getFloat(int column) {
        return mCursor.getFloat(column);
    }

    @Override
    public double getDouble(int column) {
        return mCursor.getDouble(column);
    }

    @Override
    public int getType(int column) {
        return mCursor.getType(column);
    }

    @Override
    public boolean isNull(int column) {
        return mCursor.isNull(column);
    }

    @Override
    public byte[] getBlob(int column) {
        return mCursor.getBlob(column);
    }

    @Override
    public String[] getColumnNames() {
        if (mCursor != null) {
            return mCursor.getColumnNames();
        } else {
            return new String[0];
        }
    }

    @Override
    public void deactivate() {
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] != null) {
                mCursors[i].deactivate();
            }
        }
        super.deactivate();
    }

    @Override
    public void close() {
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] == null) continue;
            mCursors[i].close();
        }
        super.close();
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] != null) {
                mCursors[i].registerContentObserver(observer);
            }
        }
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] != null) {
                mCursors[i].unregisterContentObserver(observer);
            }
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] != null) {
                mCursors[i].registerDataSetObserver(observer);
            }
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] != null) {
                mCursors[i].unregisterDataSetObserver(observer);
            }
        }
    }

    @Override
    public boolean requery() {
        int length = mCursors.length;
        for (int i = 0; i < length; i++) {
            if (mCursors[i] == null) {
                continue;
            }

            if (mCursors[i].requery() == false) {
                return false;
            }
        }

        return true;
    }

    public int getCursorCount() {
        return mCursors.length;

    }

    public Cursor[] getCursors() {
        return mCursors;
    }
}