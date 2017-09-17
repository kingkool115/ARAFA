package com.android.pushbots;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amitshekhar.DebugDB;
import com.squareup.picasso.Picasso;

import java.util.List;

import db.DBHelper;
import util.Answer;
import util.Question;
import util.RestTask;


/**
 * This Activity displays a multiple choice or a text response question.
 * User can answer it by clicking on submit button.
 * */
public class AnswerQuestionActivity extends AppCompatActivity {

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    RestTask restTask;
    private static final String ACTION_FOR_INTENT_CALLBACK = "THIS_IS_A_UNIQUE_KEY_WE_USE_TO_COMMUNICATE";

    DBHelper dbHelper;
    Question question;

    TextView questionText;
    LinearLayout multiSelectAnswersLayout ;
    RadioGroup radioGroupAnswers;
    EditText textResponseTextField;

    ImageView expandedImageView;
    String imageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_question);
        getSupportActionBar().setTitle(R.string.question);

        restTask = new RestTask(this, ACTION_FOR_INTENT_CALLBACK);

        dbHelper = new DBHelper(this);
        question = dbHelper.getQuestionById(getIntent().getStringExtra("question_id"));
        imageUrl = question.getImageUrl();

        questionText = (TextView) findViewById(R.id.multiple_choice_question);
        questionText.setText(question.getQuestion());

        // set image for this question
        setImage();

        initQuestionType();


    }

    /**
     * Get Question object.
     * */
    public Question getQuestion() {
        return question;
    }

    /**
     * Create the onClickListener for submit button.
     * This OnClickListener handles every of the three question types.
     *
     * @param isTextResponse true if it's a text response question, else false.
     * @param isMultiSelect true if more than one answer is correct, else false.
     * @return OnClickListener instance.
     * */
    private View.OnClickListener getSubmitOnClickListener(final boolean isTextResponse,
                                                          final boolean isMultiSelect) {
        return new View.OnClickListener() {
            public void onClick(View v) {


                // iterate all lecture checkboxes
                String answer = "";
                // multiple-choice -> multi select
                if (!isTextResponse && isMultiSelect) {
                    for (int i = 0; i < multiSelectAnswersLayout.getChildCount(); i++) {
                        CheckBox checkbox = (CheckBox) multiSelectAnswersLayout.getChildAt(i);
                        if (checkbox.isChecked()) {
                            answer += checkbox.getTag() + ",";
                        }
                    }
                // multiple choice -> single select
                } else if (!isTextResponse && !isMultiSelect) {
                    for (int i = 0; i < radioGroupAnswers.getChildCount(); i++) {
                        RadioButton checkbox = (RadioButton) radioGroupAnswers.getChildAt(i);
                        if (checkbox.isChecked()) {
                            answer += checkbox.getTag();
                        }
                    }
                // text reponse
                } else {
                    answer = textResponseTextField.getText().toString();
                }

                restTask.submitAnswer(question, answer);

                Intent openQuestionsActivity = new Intent(AnswerQuestionActivity.this, OpenQuestionsActivity.class);
                AnswerQuestionActivity.this.startActivity(openQuestionsActivity);
                AnswerQuestionActivity.this.finish();
            }
        };
    }

    /**
     * different widgets for different types of question.
     * */
    private void initQuestionType() {
        multiSelectAnswersLayout = (LinearLayout) findViewById(R.id.multiSelectAnswersLayout);
        radioGroupAnswers = (RadioGroup) findViewById(R.id.radioGroupAnswers);
        textResponseTextField = (EditText) findViewById(R.id.textResponseTextField);
        Button submitButton = (Button) findViewById(R.id.submit_answer_button);
        submitButton.setOnClickListener(getSubmitOnClickListener(question.isTr(),
                question.isMultiSelect()));

        // multiple-choice -> multi select
        if (!question.isTr() && question.isMultiSelect()) {
            multiSelectAnswersLayout.setVisibility(View.VISIBLE);
            radioGroupAnswers.setVisibility(View.GONE);
            textResponseTextField.setVisibility(View.GONE);
            // multiple choice -> single select
        } else if (!question.isTr() && !question.isMultiSelect()) {
            radioGroupAnswers.setVisibility(View.VISIBLE);
            multiSelectAnswersLayout.setVisibility(View.GONE);
            textResponseTextField.setVisibility(View.GONE);
            // text reponse
        } else {
            textResponseTextField.setVisibility(View.VISIBLE);
            multiSelectAnswersLayout.setVisibility(View.GONE);
            radioGroupAnswers.setVisibility(View.GONE);
        }

        // add possible answers to view
        if (!question.isTr()) {
            List<Answer> possibleAnswers = dbHelper.getAnswersOfQuestion(question.getId());
            // it's a multiple choice question
            for (Answer answer : possibleAnswers) {
                // just one answer is correct
                if (!question.isTr() && !question.isMultiSelect()) {
                    RadioButton button = new RadioButton(this);
                    button.setText(answer.getAnswer());
                    button.setTag(answer.getId());
                    button.setTextSize(18);
                    radioGroupAnswers.addView(button);
                    // more than one answer is correct
                } else if (!question.isTr() && question.isMultiSelect()){
                    CheckBox checkbox = new CheckBox(this);
                    checkbox.setText(answer.getAnswer());
                    checkbox.setTag(answer.getId());
                    checkbox.setTextSize(18);
                    multiSelectAnswersLayout.addView(checkbox);
                }
            }
        }
    }

    /**
     * Set the image for this question if there is any.
     * */
    private void setImage() {
        final ImageButton smallQuestionImage = (ImageButton) findViewById(R.id.multiple_choice_image_button);
        Picasso.with(this)
                .load(question.getImageUrl()).fit()
                .into(smallQuestionImage);

        smallQuestionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageFromThumb(smallQuestionImage);
            }
        });

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    /**
     * Make Picture from question full sized.
     *
     * @param thumbView where full size picture will be displayed.
     * */
    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        expandedImageView = (ImageView) findViewById(R.id.expanded_multiple_choice_image);
        Picasso.with(this)
                .load(imageUrl)
                .into(expandedImageView);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.linearLayout)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
