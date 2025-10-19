package com.seafile.seadroid2.view.rich_edittext

import android.content.Context
import android.net.Uri
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.seafile.seadroid2.R
import com.seafile.seadroid2.config.Constants.DP
import com.seafile.seadroid2.databinding.LayoutUploadFileBinding

import com.seafile.seadroid2.view.MaxHeightScrollView

class RichEditText : MaxHeightScrollView {
    var DP_2: Int = DP.DP_2
    var DP_4: Int = DP.DP_4
    var DP_8: Int = DP.DP_8
    var DP_16: Int = DP.DP_16
    var DP_32: Int = DP.DP_32


    private var container: LinearLayout? = null
    private var inflater: LayoutInflater? = null
    private var keyListener: OnKeyListener? = null
    private var onRichAtListener: OnRichAtListener? = null

    private var onCloseClickListener: OnClickListener? = null

    private var focusListener: OnFocusChangeListener? = null

    private var onRichImageStatusChangeListener: OnRichImageClickListener? = null

    private var lastFocusEdit: EditText? = null

    private var removingImageIndex = 0

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context?) {
        inflater = LayoutInflater.from(context)

        initLayoutView(context)

        initListener()

        initFirstEditText()
    }

    private fun initLayoutView(context: Context?) {
        container = LinearLayout(context)
        container!!.setOrientation(LinearLayout.VERTICAL)

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(container, layoutParams)
    }

    private fun initFirstEditText() {
        val firstEditParam = LinearLayout.LayoutParams(-1, -2)
        val firstEdit = buildEditText()

        container!!.addView(firstEdit, firstEditParam)
        lastFocusEdit = firstEdit
    }

    private fun initListener() {
        keyListener = OnKeyListener { v: View?, keyCode: Int, event: KeyEvent? ->
            if (event!!.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                val edit = v as EditText?
                onBackspacePress(edit)
            }
            false
        }

        //
        onCloseClickListener = OnClickListener { v: View? ->
            if (v!!.getId() == R.id.remove) {
                val parentView = v.getParent() as FrameLayout?
                remove(parentView)
            }
        }

        //
        focusListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                lastFocusEdit = v as EditText
            }
        }
    }


    private fun onBackspacePress(editText: EditText?) {
        if (editText == null) {
            return
        }

        val startSelection = editText.getSelectionStart()
        if (startSelection != 0) {
            return
        }

        val indexOfChild = container!!.indexOfChild(editText)
        if (indexOfChild <= 0) {
            return
        }

        val preChildView = container!!.getChildAt(indexOfChild - 1)
        if (null == preChildView) {
            return
        }

        if (preChildView is FrameLayout) {
            remove(preChildView)
        } else if (preChildView is EditText) {
            val str1 = editText.getText().toString()

            val preEditText = preChildView
            val str2 = preEditText.getText().toString()

            container!!.removeView(editText)

            preEditText.setText(String.format("%s%s", str2, str1))
            preEditText.requestFocus()
            preEditText.setSelection(str2.length, str2.length)

            lastFocusEdit = preEditText
        }
    }

    fun remove(view: View?) {
        removingImageIndex = container!!.indexOfChild(view)
        container!!.removeView(view)
        mergeEditText()
    }

    fun setOnRichImageStatusChangeListener(l: OnRichImageClickListener?) {
        this.onRichImageStatusChangeListener = l
    }


    fun setOnRichAtListener(onRichAtListener: OnRichAtListener?) {
        this.onRichAtListener = onRichAtListener
    }

    fun insertImage(uri: Uri?) {
        if (null == uri) {
            return
        }

        val lastEditIndex = container!!.indexOfChild(lastFocusEdit)
        if (lastEditIndex + 1 < container!!.getChildCount()) {
            val v = container!!.getChildAt(lastEditIndex + 1)
            if (v is FrameLayout) {
                addImageViewAtIndex(lastEditIndex + 1, uri)
                addEditTextAtIndex(lastEditIndex + 2, "")
            } else {
                addImageViewAtIndex(lastEditIndex + 1, uri)
            }
        } else {
            addImageViewAtIndex(-1, uri)
            addEditTextAtIndex(-1, "")
        }
    }

    private fun addEditTextAtIndex(index: Int, editStr: CharSequence) {
        val editText = buildEditText()
        editText.setText(editStr)

        if (index == -1) {
            container!!.addView(editText)
        } else {
            container!!.addView(editText, index)
        }

        lastFocusEdit = editText
        lastFocusEdit!!.requestFocus()
        lastFocusEdit!!.setSelection(editStr.length, editStr.length)
    }

    private fun addImageViewAtIndex(index: Int, uri: Uri) {
        val uploadFileBinding = LayoutUploadFileBinding.inflate(inflater!!)
        uploadFileBinding.getRoot().setTag(VIEW_TAG_KV_IMAGE)
        uploadFileBinding.getRoot().setTag(VIEW_TAG_VALUE_IMAGE_URI, uri.toString())
        uploadFileBinding.remove.setOnClickListener(onCloseClickListener)
        uploadFileBinding.uploadImage.setOnClickListener(OnClickListener { v: View? ->
            if (onRichImageStatusChangeListener != null) {
                onRichImageStatusChangeListener!!.onClick(
                    uploadFileBinding.uploadImage,
                    uri.toString()
                )
            }
        })


        val llp = LinearLayout.LayoutParams(SizeUtils.dp2px(96f), SizeUtils.dp2px(96f))
        llp.topMargin = DP_2
        llp.bottomMargin = DP_2
        uploadFileBinding.getRoot().setLayoutParams(llp)

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(uploadFileBinding.uploadImage)

        if (index == -1) {
            container!!.addView(uploadFileBinding.getRoot())
        } else {
            container!!.addView(uploadFileBinding.getRoot(), index)
        }
    }

    private fun mergeEditText() {
        try {
            val preView = container!!.getChildAt(removingImageIndex - 1)
            val nextView = container!!.getChildAt(removingImageIndex)
            if (preView is EditText && nextView is EditText) {
                val preEdit = preView

                val nextEdit = nextView
                val str1 = preEdit.getText().toString()
                val str2 = nextEdit.getText().toString()
                var mergeText = ""
                if (str2.length > 0) {
                    mergeText = str1 + "\n" + str2
                } else {
                    mergeText = str1
                }

                container!!.removeView(nextEdit)
                preEdit.setText(mergeText)
                //设置光标的定位
                preEdit.requestFocus()
                preEdit.setSelection(str1.length, str1.length)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateUploadState(uri: String?, url: String?) {
        val c = container!!.getChildCount()
        if (c == 0) {
            return
        }
        var frame: View? = null
        for (i in 0..<c) {
            val key = container!!.getChildAt(i).getTag().toString()
            if (VIEW_TAG_KV_IMAGE != key) {
                continue
            }
            val uriKey = container!!.getChildAt(i).getTag(VIEW_TAG_VALUE_IMAGE_URI).toString()
            if (uriKey != uri) {
                continue
            }
            frame = container!!.getChildAt(i)
            break
        }

        if (null == frame) {
            return
        }
        frame.setTag(VIEW_TAG_VALUE_IMAGE_URL, url)
        frame.findViewById<View?>(R.id.upload_progress_bar).setVisibility(GONE)
        frame.findViewById<View?>(R.id.remove).setVisibility(VISIBLE)
    }


    private fun buildEditText(): EditText {
        val editText: EditText = DeletableEditText(getContext())
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        editText.setLayoutParams(layoutParams)
        editText.setTextSize(16f)
        editText.setCursorVisible(true)
        editText.setBackground(null)
        editText.setOnKeyListener(keyListener)
        editText.setOnFocusChangeListener(focusListener)
        editText.setPadding(0, DP_4, 0, DP_4)
        editText.setTag(VIEW_TAG_KV_INPUT)
        editText.setFilters(arrayOf<InputFilter>(object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                if (TextUtils.equals("@", source)) {
                    if (onRichAtListener != null) {
                        onRichAtListener!!.onCall(editText)
                    }
                }
                return null
            }
        }))
        return editText
    }

    fun buildRichEditData(): MutableList<RichContentModel?>? {
        val dataList: MutableList<RichContentModel?> = ArrayList<RichContentModel?>()
        val num = container!!.getChildCount()
        for (index in 0..<num) {
            val itemView = container!!.getChildAt(index)
            val richContentModel = RichContentModel()
            if (itemView is EditText) {
                val item = itemView
                richContentModel.content = item.getText().toString()
                richContentModel.type = 0
            } else if (itemView is FrameLayout) {
                val obj = itemView.getTag(VIEW_TAG_VALUE_IMAGE_URL)
                if (null == obj) {
                    return null
                }

                richContentModel.content = obj.toString()
                richContentModel.type = 1
            }
            if (!TextUtils.isEmpty(richContentModel.content)) {
                dataList.add(richContentModel)
            }
        }
        return dataList
    }

    override fun removeAllViews() {
        if (container != null) {
            container!!.removeAllViews()

            initFirstEditText()
        }
    }

    class RichContentModel {
        constructor()

        constructor(type: Int, content: String?) {
            this.type = type
            this.content = content
        }

        /**
         * content type, 0 is text, 1 is image
         */
        var type: Int = 0
        var content: String? = null
    }

    companion object {
        private const val VIEW_TAG_VALUE_IMAGE_URI = 0x2000002
        private const val VIEW_TAG_VALUE_IMAGE_URL = 0x2000003

        private const val VIEW_TAG_KV_INPUT = "input"
        private const val VIEW_TAG_KV_IMAGE = "image"
    }
}
