/*
 * Copyright (C) 2017 Vincent Ganneau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vincentganneau.recyclerview.ui.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;

/**
 * {@link RecyclerView} subclass that implements choice behavior.
 * @author Vincent Ganneau
 * @see android.widget.AbsListView
 */
public class ChoiceModeRecyclerView extends RecyclerView {

    /**
     * Controls if/how the user may choose/check items in the {@link RecyclerView}.
     */
    protected int mChoiceMode = AbsListView.CHOICE_MODE_NONE;

    /**
     * Running count of how many items are currently checked.
     */
    protected int mCheckedItemCount;

    /**
     * Running state of which positions are currently checked.
     */
    protected SparseBooleanArray mCheckStates;

    /**
     * Simple constructor to use when creating a {@link ChoiceModeRecyclerView} from code.
     * @param context the {@link Context} the {@link ChoiceModeRecyclerView} is running in, through which it can access the current theme, resources, etc.
     */
    public ChoiceModeRecyclerView(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating a {@link ChoiceModeRecyclerView} from XML.
     * @param context the {@link Context} the {@link ChoiceModeRecyclerView} is running in, through which it can access the current theme, resources, etc.
     * @param attrs the attributes of the XML tag that is inflating the {@link ChoiceModeRecyclerView}.
     */
    public ChoiceModeRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor that performs inflation from XML and applies a class-specific base style from a theme attribute.
     * @param context the {@link Context} the {@link ChoiceModeRecyclerView} is running in, through which it can access the current theme, resources, etc.
     * @param attrs the attributes of the XML tag that is inflating the {@link ChoiceModeRecyclerView}.
     * @param defStyleAttr an attribute in the current theme that contains a references to a style resource that supplies default values for the {@link ChoiceModeRecyclerView}. Can be <b>0</b> to not look for defaults.
     */
    public ChoiceModeRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Setters
    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            mAdapterHasStableIds = mAdapter.hasStableIds();
            if (mChoiceMode != CHOICE_MODE_NONE && mAdapterHasStableIds &&
                    mCheckedIdStates == null) {
                mCheckedIdStates = new LongSparseArray<Integer>();
            }
        }

