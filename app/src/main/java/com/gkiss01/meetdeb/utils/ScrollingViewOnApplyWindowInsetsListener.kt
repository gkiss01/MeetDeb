/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Rect
import android.view.View
import android.view.WindowInsets
import me.zhanghai.android.fastscroll.FastScroller

class ScrollingViewOnApplyWindowInsetsListener @JvmOverloads constructor(view: View? = null, fastScroller: FastScroller? = null) : View.OnApplyWindowInsetsListener {
    private val mPadding = Rect()
    private val mFastScroller: FastScroller?

    override fun onApplyWindowInsets(view: View, insets: WindowInsets): WindowInsets {
        view.setPadding(
            mPadding.left + insets.systemWindowInsetLeft, mPadding.top,
            mPadding.right + insets.systemWindowInsetRight,
            mPadding.bottom + insets.systemWindowInsetBottom
        )
        mFastScroller?.setPadding(
            insets.systemWindowInsetLeft, 0,
            insets.systemWindowInsetRight, insets.systemWindowInsetBottom
        )
        return insets
    }

    init {
        if (view != null) {
            mPadding[view.paddingLeft, view.paddingTop, view.paddingRight] = view.paddingBottom
        }
        mFastScroller = fastScroller
    }
}