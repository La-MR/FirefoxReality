/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.view.View;
import android.util.Log;
import android.widget.FrameLayout;

import org.mozilla.vrbrowser.Widget;

public class UIWidget extends FrameLayout implements Widget {
    UISurfaceTextureRenderer mRenderer;
    SurfaceTexture mTexture;
    int mOffset[] = new int[2];
    static final String LOGTAG = "VRB";

    public UIWidget(Context aContext) {
        super(aContext);
    }

    public UIWidget(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
    }

    public UIWidget(Context aContext, AttributeSet aAttrs, int aDefStyle) {
        super(aContext, aAttrs, aDefStyle);
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture aTexture, final int aWidth, final int aHeight) {
        if (mTexture!= null && (mTexture.equals(aTexture))) {
            Log.e(LOGTAG, "TEXTURE ALREADY SET");
            return;
        }
        mTexture = aTexture;
        if (mRenderer != null) {
            mRenderer.release();
        }
        if (aTexture != null) {
            mRenderer = new UISurfaceTextureRenderer(aTexture, aWidth, aHeight);
        }
        setWillNotDraw(mRenderer == null);
    }

    @Override
    public void handleTouchEvent(MotionEvent aEvent) {
        if (aEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
            requestFocus();
        }
        this.getLocationInWindow(mOffset);
        //Log.e(LOGTAG, "mOffset: " + mOffset[0] + " " + mOffset[1]);
        aEvent.offsetLocation(mOffset[0], mOffset[1]);
        this.dispatchTouchEvent(aEvent);
    }

    @Override
    public void handleHoverEvent(MotionEvent aEvent) {
        this.dispatchGenericMotionEvent(aEvent);
    }

    @Override
    public void releaseWidget() {
        if (mRenderer != null) {
            mRenderer.release();
            mRenderer = null;
        }
        mTexture = null;
    }

    @Override
    public void draw(Canvas aCanvas) {
        if (mRenderer == null) {
            super.draw(aCanvas);
            return;
        }
        Canvas textureCanvas = mRenderer.drawBegin();
        if(textureCanvas != null) {
            // set the proper scale
            float xScale = textureCanvas.getWidth() / (float)aCanvas.getWidth();
            textureCanvas.scale(xScale, xScale);
            // draw the view to SurfaceTexture
            super.draw(textureCanvas);
        }
        mRenderer.drawEnd();
    }

    @Override
    public void onDescendantInvalidated (View child, View target) {
        super.onDescendantInvalidated(child, target);
        if (mRenderer != null) {
            // TODO: transform rect and use invalidate(dirty)
            postInvalidate();
        }
    }

    // Need to keep this deprecated function to work on N versions of Android.
    @SuppressWarnings("deprecation")
    @Override
    public ViewParent invalidateChildInParent(int[] aLocation, Rect aDirty) {
        ViewParent parent =  super.invalidateChildInParent(aLocation, aDirty);
        if (parent != null && mRenderer != null) {
            // TODO: transform rect and use invalidate(dirty)
            postInvalidate();
        }
        return parent;
    }
}