        if (mCheckStates != null) {
            mCheckStates.clear();
        }

        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        super.setAdapter();
    }

    /**
     * Returns the number of items currently selected. This will only be valid
     * if the choice mode is not {@link #CHOICE_MODE_NONE} (default).
     *
     * <p>To determine the specific items that are currently selected, use one of
     * the <code>getChecked*</code> methods.
     *
     * @return The number of items currently selected
     *
     * @see #getCheckedItemPosition()
     * @see #getCheckedItemPositions()
     * @see #getCheckedItemIds()
     */
    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    /**
     * Returns the checked state of the specified position. The result is only
     * valid if the choice mode has been set to {@link #CHOICE_MODE_SINGLE}
     * or {@link #CHOICE_MODE_MULTIPLE}.
     *
     * @param position The item whose checked state to return
     * @return The item's checked state or <code>false</code> if choice mode
     *         is invalid
     *
     * @see #setChoiceMode(int)
     */
    public boolean isItemChecked(int position) {
        if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
            return mCheckStates.get(position);
        }

        return false;
    }

    /**
     * Returns the currently checked item. The result is only valid if the choice
     * mode has been set to {@link #CHOICE_MODE_SINGLE}.
     *
     * @return The position of the currently checked item or
     *         {@link #INVALID_POSITION} if nothing is selected
     *
     * @see #setChoiceMode(int)
     */
    public int getCheckedItemPosition() {
        if (mChoiceMode == CHOICE_MODE_SINGLE && mCheckStates != null && mCheckStates.size() == 1) {
            return mCheckStates.keyAt(0);
        }

        return INVALID_POSITION;
    }

    /**
     * Returns the set of checked items in the list. The result is only valid if
     * the choice mode has not been set to {@link #CHOICE_MODE_NONE}.
     *
     * @return  A SparseBooleanArray which will return true for each call to
     *          get(int position) where position is a checked position in the
     *          list and false otherwise, or <code>null</code> if the choice
     *          mode is set to {@link #CHOICE_MODE_NONE}.
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != CHOICE_MODE_NONE) {
            return mCheckStates;
        }
        return null;
    }

    /**
     * Clear any choices previously set
     */
    public void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        mCheckedItemCount = 0;
    }

    /**
     * Sets the checked state of the specified position. The is only valid if
     * the choice mode has been set to {@link #CHOICE_MODE_SINGLE} or
     * {@link #CHOICE_MODE_MULTIPLE}.
     *
     * @param position The item whose checked state is to be checked
     * @param value The new checked state for the item
     */
    public void setItemChecked(int position, boolean value) {
        if (mChoiceMode == CHOICE_MODE_NONE) {
            return;
        }

        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value && mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode == null) {
            if (mMultiChoiceModeCallback == null ||
                    !mMultiChoiceModeCallback.hasWrappedCallback()) {
                throw new IllegalStateException("AbsListView: attempted to start selection mode " +
                        "for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was " +
                        "supplied. Call setMultiChoiceModeListener to set a callback.");
            }
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
        }

        final boolean itemCheckChanged;
        if (mChoiceMode == CHOICE_MODE_MULTIPLE || mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            boolean oldValue = mCheckStates.get(position);
            mCheckStates.put(position, value);
            if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
                if (value) {
                    mCheckedIdStates.put(mAdapter.getItemId(position), position);
                } else {
                    mCheckedIdStates.delete(mAdapter.getItemId(position));
                }
            }
            itemCheckChanged = oldValue != value;
            if (itemCheckChanged) {
                if (value) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
            }
            if (mChoiceActionMode != null) {
                final long id = mAdapter.getItemId(position);
                mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                        position, id, value);
            }
        } else {
            boolean updateIds = mCheckedIdStates != null && mAdapter.hasStableIds();
            // Clear all values if we're checking something, or unchecking the currently
            // selected item
            itemCheckChanged = isItemChecked(position) != value;
            if (value || isItemChecked(position)) {
                mCheckStates.clear();
                if (updateIds) {
                    mCheckedIdStates.clear();
                }
            }
            // this may end up selecting the value we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (value) {
                mCheckStates.put(position, true);
                if (updateIds) {
                    mCheckedIdStates.put(mAdapter.getItemId(position), position);
                }
                mCheckedItemCount = 1;
            } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                mCheckedItemCount = 0;
            }
        }

        // Do not generate a data change while we are in the layout phase or data has not changed
        if (!mInLayout && !mBlockLayoutRequests && itemCheckChanged) {
            mDataChanged = true;
            rememberSyncState();
            requestLayout();
        }
    }

    /**
     * @see #setChoiceMode(int)
     *
     * @return The current choice mode
     */
    public int getChoiceMode() {
        return mChoiceMode;
    }

    /**
     * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
     * ({@link #CHOICE_MODE_NONE}). By setting the choiceMode to {@link #CHOICE_MODE_SINGLE}, the
     * List allows up to one item to  be in a chosen state. By setting the choiceMode to
     * {@link #CHOICE_MODE_MULTIPLE}, the list allows any number of items to be chosen.
     *
     * @param choiceMode One of {@link #CHOICE_MODE_NONE}, {@link #CHOICE_MODE_SINGLE}, or
     * {@link #CHOICE_MODE_MULTIPLE}
     */
    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        if (mChoiceActionMode != null) {
            mChoiceActionMode.finish();
            mChoiceActionMode = null;
        }
        if (mChoiceMode != CHOICE_MODE_NONE) {
            if (mCheckStates == null) {
                mCheckStates = new SparseBooleanArray(0);
            }
            if (mCheckedIdStates == null && mAdapter != null && mAdapter.hasStableIds()) {
                mCheckedIdStates = new LongSparseArray<Integer>(0);
            }
            // Modal multi-choice mode only has choices when the mode is active. Clear them.
            if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
                clearChoices();
                setLongClickable(true);
            }
        }
    }

    static class SavedState extends BaseSavedState {
        long selectedId;
        long firstId;
        int viewTop;
        int position;
        int height;
        String filter;
        boolean inActionMode;
        int checkedItemCount;
        SparseBooleanArray checkState;
        LongSparseArray<Integer> checkIdState;

        /**
         * Constructor called from {@link AbsListView#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            selectedId = in.readLong();
            firstId = in.readLong();
            viewTop = in.readInt();
            position = in.readInt();
            height = in.readInt();
            filter = in.readString();
            inActionMode = in.readByte() != 0;
            checkedItemCount = in.readInt();
            checkState = in.readSparseBooleanArray();
            final int N = in.readInt();
            if (N > 0) {
                checkIdState = new LongSparseArray<Integer>();
                for (int i=0; i<N; i++) {
                    final long key = in.readLong();
                    final int value = in.readInt();
                    checkIdState.put(key, value);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(selectedId);
            out.writeLong(firstId);
            out.writeInt(viewTop);
            out.writeInt(position);
            out.writeInt(height);
            out.writeString(filter);
            out.writeByte((byte) (inActionMode ? 1 : 0));
            out.writeInt(checkedItemCount);
            out.writeSparseBooleanArray(checkState);
            final int N = checkIdState != null ? checkIdState.size() : 0;
            out.writeInt(N);
            for (int i=0; i<N; i++) {
                out.writeLong(checkIdState.keyAt(i));
                out.writeInt(checkIdState.valueAt(i));
            }
        }

        @Override
        public String toString() {
            return "AbsListView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " selectedId=" + selectedId
                    + " firstId=" + firstId
                    + " viewTop=" + viewTop
                    + " position=" + position
                    + " height=" + height
                    + " filter=" + filter
                    + " checkState=" + checkState + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        /*
         * This doesn't really make sense as the place to dismiss the
         * popups, but there don't seem to be any other useful hooks
         * that happen early enough to keep from getting complaints
         * about having leaked the window.
         */
        dismissPopup();

        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        if (mPendingSync != null) {
            // Just keep what we last restored.
            ss.selectedId = mPendingSync.selectedId;
            ss.firstId = mPendingSync.firstId;
            ss.viewTop = mPendingSync.viewTop;
            ss.position = mPendingSync.position;
            ss.height = mPendingSync.height;
            ss.filter = mPendingSync.filter;
            ss.inActionMode = mPendingSync.inActionMode;
            ss.checkedItemCount = mPendingSync.checkedItemCount;
            ss.checkState = mPendingSync.checkState;
            ss.checkIdState = mPendingSync.checkIdState;
            return ss;
        }

        boolean haveChildren = getChildCount() > 0 && mItemCount > 0;
        long selectedId = getSelectedItemId();
        ss.selectedId = selectedId;
        ss.height = getHeight();

        if (selectedId >= 0) {
            // Remember the selection
            ss.viewTop = mSelectedTop;
            ss.position = getSelectedItemPosition();
            ss.firstId = INVALID_POSITION;
        } else {
            if (haveChildren && mFirstPosition > 0) {
                // Remember the position of the first child.
                // We only do this if we are not currently at the top of
                // the list, for two reasons:
                // (1) The list may be in the process of becoming empty, in
                // which case mItemCount may not be 0, but if we try to
                // ask for any information about position 0 we will crash.
                // (2) Being "at the top" seems like a special case, anyway,
                // and the user wouldn't expect to end up somewhere else when
                // they revisit the list even if its content has changed.
                View v = getChildAt(0);
                ss.viewTop = v.getTop();
                int firstPos = mFirstPosition;
                if (firstPos >= mItemCount) {
                    firstPos = mItemCount - 1;
                }
                ss.position = firstPos;
                ss.firstId = mAdapter.getItemId(firstPos);
            } else {
                ss.viewTop = 0;
                ss.firstId = INVALID_POSITION;
                ss.position = 0;
            }
        }

        ss.filter = null;
        if (mFiltered) {
            final EditText textFilter = mTextFilter;
            if (textFilter != null) {
                Editable filterText = textFilter.getText();
                if (filterText != null) {
                    ss.filter = filterText.toString();
                }
            }
        }

        ss.inActionMode = mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode != null;

        if (mCheckStates != null) {
            ss.checkState = mCheckStates.clone();
        }
        if (mCheckedIdStates != null) {
            final LongSparseArray<Integer> idState = new LongSparseArray<Integer>();
            final int count = mCheckedIdStates.size();
            for (int i = 0; i < count; i++) {
                idState.put(mCheckedIdStates.keyAt(i), mCheckedIdStates.valueAt(i));
            }
            ss.checkIdState = idState;
        }
        ss.checkedItemCount = mCheckedItemCount;

        if (mRemoteAdapter != null) {
            mRemoteAdapter.saveRemoteViewsCache();
        }

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        mDataChanged = true;

        mSyncHeight = ss.height;

        if (ss.selectedId >= 0) {
            mNeedSync = true;
            mPendingSync = ss;
            mSyncRowId = ss.selectedId;
            mSyncPosition = ss.position;
            mSpecificTop = ss.viewTop;
            mSyncMode = SYNC_SELECTED_POSITION;
        } else if (ss.firstId >= 0) {
            setSelectedPositionInt(INVALID_POSITION);
            // Do this before setting mNeedSync since setNextSelectedPosition looks at mNeedSync
            setNextSelectedPositionInt(INVALID_POSITION);
            mSelectorPosition = INVALID_POSITION;
            mNeedSync = true;
            mPendingSync = ss;
            mSyncRowId = ss.firstId;
            mSyncPosition = ss.position;
            mSpecificTop = ss.viewTop;
            mSyncMode = SYNC_FIRST_POSITION;
        }

        setFilterText(ss.filter);

        if (ss.checkState != null) {
            mCheckStates = ss.checkState;
        }

        if (ss.checkIdState != null) {
            mCheckedIdStates = ss.checkIdState;
        }

        mCheckedItemCount = ss.checkedItemCount;

        if (ss.inActionMode && mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL &&
                mMultiChoiceModeCallback != null) {
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
        }

        requestLayout();
    }


    // Setters
    /**
     * Defines the choice behavior for the {@link MultiChoiceRecyclerView}. By default, {@link MultiChoiceRecyclerView} does not have any choice behavior ({@code CHOICE_MODE_NONE}). By setting the choice mode to {@code CHOICE_MODE_MULTIPLE}, the {@link MultiChoiceRecyclerView} allows any number of items to be chosen.
     * @param choiceMode either {@code CHOICE_MODE_NONE} or {@code CHOICE_MODE_MULTIPLE}.
     */
    public void setChoiceMode(int choiceMode) {
        if (choiceMode == CHOICE_MODE_NONE || choiceMode == CHOICE_MODE_MULTIPLE) {
            mChoiceMode = choiceMode;
            if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates == null) {
                mCheckStates = new SparseBooleanArray(0);
            }
        }
    }

    /**
     * Clears any choices previously set.
     */
    public void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        for (int i = 0; i < getChildCount(); i++) {
            ViewCompat.setActivated(getChildAt(i), false);
        }
        mCheckedItemCount = 0;
    }

    /**
     * Sets the checked state of the specified position. The is only valid if the choice mode has been set to {@code CHOICE_MODE_MULTIPLE}.
     * @param view the view at the specified position.
     * @param position the item whose checked state is to be checked.
     * @param value the new checked state for the item.
     */
    public void setItemChecked(View view, int position, boolean value) {
        if (mChoiceMode == CHOICE_MODE_NONE) {
            return;
        }
        boolean oldValue = mCheckStates.get(position);
        mCheckStates.put(position, value);
        if (oldValue != value) {
            if (value) {
                mCheckedItemCount++;
            } else {
                mCheckedItemCount--;
            }
            ViewCompat.setActivated(view, value);
        }
    }

    // Getters
    /**
     * Returns the number of items currently selected. This will only be valid if the choice mode is not {@code CHOICE_MODE_NONE} (default).
     * @return the number of items currently selected.
     */
    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    /**
     * Returns the checked state of the specified position. The result is only valid if the choice mode has been set to {@code CHOICE_MODE_MULTIPLE}.
     * @param position the item whose checked state to return.
     * @return the item's checked state or <b>false</b> if choice mode is invalid.
     */
    public boolean isItemChecked(int position) {
        return mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null && mCheckStates.get(position);
    }

    /**
     * Returns the set of checked items in the {@link MultiChoiceRecyclerView}. The result is only valid if the choice mode has not been set to {@code CHOICE_MODE_NONE} (default).
     * @return the set of checked items.
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != CHOICE_MODE_NONE) {
            return mCheckStates;
        }
        return null;
    }

    // State
    static class SavedState extends BaseSavedState {

        // States
        int checkedItemCount;
        SparseBooleanArray checkStates;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel source) {
            super(source);
            checkedItemCount = source.readInt();
            checkStates = source.readSparseBooleanArray();
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(checkedItemCount);
            dest.writeSparseBooleanArray(checkStates);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        if (mCheckStates != null) {
            savedState.checkStates = mCheckStates.clone();
        }
        savedState.checkedItemCount = mCheckedItemCount;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.checkStates != null) {
            mCheckStates = savedState.checkStates;
        }
        mCheckedItemCount = savedState.checkedItemCount;
    }
}
