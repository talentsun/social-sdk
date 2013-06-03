package com.limijiaoyin.socialsdk.dialogs;

import com.limijiaoyin.socialsdk.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ShareDialog extends Dialog {
  private TextView mTitle;
  private EditText mCommentDialogContent;
  private TextView mShareTo;
  private TextView mDescription;
  private Button mShare;
  private Button mCancel;

  public ShareDialog(Context context) {
    this(context, R.style.ShareDialog);
  }

  public ShareDialog(Context context, int theme) {
    super(context, theme);

    setContentView(R.layout.share_dialog);

    mTitle = (TextView) findViewById(R.id.dialog_title);
    mCommentDialogContent = (EditText) findViewById(R.id.comment_dialog_content);
    mShareTo = (TextView) findViewById(R.id.share_to);
    mDescription = (TextView) findViewById(R.id.description);
    mShare = (Button) findViewById(R.id.share);
    mCancel = (Button) findViewById(R.id.cancel);

    Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/yuppy-sc.otf");
    mTitle.setTypeface(typeface);

    mCommentDialogContent.setTypeface(typeface);
    mShareTo.setTypeface(typeface);
    mDescription.setTypeface(typeface);
    mShare.setTypeface(typeface);
    mCancel.setTypeface(typeface);
  }



}
